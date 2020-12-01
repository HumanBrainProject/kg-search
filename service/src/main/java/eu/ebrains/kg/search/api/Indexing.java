package eu.ebrains.kg.search.api;

import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.controller.elasticsearch.ElasticSearchController;
import eu.ebrains.kg.search.controller.mapping.MappingController;
import eu.ebrains.kg.search.controller.translators.TranslationController;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@RequestMapping("/indexing")
@RestController
public class Indexing {

    private final MappingController mappingController;
    private final ElasticSearchController elasticSearchController;
    private final TranslationController translationController;

    public Indexing(MappingController mappingController, ElasticSearchController elasticSearchController, TranslationController translationController) {
        this.mappingController = mappingController;
        this.elasticSearchController = elasticSearchController;
        this.translationController = translationController;
    }

    @PostMapping
    public ResponseEntity<?> fullReplacement(@RequestParam("databaseScope") DatabaseScope databaseScope, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        try {
            Constants.TARGET_MODELS_MAP.forEach((type, clazz) -> recreateIndex(databaseScope, type, clazz));
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/{type}")
    public ResponseEntity<?> fullReplacementByType(@RequestParam("databaseScope") DatabaseScope databaseScope,
                                                   @PathVariable("type") String type,
                                                   @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        Class<?> clazz = Constants.TARGET_MODELS_MAP.get(type);
        if (clazz != null) {
            try {
                recreateIndex(databaseScope, type, clazz);
                List<TargetInstance> instances = translationController.createInstances(databaseScope, false, type, authorization);
//                elasticSearchController.
                return ResponseEntity.ok().build();
            } catch (WebClientResponseException e) {
                return ResponseEntity.status(e.getStatusCode()).build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping
    public void incrementalUpdate(@RequestParam("databaseScope") DatabaseScope databaseScope) {

    }

    private void recreateIndex(DatabaseScope databaseScope, String type, Class<?> clazz) {
        Map<String, Object> mapping = mappingController.generateMapping(clazz);
        Map<String, Object> mappingResult = Map.of("mappings", mapping);
        elasticSearchController.recreateIndex(mappingResult, type, databaseScope);
    }

}
