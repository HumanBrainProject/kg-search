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

package eu.ebrains.kg.search.api;

import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.controller.indexing.IndexingController;
import eu.ebrains.kg.search.controller.sitemap.SitemapController;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.utils.ESHelper;
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
                                             @RequestHeader("X-Legacy-Authorization") String legacyAuthorization
                                             ) {
        try {
            logger.info(String.format("Creating index %s", ESHelper.getIdentifierIndex(dataStage)));
            indexingController.recreateIdentifiersIndex(dataStage);
            logger.info(String.format("Successfully created index %s", ESHelper.getIdentifierIndex(dataStage)));
            Constants.TARGET_MODELS_ORDER.forEach(clazz -> indexingController.fullReplacementByType(clazz, dataStage, legacyAuthorization));
            sitemapController.updateSitemapCache(dataStage);
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/autorelease")
    public ResponseEntity<?> fullReplacementAutoRelease() {
        try {
            Constants.AUTO_RELEASED_MODELS_ORDER.forEach(indexingController::fullReplacementAutoReleasedByType);
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful autorelease indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PutMapping
    public ResponseEntity<?> incrementalUpdate(@RequestParam("databaseScope") DataStage dataStage,
                                               @RequestHeader("X-Legacy-Authorization") String legacyAuthorization) {
        try {
            indexingController.incrementalUpdateAll(dataStage, legacyAuthorization);
            sitemapController.updateSitemapCache(dataStage);
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful incremental indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PutMapping("/autorelease")
    public ResponseEntity<?> incrementalUpdateAutoRelease() {
        try {
            indexingController.incrementalUpdateAutoRelease();
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful incremental autorelease indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }


}
