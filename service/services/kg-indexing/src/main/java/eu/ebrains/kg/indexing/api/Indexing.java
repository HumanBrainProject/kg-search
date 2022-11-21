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

package eu.ebrains.kg.indexing.api;

import eu.ebrains.kg.indexing.controller.indexing.IndexingController;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.ErrorReportResult;
import eu.ebrains.kg.common.model.TranslatorModel;
import eu.ebrains.kg.common.services.DOICitationFormatter;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequestMapping("/indexing")
@RestController
public class Indexing {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final IndexingController indexingController;
    private final DOICitationFormatter doiCitationFormatter;

    public Indexing(IndexingController indexingController, DOICitationFormatter doiCitationFormatter) {
        this.indexingController = indexingController;
        this.doiCitationFormatter = doiCitationFormatter;
    }

    @PostMapping("/doiCitations")
    public void refreshDOICitation(@RequestParam("doi") String doi, @RequestParam(value = "style", defaultValue = "apa") String style, @RequestParam(value = "contentType", defaultValue = "text/x-bibliography") String contentType){
        this.doiCitationFormatter.refreshDOICitation(doi, style, contentType);
    }

    @PostMapping("/evictDoiCitations")
    public void evictDoiCitations(){
        this.doiCitationFormatter.evictAll();
    }

