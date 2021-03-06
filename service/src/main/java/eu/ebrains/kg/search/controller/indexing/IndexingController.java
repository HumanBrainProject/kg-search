/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

package eu.ebrains.kg.search.controller.indexing;

import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.controller.elasticsearch.ElasticSearchController;
import eu.ebrains.kg.search.controller.mapping.MappingController;
import eu.ebrains.kg.search.controller.translators.TargetInstancesResult;
import eu.ebrains.kg.search.controller.translators.TranslationController;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.IdSources;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.utils.MetaModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class IndexingController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final MappingController mappingController;
    private final ElasticSearchController elasticSearchController;
    private final TranslationController translationController;
    private final MetaModelUtils utils;

    private int getBulkSize(Class<?> clazz) {
        return Constants.isSourceModelBig(clazz) ? 100 : 1000;
    }

    public IndexingController(MappingController mappingController, ElasticSearchController elasticSearchController, TranslationController translationController, MetaModelUtils utils) {
        this.mappingController = mappingController;
        this.elasticSearchController = elasticSearchController;
        this.translationController = translationController;
        this.utils = utils;
    }

    public void incrementalUpdateAll(DataStage dataStage, String legacyAuthorization) {
        Constants.TARGET_MODELS_ORDER.forEach(clazz -> incrementalUpdateByType(clazz, dataStage, legacyAuthorization));
    }

    public void incrementalUpdateAutoRelease() {
        Constants.AUTO_RELEASED_MODELS_ORDER.forEach(clazz -> {
            String type = utils.getNameForClass(clazz);
            Set<String> ids = new HashSet<>();
            boolean hasMore = true;
            int from = 10;
            while (hasMore) {
                TargetInstancesResult result = translationController.createInstancesFromV3ForIndexing(clazz, DataStage.IN_PROGRESS, false, from, getBulkSize(clazz)); //TODO: Change to RELEASED
                List<TargetInstance> instances = result.getTargetInstances();
                if (instances != null) {
                    ids.addAll(instances.stream().map(TargetInstance::getId).collect(Collectors.toList()));
                    elasticSearchController.updateAutoReleasedIndex(instances, type);
                }
                from = result.getFrom() + result.getSize();
                hasMore = from < result.getTotal();
            }
            elasticSearchController.removeDeprecatedDocumentsFromAutoReleasedIndex(type, ids);
        });
    }

    public void incrementalUpdateByType(Class<?> clazz, DataStage dataStage, String legacyAuthorization) {
        if (translationController.isTypeCombined(clazz)) {
            incrementalUpdateCombinedByType(clazz, dataStage, legacyAuthorization);
        } else {
            incrementalUpdateFromV3ByType(clazz, dataStage);
        }
    }

    public void incrementalUpdateCombinedByType(Class<?> clazz, DataStage dataStage, String legacyAuthorization) {
        String type = utils.getNameForClass(clazz);
        List<IdSources> sources = translationController.getIdSources(clazz, dataStage, legacyAuthorization);
        Set<String> searchableIds = new HashSet<>();
        Set<String> nonSearchableIds = new HashSet<>();
        List<TargetInstance> searchableInstances = new ArrayList<>();
        List<TargetInstance> nonSearchableInstances = new ArrayList<>();
        int counter = 0;
        for (IdSources source : sources) {
            TargetInstance instance = translationController.createInstanceCombinedForIndexing(clazz, dataStage, false, legacyAuthorization, source);
            if (instance != null) {
                counter++;
                if (instance.isSearchable()) {
                    searchableIds.add(instance.getId());
                    searchableInstances.add(instance);
                } else {
                    nonSearchableIds.add(instance.getId());
                    nonSearchableInstances.add(instance);
                }
                if (searchableInstances.size() == getBulkSize(clazz)) {
                    elasticSearchController.updateSearchIndex(searchableInstances, type, dataStage);
                    searchableInstances = new ArrayList<>();
                }
                if (nonSearchableInstances.size() == getBulkSize(clazz)) {
                    elasticSearchController.updateIdentifiersIndex(nonSearchableInstances, dataStage);
                    nonSearchableInstances = new ArrayList<>();
                }
            }
        }
        if (searchableInstances.size() > 0) {
            elasticSearchController.updateSearchIndex(searchableInstances, type, dataStage);
        }
        if (nonSearchableInstances.size() > 0) {
            elasticSearchController.updateIdentifiersIndex(nonSearchableInstances, dataStage);
        }
        elasticSearchController.removeDeprecatedDocumentsFromSearchIndex(type, dataStage, searchableIds);
        elasticSearchController.removeDeprecatedDocumentsFromIdentifiersIndex(type, dataStage, nonSearchableIds);
        logger.info(String.format("Created %d instances for type %s!", counter, type));
    }

    public void incrementalUpdateFromV3ByType(Class<?> clazz, DataStage dataStage) {
        String type = utils.getNameForClass(clazz);
        Set<String> searchableIds = new HashSet<>();
        Set<String> nonSearchableIds = new HashSet<>();
        boolean hasMore = true;
        int from = 10;
        while (hasMore) {
            TargetInstancesResult result = translationController.createInstancesFromV3ForIndexing(clazz, dataStage, false, from, getBulkSize(clazz));
            List<TargetInstance> instances = result.getTargetInstances();
            if (instances != null) {
                List<TargetInstance> searchableInstances = new ArrayList<>();
                List<TargetInstance> nonSearchableInstances = new ArrayList<>();
                instances.forEach(instance -> {
                    if (instance.isSearchable()) {
                        searchableIds.add(instance.getId());
                        searchableInstances.add(instance);
                    } else {
                        nonSearchableIds.add(instance.getId());
                        nonSearchableInstances.add(instance);
                    }
                });
                if (!CollectionUtils.isEmpty(searchableInstances)) {
                    elasticSearchController.updateSearchIndex(searchableInstances, type, dataStage);
                }
                if (!CollectionUtils.isEmpty(nonSearchableInstances)) {
                    elasticSearchController.updateIdentifiersIndex(nonSearchableInstances, dataStage);
                }
            }
            from = result.getFrom() + result.getSize();
            hasMore = from < result.getTotal();
        }
        elasticSearchController.removeDeprecatedDocumentsFromSearchIndex(type, dataStage, searchableIds);
        elasticSearchController.removeDeprecatedDocumentsFromIdentifiersIndex(type, dataStage, nonSearchableIds);
    }


    public void fullReplacementByType(Class<?> clazz, DataStage dataStage, String legacyAuthorization) {
        if (translationController.isTypeCombined(clazz)) {
            fullReplacementCombinedByType(clazz, dataStage, legacyAuthorization);
        } else {
            fullReplacementFromV3ByType(clazz, dataStage);
        }
    }

    public void fullReplacementAutoReleasedByType(Class<?> clazz) {
        String type = utils.getNameForClass(clazz);
        logger.info(String.format("Creating index auto_released_%s for %s", type.toLowerCase(), type));
        recreateAutoReleasedIndex(type, clazz);
        logger.info(String.format("Successfully created index auto_released_%s for %s", type.toLowerCase(), type));
        boolean hasMore = true;
        int from = 0;
        while (hasMore) {
            TargetInstancesResult result = translationController.createInstancesFromV3ForIndexing(clazz, DataStage.IN_PROGRESS, false, from, getBulkSize(clazz)); //TODO: Change Datastage to RELEASED
            List<TargetInstance> instances = result.getTargetInstances();
            if (instances != null) {
                elasticSearchController.updateAutoReleasedIndex(instances, type);
            }
            from = result.getFrom() + result.getSize();
            hasMore = from < result.getTotal();
        }
    }

    public void fullReplacementCombinedByType(Class<?> clazz, DataStage dataStage, String legacyAuthorization) {
        String type = utils.getNameForClass(clazz);
        logger.info(String.format("Creating index %s_searchable_%s for %s", dataStage, type.toLowerCase(), type));
        recreateSearchIndex(dataStage, type, clazz);
        logger.info(String.format("Successfully created index %s_searchable_%s for %s", dataStage, type.toLowerCase(), type));
        List<IdSources> sources = translationController.getIdSources(clazz, dataStage, legacyAuthorization);
        Set<String> nonSearchableIds = new HashSet<>();
        List<TargetInstance> searchableInstances = new ArrayList<>();
        List<TargetInstance> nonSearchableInstances = new ArrayList<>();
        int counter = 0;
        for (IdSources source : sources) {
            TargetInstance instance = translationController.createInstanceCombinedForIndexing(clazz, dataStage, false, legacyAuthorization, source);
            if (instance != null) {
                counter++;
                if (instance.isSearchable()) {
                    searchableInstances.add(instance);
                } else {
                    nonSearchableIds.add(instance.getId());
                    nonSearchableInstances.add(instance);
                }
                if (searchableInstances.size() == getBulkSize(clazz)) {
                    elasticSearchController.updateSearchIndex(searchableInstances, type, dataStage);
                    searchableInstances = new ArrayList<>();
                }
                if (nonSearchableInstances.size() == getBulkSize(clazz)) {
                    elasticSearchController.updateIdentifiersIndex(nonSearchableInstances, dataStage);
                    nonSearchableInstances = new ArrayList<>();
                }
            }
        }
        if (searchableInstances.size() > 0) {
            elasticSearchController.updateSearchIndex(searchableInstances, type, dataStage);
        }
        if (nonSearchableInstances.size() > 0) {
            elasticSearchController.updateIdentifiersIndex(nonSearchableInstances, dataStage);
        }
        elasticSearchController.removeDeprecatedDocumentsFromIdentifiersIndex(type, dataStage, nonSearchableIds);
        logger.info(String.format("Created %d instances for type %s!", counter, type));
    }

    public void fullReplacementFromV3ByType(Class<?> clazz, DataStage dataStage) {
        String type = utils.getNameForClass(clazz);
        logger.info(String.format("Creating index %s_searchable_%s for %s", dataStage, type.toLowerCase(), type));
        recreateSearchIndex(dataStage, type, clazz);
        logger.info(String.format("Successfully created index %s_searchable_%s for %s", dataStage, type.toLowerCase(), type));
        Set<String> nonSearchableIds = new HashSet<>();
        boolean hasMore = true;
        int from = 0;
        while (hasMore) {
            TargetInstancesResult result = translationController.createInstancesFromV3ForIndexing(clazz, dataStage, false, from, getBulkSize(clazz));
            List<TargetInstance> instances = result.getTargetInstances();
            if (instances != null) {
                List<TargetInstance> searchableInstances = new ArrayList<>();
                List<TargetInstance> nonSearchableInstances = new ArrayList<>();
                instances.forEach(instance -> {
                    if (instance.isSearchable()) {
                        searchableInstances.add(instance);
                    } else {
                        nonSearchableIds.add(instance.getId());
                        nonSearchableInstances.add(instance);
                    }
                });
                if (!CollectionUtils.isEmpty(searchableInstances)) {
                    elasticSearchController.updateSearchIndex(searchableInstances, type, dataStage);
                }
                if (!CollectionUtils.isEmpty(nonSearchableInstances)) {
                    elasticSearchController.updateIdentifiersIndex(nonSearchableInstances, dataStage);
                }
            }
            from = result.getFrom() + result.getSize();
            hasMore = from < result.getTotal();
        }
        elasticSearchController.removeDeprecatedDocumentsFromIdentifiersIndex(type, dataStage, nonSearchableIds);
    }

    public void recreateIdentifiersIndex(DataStage dataStage) {
        Map<String, Object> mapping = mappingController.generateIdentifierMapping();
        Map<String, Object> mappingResult = Map.of("mappings", mapping);
        elasticSearchController.recreateIdentifiersIndex(mappingResult, dataStage);
    }

    private void recreateSearchIndex(DataStage dataStage, String type, Class<?> clazz) {
        Map<String, Object> mapping = mappingController.generateMapping(clazz);
        Map<String, Object> mappingResult = Map.of("mappings", mapping);
        elasticSearchController.recreateSearchIndex(mappingResult, type, dataStage);
    }

    private void recreateAutoReleasedIndex(String type, Class<?> clazz) {
        Map<String, Object> mapping = mappingController.generateMapping(clazz);
        Map<String, Object> mappingResult = Map.of("mappings", mapping);
        elasticSearchController.recreateAutoReleasedIndex(mappingResult, type);
    }
}