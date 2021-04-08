package eu.ebrains.kg.search.controller.indexing;

import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.controller.elasticsearch.ElasticSearchController;
import eu.ebrains.kg.search.controller.mapping.MappingController;
import eu.ebrains.kg.search.controller.translators.TranslationController;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstances;
import eu.ebrains.kg.search.utils.MetaModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Component
public class IndexingController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final MappingController mappingController;
    private final ElasticSearchController elasticSearchController;
    private final TranslationController translationController;
    private final MetaModelUtils utils;

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
        String type = utils.getNameForClass(clazz);
        TargetInstances instances = translationController.createInstancesCombined(clazz, dataStage, false, legacyAuthorization);
        if (!CollectionUtils.isEmpty(instances.getSearchableInstances())) {
            elasticSearchController.updateSearchIndex(instances.getSearchableInstances(), type, dataStage);
        }
        if (!CollectionUtils.isEmpty(instances.getAllInstances())) {
            elasticSearchController.updateIdentifiersIndex(instances.getAllInstances(), type, dataStage);
        }
    }

    public void fullReplacementByType(Class<?> clazz, DataStage dataStage, String legacyAuthorization) {
        String type = utils.getNameForClass(clazz);
        TargetInstances instances = translationController.createInstancesCombined(clazz, dataStage, false, legacyAuthorization);
        logger.info(String.format("Creating index %s_%s for %s", dataStage, type.toLowerCase(), type));
        recreateSearchIndex(dataStage, type, clazz);
        logger.info(String.format("Created index %s_%s for %s", dataStage, type.toLowerCase(), type));
        if (!CollectionUtils.isEmpty(instances.getSearchableInstances())) {
            logger.info(String.format("Start search indexing %s instances for %s", instances.getSearchableInstances().size(), type));
            elasticSearchController.indexSearchDocuments(instances.getSearchableInstances(), type, dataStage);
            logger.info(String.format("Indexed search for %s", type));
        } else {
            logger.info(String.format("Bypass search indexing for %s, because no instance", type));
        }
        if (!CollectionUtils.isEmpty(instances.getAllInstances())) {
            logger.info(String.format("Start instance indexing %s instances with %s", instances.getAllInstances().size(), type));
            elasticSearchController.indexIdentifierDocuments(instances.getAllInstances(), dataStage);
            logger.info(String.format("Indexed instance with %s", type));
        } else {
            logger.info(String.format("Bypass instance indexing with %s, because no instance", type));
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
