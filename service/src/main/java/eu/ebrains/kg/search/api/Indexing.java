package eu.ebrains.kg.search.api;

import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.controller.indexing.IndexingController;
import eu.ebrains.kg.search.model.DatabaseScope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RequestMapping("/indexing")
@RestController
public class Indexing {

    private final IndexingController indexingController;

    public Indexing(IndexingController indexingController) {
        this.indexingController = indexingController;
    }

    @PostMapping
    public ResponseEntity<?> fullReplacement(@RequestParam("databaseScope") DatabaseScope databaseScope,
                                             @RequestHeader("X-Legacy-Authorization") String authorization) {
        try {
            Constants.TARGET_MODELS_MAP.forEach((type, clazz) -> indexingController.fullReplacementByType(databaseScope, type, authorization, clazz));
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/{type}")
    public ResponseEntity<?> fullReplacementByType(@RequestParam("databaseScope") DatabaseScope databaseScope,
                                                   @PathVariable("type") String type,
                                                   @RequestHeader("X-Legacy-Authorization") String authorization) {
        Class<?> clazz = Constants.TARGET_MODELS_MAP.get(type);
        if (clazz != null) {
            try {
                indexingController.fullReplacementByType(databaseScope, type, authorization, clazz);
                return ResponseEntity.ok().build();
            } catch (WebClientResponseException e) {
                return ResponseEntity.status(e.getStatusCode()).build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping
    public ResponseEntity<?> incrementalUpdate(@RequestParam("databaseScope") DatabaseScope databaseScope,
                                               @RequestHeader("X-Legacy-Authorization") String authorization) {
        try {
            indexingController.incrementalUpdateAll(databaseScope, authorization);
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PutMapping("/{type}")
    public ResponseEntity<?> incrementalUpdate(@RequestParam("databaseScope") DatabaseScope databaseScope,
                                               @PathVariable("type") String type,
                                               @RequestHeader("X-Legacy-Authorization") String authorization) {
        try {
            indexingController.incrementalUpdateByType(databaseScope, type, authorization);
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

}
