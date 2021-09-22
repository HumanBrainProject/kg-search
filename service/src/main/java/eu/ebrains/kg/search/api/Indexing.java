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

import eu.ebrains.kg.search.controller.indexing.IndexingController;
import eu.ebrains.kg.search.controller.sitemap.SitemapController;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.ErrorReport;
import eu.ebrains.kg.search.model.TranslatorModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<Map<String, Map<String, ErrorReport>>> fullReplacement(@RequestParam("databaseScope") DataStage dataStage) {
        try {
            Map<String, Map<String, ErrorReport>> errorReportByTargetAndSourceType = new HashMap<>();
            indexingController.recreateIdentifiersIndex(dataStage);
            TranslatorModel.MODELS.stream().filter(m -> !m.isAutoRelease()).forEach(m -> {
                indexingController.recreateSearchIndex(dataStage, m.getTargetClass());
                final Map<String, ErrorReport> errorReportMap = indexingController.populateIndex(m, dataStage);
                if(!errorReportMap.isEmpty()){
                    errorReportByTargetAndSourceType.put(m.getTargetClass().getSimpleName(), errorReportMap);
                }
            });
            if(!errorReportByTargetAndSourceType.isEmpty()){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorReportByTargetAndSourceType);
            }
            sitemapController.updateSitemapCache(dataStage);
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("categories/{category}")
    public ResponseEntity<Map<String, ErrorReport>> fullReplacementByType(@RequestParam("databaseScope") DataStage dataStage, @PathVariable("category") String category) {
        try {
            Map<String, ErrorReport> errorReportBySourceType = new HashMap<>();
            TranslatorModel.MODELS.stream().filter(m -> !m.isAutoRelease() && m.getTargetClass().getSimpleName().equals(category)).forEach(m ->{
                indexingController.recreateSearchIndex(dataStage, m.getTargetClass());
                final Map<String, ErrorReport> errorReportMap = indexingController.populateIndex(m, dataStage);
                if(!errorReportMap.isEmpty()){
                    errorReportBySourceType.putAll(errorReportMap);
                }
            } );
            sitemapController.updateSitemapCache(dataStage);
            if(!errorReportBySourceType.isEmpty()){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorReportBySourceType);
            }
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/autorelease")
    public ResponseEntity<Map<String, Map<String, ErrorReport>>> fullReplacementAutoRelease() {
        try {
            Map<String, Map<String, ErrorReport>> errorReportByTargetAndSourceType = new HashMap<>();
            TranslatorModel.MODELS.stream().filter(TranslatorModel::isAutoRelease).forEach(m -> {
                indexingController.recreateAutoReleasedIndex(m.getTargetClass());
                final Map<String, ErrorReport> errorReportMap = indexingController.populateIndex(m, DataStage.RELEASED);
                if(!errorReportMap.isEmpty()){
                    errorReportByTargetAndSourceType.put(m.getTargetClass().getSimpleName(), errorReportMap);
                }
            });
            if(!errorReportByTargetAndSourceType.isEmpty()){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorReportByTargetAndSourceType);
            }
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful autorelease indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PutMapping
    public ResponseEntity<Map<String, Map<String, ErrorReport>>> incrementalUpdate(@RequestParam("databaseScope") DataStage dataStage) {
        try {
            Map<String, Map<String, ErrorReport>> errorReportByTargetAndSourceType = new HashMap<>();
            TranslatorModel.MODELS.stream().filter(m -> !m.isAutoRelease()).forEach(m -> {
                final Map<String, ErrorReport> errorReportMap = indexingController.populateIndex(m, dataStage);
                if(!errorReportMap.isEmpty()){
                    errorReportByTargetAndSourceType.put(m.getTargetClass().getSimpleName(), errorReportMap);
                }
            });
            sitemapController.updateSitemapCache(dataStage);
            if(!errorReportByTargetAndSourceType.isEmpty()){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorReportByTargetAndSourceType);
            }
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful incremental indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PutMapping("categories/{category}")
    public ResponseEntity<Map<String, ErrorReport>> incrementalUpdateByType(@RequestParam("databaseScope") DataStage dataStage, @PathVariable("category") String category) {
        try {
            Map<String, ErrorReport> errorReportBySourceType = new HashMap<>();
            TranslatorModel.MODELS.stream().filter(m -> !m.isAutoRelease() && m.getTargetClass().getSimpleName().equals(category)).forEach(m ->{
                final Map<String, ErrorReport> errorReportMap = indexingController.populateIndex(m, dataStage);
                if(!errorReportMap.isEmpty()){
                    errorReportBySourceType.putAll(errorReportMap);
                }
            } );
            sitemapController.updateSitemapCache(dataStage);
            if(!errorReportBySourceType.isEmpty()){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorReportBySourceType);
            }
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful incremental indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PutMapping("/autorelease")
    public ResponseEntity<Map<String, Map<String, ErrorReport>>> incrementalUpdateAutoRelease() {
        try {
            Map<String, Map<String, ErrorReport>> errorReportByTargetAndSourceType = new HashMap<>();
            TranslatorModel.MODELS.stream().filter(TranslatorModel::isAutoRelease).forEach(m -> {
                final Map<String, ErrorReport> errorReportMap = indexingController.populateIndex(m, DataStage.RELEASED);
                if(!errorReportMap.isEmpty()){
                    errorReportByTargetAndSourceType.put(m.getTargetClass().getSimpleName(), errorReportMap);
                }
            });
            if(!errorReportByTargetAndSourceType.isEmpty()){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorReportByTargetAndSourceType);
            }
            return ResponseEntity.ok().build();
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful incremental autorelease indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }


}
