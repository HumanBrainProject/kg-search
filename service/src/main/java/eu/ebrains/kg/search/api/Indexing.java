package eu.ebrains.kg.search.api;

import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.controller.indexing.IndexingController;
import eu.ebrains.kg.search.controller.sitemap.SitemapController;
import eu.ebrains.kg.search.model.DataStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RequestMapping("/indexing")
@RestController
public class Indexing {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final IndexingController indexingController;
    private final SitemapController sitemapController;

    public Indexing(IndexingController indexingController, SitemapController sitemapController) {
        this.indexingController = indexingController;
        this.sitemapController = sitemapController;
    }

    @PostMapping
    public ResponseEntity<?> fullReplacement(@RequestParam("databaseScope") DataStage dataStage,
                                             @RequestHeader("Authorization") String authorization,
                                             @RequestHeader("X-Legacy-Authorization") String legacyAuthorization
                                             ) {
        try {
            Constants.TARGET_MODELS_MAP.forEach((type, clazz) -> indexingController.fullReplacementByType(dataStage, type, authorization, legacyAuthorization, clazz));
            sitemapController.updateSitemapCache(dataStage);
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/{type}")
    public ResponseEntity<?> fullReplacementByType(@RequestParam("databaseScope") DataStage dataStage,
                                                   @PathVariable("type") String type,
                                                   @RequestHeader("Authorization") String authorization,
                                                   @RequestHeader("X-Legacy-Authorization") String legacyAuthorization) {
        Class<?> clazz = Constants.TARGET_MODELS_MAP.get(type);
        if (clazz != null) {
            try {
                indexingController.fullReplacementByType(dataStage, type, authorization, legacyAuthorization, clazz);
                sitemapController.updateSitemapCache(dataStage);
                return ResponseEntity.ok().build();
            } catch (WebClientResponseException e) {
                logger.info("Unsuccessful indexing", e);
                return ResponseEntity.status(e.getStatusCode()).build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping
    public ResponseEntity<?> incrementalUpdate(@RequestParam("databaseScope") DataStage dataStage,
                                               @RequestHeader("Authorization") String authorization,
                                               @RequestHeader("X-Legacy-Authorization") String legacyAuthorization) {
        try {
            indexingController.incrementalUpdateAll(dataStage, authorization, legacyAuthorization);
            sitemapController.updateSitemapCache(dataStage);
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {

            logger.info("Unsuccessful incremental indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PutMapping("/{type}")
    public ResponseEntity<?> incrementalUpdate(@RequestParam("databaseScope") DataStage dataStage,
                                               @PathVariable("type") String type,
                                               @RequestHeader("Authorization") String authorization,
                                               @RequestHeader("X-Legacy-Authorization") String legacyAuthorization) {
        try {
            indexingController.incrementalUpdateByType(dataStage, type, authorization, legacyAuthorization);
            sitemapController.updateSitemapCache(dataStage);
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful incremental indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

}