    @PostMapping
    @Operation(summary="Full replacement")
    public ResponseEntity<ErrorReportResult> fullReplacement(@RequestParam("databaseScope") DataStage dataStage) {
        try {
            indexingController.recreateIdentifiersIndex(dataStage);
            final List<ErrorReportResult.ErrorReportResultByTargetType> errorsByTarget = TranslatorModel.MODELS.stream().filter(m -> !m.isAutoRelease()).map(m -> {
                //In full replacement mode, we first create a temporary index
                indexingController.recreateIndex(dataStage, m.getTargetClass(), m.isAutoRelease(), true);
                //Which we're then going to populate.
                final List<ErrorReportResult.ErrorReportResultBySourceType> errorsBySource = indexingController.populateIndex(m, dataStage, true);
                //Eventually, we're reindexing the temporary index to the real one
                indexingController.reindexTemporaryToReal(dataStage, m.getTargetClass(), m.isAutoRelease());
                return handleErrorReportResultByTargetType(m, errorsBySource);
            }).filter(Objects::nonNull).collect(Collectors.toList());
            return handleErrorReportResult(errorsByTarget);
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("categories/{category}")
    @Operation(summary="Full replacement by type")
    public ResponseEntity<ErrorReportResult> fullReplacementByType(@RequestParam("databaseScope") DataStage dataStage, @PathVariable("category") String category) {
        try {
            final List<ErrorReportResult.ErrorReportResultByTargetType> errorsByTarget = TranslatorModel.MODELS.stream().filter(m -> m.getTargetClass().getSimpleName().equals(category)).map(m -> {
                //In full replacement mode, we first create a temporary index
                indexingController.recreateIndex(dataStage, m.getTargetClass(), m.isAutoRelease(), true);
                //Which we're then going to populate.
                final List<ErrorReportResult.ErrorReportResultBySourceType> errorsBySource =  indexingController.populateIndex(m, dataStage, true);
                //Eventually, we're reindexing the temporary index to the real one
                indexingController.reindexTemporaryToReal(dataStage, m.getTargetClass(), m.isAutoRelease());
                return handleErrorReportResultByTargetType(m, errorsBySource);
            }).filter(Objects::nonNull).collect(Collectors.toList());
            return handleErrorReportResult(errorsByTarget);
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/autorelease")
    @Operation(summary="full replacement auto release")
    public ResponseEntity<ErrorReportResult> fullReplacementAutoRelease(@RequestParam("databaseScope") DataStage dataStage) {
        try {
            final List<ErrorReportResult.ErrorReportResultByTargetType> errorsByTarget = TranslatorModel.MODELS.stream().filter(TranslatorModel::isAutoRelease).map(m -> {
                indexingController.recreateIndex(dataStage, m.getTargetClass(), m.isAutoRelease(), true);
                List<ErrorReportResult.ErrorReportResultBySourceType> errorsBySource = indexingController.populateIndex(m, dataStage, true);
                indexingController.reindexTemporaryToReal(dataStage, m.getTargetClass(), m.isAutoRelease());
                return handleErrorReportResultByTargetType(m, errorsBySource);
            }).filter(Objects::nonNull).collect(Collectors.toList());
            return handleErrorReportResult(errorsByTarget);
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful autorelease indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PutMapping
    @Operation(summary="incremental update")
    public ResponseEntity<ErrorReportResult> incrementalUpdate(@RequestParam("databaseScope") DataStage dataStage) {
        try {
            final List<ErrorReportResult.ErrorReportResultByTargetType> errorsByTarget = TranslatorModel.MODELS.stream().filter(m -> !m.isAutoRelease()).map(m -> {
                final List<ErrorReportResult.ErrorReportResultBySourceType> errorsBySource = indexingController.populateIndex(m, dataStage, false);
                return handleErrorReportResultByTargetType(m, errorsBySource);
            }).filter(Objects::nonNull).collect(Collectors.toList());
            return handleErrorReportResult(errorsByTarget);
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful incremental indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PutMapping("categories/{category}")
    @Operation(summary="incremental update by type")
    public ResponseEntity<ErrorReportResult> incrementalUpdateByType(@RequestParam("databaseScope") DataStage dataStage, @PathVariable("category") String category) {
        try {
            final List<ErrorReportResult.ErrorReportResultByTargetType> errorsByTarget = TranslatorModel.MODELS.stream().filter(m -> !m.isAutoRelease() && m.getTargetClass().getSimpleName().equals(category)).map(m -> {
                final List<ErrorReportResult.ErrorReportResultBySourceType> errorsBySource =  indexingController.populateIndex(m, dataStage, false);
                return handleErrorReportResultByTargetType(m, errorsBySource);
            }).filter(Objects::nonNull).collect(Collectors.toList());
            return handleErrorReportResult(errorsByTarget);
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful incremental indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PutMapping("/autorelease")
    @Operation(summary="incremental auto release")
    public ResponseEntity<ErrorReportResult> incrementalUpdateAutoRelease(@RequestParam("databaseScope") DataStage dataStage) {
        try {
            final List<ErrorReportResult.ErrorReportResultByTargetType> errorsByTarget = TranslatorModel.MODELS.stream().filter(TranslatorModel::isAutoRelease).map(m -> {
                List<ErrorReportResult.ErrorReportResultBySourceType> errorsBySource = indexingController.populateIndex(m, dataStage, false);
                return  handleErrorReportResultByTargetType(m, errorsBySource);
            }).filter(Objects::nonNull).collect(Collectors.toList());
            return handleErrorReportResult(errorsByTarget);
        } catch (WebClientResponseException e) {
            logger.info("Unsuccessful incremental autorelease indexing", e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PutMapping(value = "/resources/{id}")
    @Operation(description="Add/update the JSON resource with the given id")
    public void addResource(@PathVariable("id") String id, @RequestBody Map<String, Object> payload) {
        indexingController.addResource(id, payload);
    }

    @DeleteMapping(value = "/resources/{id}")
    @Operation(description="Remove the JSON resource with the given id")
    public void deleteResource(@PathVariable("id") String id) {
        indexingController.deleteResource(id);
    }



    private ErrorReportResult.ErrorReportResultByTargetType handleErrorReportResultByTargetType(TranslatorModel<?, ?, ?, ?> m, List<ErrorReportResult.ErrorReportResultBySourceType> errorsBySource) {
        if (!errorsBySource.isEmpty()) {
            ErrorReportResult.ErrorReportResultByTargetType e = new ErrorReportResult.ErrorReportResultByTargetType();
            e.setTargetType(m.getTargetClass().getSimpleName());
            e.setErrorsBySource(errorsBySource);
            return e;
        }
        return null;
    }

    private ResponseEntity<ErrorReportResult> handleErrorReportResult(List<ErrorReportResult.ErrorReportResultByTargetType> errorsByTarget) {
        if (!errorsByTarget.isEmpty()) {
            final ErrorReportResult result = new ErrorReportResult();
            result.setErrorsByTarget(errorsByTarget);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
        return ResponseEntity.ok().build();
    }


}
