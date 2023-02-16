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

package eu.ebrains.kg.indexing.controller.indexing;

import eu.ebrains.kg.common.controller.kg.KG;
import eu.ebrains.kg.common.controller.kg.KGv3;
import eu.ebrains.kg.common.controller.translators.TargetInstancesResult;
import eu.ebrains.kg.common.controller.translators.TranslationController;
import eu.ebrains.kg.common.controller.translators.Translator;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.ErrorReport;
import eu.ebrains.kg.common.model.ErrorReportResult;
import eu.ebrains.kg.common.model.TranslatorModel;
import eu.ebrains.kg.common.model.source.openMINDSv3.SourceInstanceV3;
import eu.ebrains.kg.common.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.indexing.controller.elasticsearch.ElasticSearchController;
import eu.ebrains.kg.indexing.controller.mapping.MappingController;
import eu.ebrains.kg.indexing.controller.metrics.MetricsController;
import eu.ebrains.kg.indexing.controller.settings.SettingsController;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static eu.ebrains.kg.common.controller.translators.Helpers.collectAllTargetInternalReferences;

@Component
public class IndexingController {

    private final MappingController mappingController;
    private final MetricsController metricsController;
    private final SettingsController settingsController;
    private final ElasticSearchController elasticSearchController;
    private final TranslationController translationController;

    private final KGv3 kgV3;


    public IndexingController(MappingController mappingController, MetricsController metricsController, SettingsController settingsController, ElasticSearchController elasticSearchController, TranslationController translationController, KGv3 kgV3) {
        this.mappingController = mappingController;
        this.metricsController = metricsController;
        this.settingsController = settingsController;
        this.elasticSearchController = elasticSearchController;
        this.translationController = translationController;
        this.kgV3 = kgV3;
    }

    public <v3Input extends SourceInstanceV3, Target extends TargetInstance> ErrorReportResult.ErrorReportResultByTargetType populateIndex(TranslatorModel<v3Input, Target> translatorModel, DataStage dataStage, boolean temporary) {
        ErrorReportResult.ErrorReportResultByTargetType errorReportByTargetType =  null;
        Set<String> searchableIds = new HashSet<>();
        Set<String> nonSearchableIds = new HashSet<>();
        if (translatorModel.getV3translator() != null) {
            final UpdateResult updateResultV3 = update(kgV3, translatorModel.getTargetClass(), translatorModel.getV3translator(), translatorModel.getBulkSize(), dataStage, Collections.emptySet(), instance -> instance, translatorModel.isAutoRelease(), temporary);
            if (!updateResultV3.errors.isEmpty()) {
                errorReportByTargetType = new ErrorReportResult.ErrorReportResultByTargetType();
                errorReportByTargetType.setTargetType(translatorModel.getV3translator().getTargetType().getSimpleName());
                errorReportByTargetType.setErrors(updateResultV3.errors);
            }
            searchableIds.addAll(updateResultV3.searchableIds);
            nonSearchableIds.addAll(updateResultV3.nonSearchableIds);
        }
        if (translatorModel.isAutoRelease()) {
            elasticSearchController.removeDeprecatedDocumentsFromAutoReleasedIndex(translatorModel.getTargetClass(), dataStage, nonSearchableIds, temporary);
        } else {
            elasticSearchController.removeDeprecatedDocumentsFromSearchIndex(translatorModel.getTargetClass(), dataStage, searchableIds, temporary);
            elasticSearchController.removeDeprecatedDocumentsFromIdentifiersIndex(translatorModel.getTargetClass(), dataStage, nonSearchableIds);
        }
        return errorReportByTargetType;
    }

    private static class UpdateResult {
        private final Set<String> searchableIds = new HashSet<>();
        private final Set<String> nonSearchableIds = new HashSet<>();
        private final ErrorReport errors = new ErrorReport();
    }

    private <Target extends TargetInstance> void clearNonResolvableReferences(List<Target> instances, DataStage dataStage) {
        List<TargetInternalReference> references = new ArrayList<>();
        instances.forEach(i -> collectAllTargetInternalReferences(i, references));
        final List<String> refs = references.stream().map(TargetInternalReference::getReference).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        final Set<String> existingRefs = elasticSearchController.existingDocuments(refs, dataStage);
        references.forEach(r -> {
            if (r.getReference() != null && !existingRefs.contains(r.getReference())) {
                r.setReference(null);
            }
        });
    }

