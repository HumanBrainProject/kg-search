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

package eu.ebrains.kg.search.controller.indexing;

import eu.ebrains.kg.search.controller.elasticsearch.ElasticSearchController;
import eu.ebrains.kg.search.controller.kg.KG;
import eu.ebrains.kg.search.controller.kg.KGv2;
import eu.ebrains.kg.search.controller.kg.KGv3;
import eu.ebrains.kg.search.controller.mapping.MappingController;
import eu.ebrains.kg.search.controller.translators.TargetInstancesResult;
import eu.ebrains.kg.search.controller.translators.TranslationController;
import eu.ebrains.kg.search.controller.translators.Translator;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.ErrorReport;
import eu.ebrains.kg.search.model.ErrorReportResult;
import eu.ebrains.kg.search.model.TranslatorModel;
import eu.ebrains.kg.search.model.source.ResultsOfKG;
import eu.ebrains.kg.search.model.source.ResultsOfKGv2;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.TranslationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;

@Component
public class IndexingController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final MappingController mappingController;
    private final ElasticSearchController elasticSearchController;
    private final TranslationController translationController;
    private final DOICitationFormatter doiCitationFormatter;

    private final KGv2 kgV2;
    private final KGv3 kgV3;

    public IndexingController(MappingController mappingController, ElasticSearchController elasticSearchController, TranslationController translationController, KGv2 kgV2, KGv3 kgV3, DOICitationFormatter doiCitationFormatter) {
        this.mappingController = mappingController;
        this.elasticSearchController = elasticSearchController;
        this.translationController = translationController;
        this.kgV2 = kgV2;
        this.kgV3 = kgV3;
        this.doiCitationFormatter = doiCitationFormatter;
    }

    private <Source, Target extends TargetInstance> Target getRelatedInstance(KG kg, Translator<Source, Target, ? extends ResultsOfKG<Source>> translator, Target instance, DataStage dataStage){
        return instance.getIdentifier().stream().filter(id -> translator.getQueryIds().stream().anyMatch(id::contains))
                .map(id -> {
                    final String queryId = translator.getQueryIds().stream().filter(id::contains).findFirst().orElse(null);
                    if (queryId != null) {
                        final Source source = kg.executeQueryForInstance(translator.getSourceType(), dataStage, String.format("%s/search", queryId), id, true);
                        if(source!=null){
                            try {
                                return translator.translate(source, dataStage, false, doiCitationFormatter);
                            } catch (TranslationException e) {
                                //We don't take this error any further since only the "old" world is affected.
                                logger.error(e.getMessage());
                                return null;
                            }
                        }
                        return null;
                    }
                    return null;
                }).filter(Objects::nonNull).findFirst().orElse(null);
    }


    public <v1Input, v2Input, v3Input, Target extends TargetInstance> List<ErrorReportResult.ErrorReportResultBySourceType> populateIndex(TranslatorModel<v1Input, v2Input, v3Input, Target> translatorModel, DataStage dataStage) {
        List<ErrorReportResult.ErrorReportResultBySourceType> errorReportBySourceType = new ArrayList<>();
        Set<String> handledIdentifiers = new HashSet<>();
        Set<String> searchableIds = new HashSet<>();
        Set<String> nonSearchableIds = new HashSet<>();
        if(translatorModel.getV3translator()!=null) {
            final UpdateResult updateResultV3 = update(kgV3, translatorModel.getTargetClass(), translatorModel.getV3translator(), translatorModel.getBulkSize(), dataStage, Collections.<String>emptySet(), instance -> {
                if (translatorModel.getMerger() != null) {
                    final Translator<v2Input, Target, ? extends ResultsOfKGv2<v2Input>> v2translator = translatorModel.getV2translator();
                    final Target fromV2 = v2translator != null ? getRelatedInstance(kgV2, v2translator, instance, dataStage) : null;
                    final Translator<v1Input, Target, ? extends ResultsOfKGv2<v1Input>> v1translator = translatorModel.getV1translator();
                    final Target fromV1 = v1translator != null ? getRelatedInstance(kgV2, v1translator, instance, dataStage) : null;
                    return translatorModel.getMerger().merge(fromV1, fromV2, instance);
                } else {
                    return instance;
                }
            }, translatorModel.isAutoRelease());
            if (!updateResultV3.errors.isEmpty()) {
                ErrorReportResult.ErrorReportResultBySourceType e = new ErrorReportResult.ErrorReportResultBySourceType();
                e.setSourceType(translatorModel.getV3translator().getSourceType().getSimpleName());
                e.setErrors(updateResultV3.errors);
                errorReportBySourceType.add(e);
            }
            handledIdentifiers.addAll(updateResultV3.handledIdentifiers);
            searchableIds.addAll(updateResultV3.searchableIds);
            nonSearchableIds.addAll(updateResultV3.nonSearchableIds);
        }
        boolean indexDataFromOldKG = dataStage == DataStage.RELEASED || !translatorModel.isOnlyV3ForInProgress();
        if (indexDataFromOldKG && translatorModel.getV2translator() != null) {
            final UpdateResult updateResultV2 = update(kgV2, translatorModel.getTargetClass(), translatorModel.getV2translator(), translatorModel.getBulkSize(), dataStage, handledIdentifiers, instance -> {
                if(translatorModel.getMerger()!=null){
                    final Translator<v1Input, Target, ? extends ResultsOfKGv2<v1Input>> v1translator = translatorModel.getV1translator();
                    final Target fromV1 = v1translator != null ? getRelatedInstance(kgV2, v1translator, instance, dataStage) : null;
                    return translatorModel.getMerger().merge(fromV1, instance, null);
                }
                else {
                    return instance;
                }
            }, translatorModel.isAutoRelease());
            if(!updateResultV2.errors.isEmpty()) {
                ErrorReportResult.ErrorReportResultBySourceType e = new ErrorReportResult.ErrorReportResultBySourceType();
                e.setSourceType(translatorModel.getV2translator().getSourceType().getSimpleName());
                e.setErrors(updateResultV2.errors);
                errorReportBySourceType.add(e);
            }
            handledIdentifiers.addAll(updateResultV2.handledIdentifiers);
            searchableIds.addAll(updateResultV2.searchableIds);
            nonSearchableIds.addAll(updateResultV2.nonSearchableIds);
        }
        if (indexDataFromOldKG && translatorModel.getV1translator() != null) {
            final UpdateResult updateResultV1 = update(kgV2, translatorModel.getTargetClass(), translatorModel.getV1translator(), translatorModel.getBulkSize(), dataStage, handledIdentifiers, null, translatorModel.isAutoRelease());
            handledIdentifiers.addAll(updateResultV1.handledIdentifiers);
            searchableIds.addAll(updateResultV1.searchableIds);
            nonSearchableIds.addAll(updateResultV1.nonSearchableIds);
            if(!updateResultV1.errors.isEmpty()) {
                ErrorReportResult.ErrorReportResultBySourceType e = new ErrorReportResult.ErrorReportResultBySourceType();
                e.setSourceType(translatorModel.getV1translator().getSourceType().getSimpleName());
                e.setErrors(updateResultV1.errors);
                errorReportBySourceType.add(e);
            }
        }
        if(translatorModel.isAutoRelease()){
            elasticSearchController.removeDeprecatedDocumentsFromAutoReleasedIndex(translatorModel.getTargetClass(), nonSearchableIds);
        }
        else {
            elasticSearchController.removeDeprecatedDocumentsFromSearchIndex(translatorModel.getTargetClass(), dataStage, searchableIds);
            elasticSearchController.removeDeprecatedDocumentsFromIdentifiersIndex(translatorModel.getTargetClass(), dataStage, nonSearchableIds);
        }
        return errorReportBySourceType;
    }

    private static class UpdateResult {
        private final Set<String> handledIdentifiers = new HashSet<>();
        private final Set<String> searchableIds = new HashSet<>();
        private final Set<String> nonSearchableIds = new HashSet<>();
        private final ErrorReport errors = new ErrorReport();

    }

    private <Target extends TargetInstance> UpdateResult update(KG kg, Class<?> type, Translator<?, Target, ?> translator, int bulkSize, DataStage dataStage, Set<String> excludedIds, Function<Target, Target> instanceHandler, boolean autorelease) {
        UpdateResult updateResult = new UpdateResult();
        translator.getQueryIds().forEach(queryId -> {
            boolean hasMore = true;
            int from = 0;
            while (hasMore) {
                TargetInstancesResult<Target> result = translationController.translateToTargetInstances(kg, translator, queryId, dataStage, from, bulkSize);
                if(result.getErrors()!=null){
                    updateResult.errors.putAll(result.getErrors());
                }
                List<Target> instances = result.getTargetInstances();
                if (instances != null) {
                    List<Target> searchableInstances = new ArrayList<>();
                    List<Target> nonSearchableInstances = new ArrayList<>();
                    instances.stream().filter(instance -> !excludedIds.contains(instance.getId())).forEach(instance -> {
                        Target handledInstance = instanceHandler!=null ? instanceHandler.apply(instance) : instance;
                        updateResult.handledIdentifiers.add(handledInstance.getId());
                        updateResult.handledIdentifiers.addAll(handledInstance.getIdentifier());
                        if (handledInstance.isSearchableInstance()) {
                            updateResult.searchableIds.add(handledInstance.getId());
                            searchableInstances.add(handledInstance);
                        } else {
                            updateResult.nonSearchableIds.add(handledInstance.getId());
                            nonSearchableInstances.add(handledInstance);
                        }
                    });
                    if (!CollectionUtils.isEmpty(searchableInstances)) {
                        elasticSearchController.updateSearchIndex(searchableInstances, type, dataStage);
                    }
                    if (!CollectionUtils.isEmpty(nonSearchableInstances)) {
                        if(autorelease){
                            elasticSearchController.updateAutoReleasedIndex(nonSearchableInstances, type);
                        }
                        else {
                            elasticSearchController.updateIdentifiersIndex(nonSearchableInstances, dataStage);
                        }
                    }
                }
                from = result.getFrom() + result.getSize();
                hasMore = from < result.getTotal();
            }
        });
        return updateResult;
    }


    public void recreateIdentifiersIndex(DataStage dataStage) {
        Map<String, Object> mapping = mappingController.generateIdentifierMapping();
        Map<String, Object> mappingResult = Map.of("mappings", mapping);
        elasticSearchController.recreateIdentifiersIndex(mappingResult, dataStage);
    }

    public void recreateSearchIndex(DataStage dataStage, Class<? extends TargetInstance> clazz) {
        Map<String, Object> mapping = mappingController.generateMapping(clazz);
        Map<String, Object> mappingResult = Map.of("mappings", mapping);
        elasticSearchController.recreateSearchIndex(mappingResult, clazz, dataStage);
    }

    public void recreateAutoReleasedIndex(Class<?> clazz) {
        Map<String, Object> mapping = mappingController.generateMapping(clazz);
        Map<String, Object> mappingResult = Map.of("mappings", mapping);
        elasticSearchController.recreateAutoReleasedIndex(mappingResult, clazz);
    }
}