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

@Component
public class IndexingController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final MappingController mappingController;
    private final ElasticSearchController elasticSearchController;
    private final TranslationController translationController;
    private final MetaModelUtils utils;

    private final int BULK_INSTANCES_SIZE = 20;

    public IndexingController(MappingController mappingController, ElasticSearchController elasticSearchController, TranslationController translationController, MetaModelUtils utils) {
        this.mappingController = mappingController;
        this.elasticSearchController = elasticSearchController;
        this.translationController = translationController;
        this.utils = utils;
    }

    public void incrementalUpdateAll(DataStage dataStage, String legacyAuthorization){
        Constants.TARGET_MODELS_ORDER.forEach(clazz -> incrementalUpdateByType(clazz, dataStage, legacyAuthorization));
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
        for (IdSources source : sources) {
            TargetInstance instance = translationController.createInstanceCombinedForIndexing(clazz, dataStage, false, legacyAuthorization, source);
            if (instance != null) {
                if (instance.isSearchable()) {
                    searchableIds.add(instance.getId());
                    searchableInstances.add(instance);
                } else {
                    nonSearchableIds.add(instance.getId());
                    nonSearchableInstances.add(instance);
                }
                if (searchableInstances.size() == BULK_INSTANCES_SIZE) {
                    elasticSearchController.updateSearchIndex(searchableInstances, type, dataStage);
                    searchableInstances = new ArrayList<>();
                }
                if (nonSearchableInstances.size() == BULK_INSTANCES_SIZE) {
                    elasticSearchController.updateSearchIndex(nonSearchableInstances, type, dataStage);
                    nonSearchableInstances = new ArrayList<>();
                }
            }
        }
        elasticSearchController.removeDeprecatedDocumentsFromSearchIndex(type, dataStage, searchableIds);
        elasticSearchController.removeDeprecatedDocumentsFromIdentifiersIndex(type, dataStage, nonSearchableIds);
    }

    public void incrementalUpdateFromV3ByType(Class<?> clazz, DataStage dataStage) {
        String type = utils.getNameForClass(clazz);
        Set<String> searchableIds = new HashSet<>();
        Set<String> nonSearchableIds = new HashSet<>();
        boolean hasMore = true;
        int from = 0;
        while (hasMore) {
            TargetInstancesResult result = translationController.createInstancesFromV3ForIndexing(clazz, dataStage, false, from, BULK_INSTANCES_SIZE);
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
    public void fullReplacementCombinedByType(Class<?> clazz, DataStage dataStage, String legacyAuthorization) {
        String type = utils.getNameForClass(clazz);
        logger.info(String.format("Creating index %s_%s for %s", dataStage, type.toLowerCase(), type));
        recreateSearchIndex(dataStage, type, clazz);
        logger.info(String.format("Successfully created index %s_%s for %s", dataStage, type.toLowerCase(), type));
        List<IdSources> sources = translationController.getIdSources(clazz, dataStage, legacyAuthorization);
        List<TargetInstance> searchableInstances = new ArrayList<>();
        List<TargetInstance> nonSearchableInstances = new ArrayList<>();
        for (IdSources source : sources) {
            TargetInstance instance = translationController.createInstanceCombinedForIndexing(clazz, dataStage, false, legacyAuthorization, source);
            if (instance != null) {
                if (instance.isSearchable()) {
                    searchableInstances.add(instance);
                } else {
                    nonSearchableInstances.add(instance);
                }
                if (searchableInstances.size() == BULK_INSTANCES_SIZE) {
                    elasticSearchController.updateSearchIndex(searchableInstances, type, dataStage);
                    searchableInstances = new ArrayList<>();
                }
                if (nonSearchableInstances.size() == BULK_INSTANCES_SIZE) {
                    elasticSearchController.updateSearchIndex(nonSearchableInstances, type, dataStage);
                    nonSearchableInstances = new ArrayList<>();
                }
            }
        }
    }

    public void fullReplacementFromV3ByType(Class<?> clazz, DataStage dataStage) {
        String type = utils.getNameForClass(clazz);
        logger.info(String.format("Creating index %s_%s for %s", dataStage, type.toLowerCase(), type));
        recreateSearchIndex(dataStage, type, clazz);
        logger.info(String.format("Successfully created index %s_%s for %s", dataStage, type.toLowerCase(), type));
        boolean hasMore = true;
        int from = 0;
        while (hasMore) {
            TargetInstancesResult result = translationController.createInstancesFromV3ForIndexing(clazz, dataStage, false, from, BULK_INSTANCES_SIZE);
            List<TargetInstance> instances = result.getTargetInstances();
            if (instances != null) {
                List<TargetInstance> searchableInstances = new ArrayList<>();
                List<TargetInstance> nonSearchableInstances = new ArrayList<>();
                instances.forEach(instance -> {
                    if (instance.isSearchable()) {
                        searchableInstances.add(instance);
                    } else {
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
}