    private <Target extends TargetInstance> UpdateResult update(KG kg, Class<?> type, Translator<? extends SourceInstanceV3, Target, ?> translator, int bulkSize, DataStage dataStage, Set<String> excludedIds, Function<Target, Target> instanceHandler, boolean autorelease, boolean temporary) {
        UpdateResult updateResult = new UpdateResult();
        final Integer trendThreshold = metricsController.getTrendThreshold(type, dataStage);
        translator.getQueryIds().forEach(queryId -> {
            Integer lastTotal = null;
            boolean hasMore = true;
            int from = 0;
            while (hasMore) {
                TargetInstancesResult<Target> result = translationController.translateToTargetInstances(kg, translator, queryId, dataStage, from, bulkSize, trendThreshold);
                if (result.getErrors() != null) {
                    updateResult.errors.putAll(result.getErrors());
                }
                List<Target> instances = result.getTargetInstances();
                if (instances != null) {
                    List<Target> searchableInstances = new ArrayList<>();
                    List<Target> nonSearchableInstances = new ArrayList<>();
                    final List<Target> processableInstances = instances.stream().filter(instance -> !excludedIds.contains(instance.getId())).collect(Collectors.toList());
                    clearNonResolvableReferences(processableInstances, dataStage);
                    processableInstances.forEach(instance -> {
                        Target handledInstance = instanceHandler != null ? instanceHandler.apply(instance) : instance;
                        if (handledInstance.isSearchableInstance()) {
                            updateResult.searchableIds.add(handledInstance.getId());
                            searchableInstances.add(handledInstance);
                        } else {
                            updateResult.nonSearchableIds.add(handledInstance.getId());
                            nonSearchableInstances.add(handledInstance);
                        }
                    });
                    if (!CollectionUtils.isEmpty(searchableInstances)) {
                        elasticSearchController.updateSearchIndex(searchableInstances, type, dataStage, temporary);
                    }
                    if (!CollectionUtils.isEmpty(nonSearchableInstances)) {
                        if (autorelease) {
                            elasticSearchController.updateAutoReleasedIndex(nonSearchableInstances, dataStage, type, temporary);
                        } else {
                            elasticSearchController.updateIdentifiersIndex(nonSearchableInstances, dataStage);
                        }
                    }
                }
                if (result.getTotal() != null) {
                    lastTotal = result.getTotal();
                }
                from = result.getFrom() + result.getSize();
                hasMore = lastTotal != null && from < lastTotal;
            }
        });
        return updateResult;
    }

    public void recreateIdentifiersIndex(DataStage dataStage) {
        Map<String, Object> mapping = mappingController.generateIdentifierMapping();
        Map<String, Object> payload = Map.of("mappings", mapping);
        elasticSearchController.recreateIdentifiersIndex(payload, dataStage);
    }


    public void reindexTemporaryToReal(DataStage dataStage, Class<? extends TargetInstance> clazz, boolean autorelease) {
        recreateIndex(dataStage, clazz, autorelease, false);
        elasticSearchController.reindexTemporaryToRealIndex(clazz, dataStage, autorelease);
    }

    public void recreateIndex(DataStage dataStage, Class<? extends TargetInstance> clazz, boolean autorelease, boolean temporary) {
        Map<String, Object> mapping = mappingController.generateMapping(clazz, !autorelease);
        if (autorelease) {
            Map<String, Object> payload = Map.of("mappings", mapping);
            elasticSearchController.recreateAutoReleasedIndex(dataStage, payload, clazz, temporary);
        } else {
            Map<String, Object> settings = settingsController.generateSearchIndexSettings();
            Map<String, Object> payload = Map.of(
                    "mappings", mapping,
                    "settings", settings);
            elasticSearchController.recreateSearchIndex(payload, clazz, dataStage, temporary);
        }
    }

    public void addResource(String id, Map<String, Object> resource) {
        elasticSearchController.addResource(id, resource);
    }

    public void deleteResource(String id) {
        elasticSearchController.deleteResource(id);
    }

}
