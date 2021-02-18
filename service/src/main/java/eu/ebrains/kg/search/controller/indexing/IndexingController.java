package eu.ebrains.kg.search.controller.indexing;

import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.controller.elasticsearch.ElasticSearchController;
import eu.ebrains.kg.search.controller.mapping.MappingController;
import eu.ebrains.kg.search.controller.translators.TranslationController;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstances;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class IndexingController {

    private final MappingController mappingController;
    private final ElasticSearchController elasticSearchController;
    private final TranslationController translationController;

    public IndexingController(MappingController mappingController, ElasticSearchController elasticSearchController, TranslationController translationController) {
        this.mappingController = mappingController;
        this.elasticSearchController = elasticSearchController;
        this.translationController = translationController;
    }

    public void incrementalUpdateAll(DataStage dataStage, String authorization, String legacyAuthorization){
        Constants.TARGET_MODELS_MAP.forEach((type, clazz) -> incrementalUpdateByType(dataStage, type, authorization, legacyAuthorization));
    }

    public void incrementalUpdateByType(DataStage dataStage, String type, String authorization, String legacyAuthorization) {
        TargetInstances instances = translationController.createInstances(dataStage, false, type, authorization, legacyAuthorization);
        elasticSearchController.updateIndex(instances.getSearchableInstances(), type, dataStage);
    }

    public void fullReplacementByType(DataStage dataStage, String type, String authorization, String legacyAuthorization, Class<?> clazz) {
        TargetInstances instances = translationController.createInstances(dataStage, false, type, authorization, legacyAuthorization);
        recreateIndex(dataStage, type, clazz);
        elasticSearchController.indexDocuments(instances.getSearchableInstances(), type, dataStage);
    }

    private void recreateIndex(DataStage dataStage, String type, Class<?> clazz) {
        Map<String, Object> mapping = mappingController.generateMapping(clazz);
        Map<String, Object> mappingResult = Map.of("mappings", mapping);
        elasticSearchController.recreateIndex(mappingResult, type, dataStage);
    }
}
