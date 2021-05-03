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
        Set<String> ids = new HashSet<>();
        List<TargetInstance> instances = new ArrayList<>();
        int counter = 0;
        for (IdSources source : sources) {
            TargetInstance instance = translationController.createInstanceCombinedForIndexing(clazz, dataStage, false, legacyAuthorization, source);
            if (instance != null) {
                counter++;
                    ids.add(instance.getId());
                    instances.add(instance);
                if (instances.size() == BULK_INSTANCES_SIZE) {
                    elasticSearchController.updateSearchIndex(instances, type, dataStage);
                    instances = new ArrayList<>();
                }
            }
        }
        if (instances.size() > 0) {
            elasticSearchController.updateSearchIndex(instances, type, dataStage);
        }
        if (TargetInstance.isSearchable(clazz)) {
            elasticSearchController.removeDeprecatedDocumentsFromSearchIndex(type, dataStage, ids);
        } else {
            elasticSearchController.removeDeprecatedDocumentsFromIdentifiersIndex(type, dataStage, ids);
        }
        logger.info(String.format("Created %d instances for type %s!", counter, type));
    }

    public void incrementalUpdateFromV3ByType(Class<?> clazz, DataStage dataStage) {
        String type = utils.getNameForClass(clazz);
        Set<String> ids = new HashSet<>();
        boolean hasMore = true;
        int from = 0;
        while (hasMore) {
            TargetInstancesResult result = translationController.createInstancesFromV3ForIndexing(clazz, dataStage, false, from, BULK_INSTANCES_SIZE);
            List<TargetInstance> instances = result.getTargetInstances();
            if (!CollectionUtils.isEmpty(instances)) {
                instances.forEach(i -> ids.add(i.getId()));
                if (TargetInstance.isSearchable(clazz)) {
                    elasticSearchController.updateSearchIndex(instances, type, dataStage);
                } else {
                    elasticSearchController.updateIdentifiersIndex(instances, dataStage);
                }
            }
            from = result.getFrom() + result.getSize();
            hasMore = from < result.getTotal();
        }
        if (TargetInstance.isSearchable(clazz)) {
            elasticSearchController.removeDeprecatedDocumentsFromSearchIndex(type, dataStage, ids);
        } else {
            elasticSearchController.removeDeprecatedDocumentsFromIdentifiersIndex(type, dataStage, ids);
        }
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
        Set<String> ids = new HashSet<>();
        List<TargetInstance> instances = new ArrayList<>();
        for (IdSources source : sources) {
            TargetInstance instance = translationController.createInstanceCombinedForIndexing(clazz, dataStage, false, legacyAuthorization, source);
            if (instance != null) {
                ids.add(instance.getId());
                instances.add(instance);
                if (instances.size() == BULK_INSTANCES_SIZE) {
                    if (TargetInstance.isSearchable(clazz)) {
                        elasticSearchController.updateSearchIndex(instances, type, dataStage);
                    } else {
                        elasticSearchController.updateIdentifiersIndex(instances, dataStage);
                    }
                    instances = new ArrayList<>();
                }
            }
        }
        if (instances.size() > 0) {
            if (TargetInstance.isSearchable(clazz)) {
                elasticSearchController.updateSearchIndex(instances, type, dataStage);
            } else {
                elasticSearchController.updateIdentifiersIndex(instances, dataStage);
            }
        }
        if (!TargetInstance.isSearchable(clazz)) {
            elasticSearchController.removeDeprecatedDocumentsFromIdentifiersIndex(type, dataStage, ids);
        }
        logger.info(String.format("Created %d instances for type %s!", ids.size(), type));
    }

    public void fullReplacementFromV3ByType(Class<?> clazz, DataStage dataStage) {
        String type = utils.getNameForClass(clazz);
        logger.info(String.format("Creating index %s_%s for %s", dataStage, type.toLowerCase(), type));
        recreateSearchIndex(dataStage, type, clazz);
        logger.info(String.format("Successfully created index %s_%s for %s", dataStage, type.toLowerCase(), type));
        Set<String> ids = new HashSet<>();
        boolean hasMore = true;
        int from = 0;
        while (hasMore) {
            TargetInstancesResult result = translationController.createInstancesFromV3ForIndexing(clazz, dataStage, false, from, BULK_INSTANCES_SIZE);
            List<TargetInstance> instances = result.getTargetInstances();
            if (!CollectionUtils.isEmpty(instances)) {
                instances.forEach(i -> ids.add(i.getId()));
                if (TargetInstance.isSearchable(clazz)) {
                    elasticSearchController.updateSearchIndex(instances, type, dataStage);
                } else {
                    elasticSearchController.updateIdentifiersIndex(instances, dataStage);
                }
            }
            from = result.getFrom() + result.getSize();
            hasMore = from < result.getTotal();
        }
        if (!TargetInstance.isSearchable(clazz)) {
            elasticSearchController.removeDeprecatedDocumentsFromIdentifiersIndex(type, dataStage, ids);
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
