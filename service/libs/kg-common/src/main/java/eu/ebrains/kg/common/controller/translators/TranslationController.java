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

package eu.ebrains.kg.common.controller.translators;

import eu.ebrains.kg.common.configuration.Configuration;
import eu.ebrains.kg.common.controller.kg.KG;
import eu.ebrains.kg.common.controller.kg.KGv3;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.ErrorReport;
import eu.ebrains.kg.common.model.TranslatorModel;
import eu.ebrains.kg.common.model.source.ResultsOfKG;
import eu.ebrains.kg.common.model.source.openMINDSv3.SourceInstanceV3;
import eu.ebrains.kg.common.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.services.DOICitationFormatter;
import eu.ebrains.kg.common.services.ESServiceClient;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.stream.Collectors;

import static eu.ebrains.kg.common.controller.translators.Helpers.*;

@Component
public class TranslationController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final KGv3 kgV3;
    private final DOICitationFormatter doiCitationFormatter;
    private final ESServiceClient esServiceClient;

    private final Configuration configuration;

    public TranslationController(KGv3 kgV3, DOICitationFormatter doiCitationFormatter, ESServiceClient esServiceClient, Configuration configuration) {
        this.doiCitationFormatter = doiCitationFormatter;
        this.kgV3 = kgV3;
        this.configuration = configuration;
        this.esServiceClient = esServiceClient;
    }

    public <Source, Target> TargetInstancesResult<Target> translateToTargetInstances(KG kg, Translator<Source, Target, ? extends ResultsOfKG<Source>> translator, String queryId, DataStage dataStage, int from, int size, Integer trendingThreshold) {
        logger.info(String.format("Starting to query %d %s from %d", size, translator.getSourceType().getSimpleName(), from));
        final ResultsOfKG<Source> instanceResults = kg.executeQuery(translator.getResultType(), dataStage, queryId, from, size);
        TargetInstancesResult<Target> result = new TargetInstancesResult<>();
        if (instanceResults == null) {
            logger.info("Was not able to read results for {} from index {} of size {}", translator.getSourceType().getSimpleName(), from, size);
            result.setTargetInstances(Collections.emptyList());
            result.setFrom(from);
            result.setSize(size);
        } else {
            Stats stats = getStats(instanceResults, from);
            translator.setConfiguration(configuration);
            logger.info(String.format("Queried %d %s (%s)", stats.getPageSize(), translator.getSourceType().getSimpleName(), stats.getInfo()));
            instanceResults.setErrors(new ErrorReport());
            List<Target> instances = instanceResults.getData().stream().filter(Objects::nonNull).map(s -> {
                        try {
                            return translator.translate(s, dataStage, false, new TranslatorUtils(doiCitationFormatter, esServiceClient, trendingThreshold));
                        } catch (TranslationException e) {
                            if (instanceResults.getErrors().get(e.getIdentifier()) != null) {
                                instanceResults.getErrors().get(e.getIdentifier()).add(e.getMessage());
                            } else {
                                List<String> errors = new ArrayList<>();
                                errors.add(e.getMessage());
                                instanceResults.getErrors().put(e.getIdentifier(), errors);
                            }
                            return null;
                        } catch (Exception e) {
                            String id = s instanceof SourceInstanceV3 ? ((SourceInstanceV3) s).getId() : "unknown";
                            instanceResults.getErrors().put(id, Collections.singletonList(String.format("Unexpected exception: %s", e.getMessage())));
                            logger.error(String.format("Unexpected exception for instance %s in translation", id), e);
                            return null;
                        }
                    }
            ).filter(Objects::nonNull).collect(Collectors.toList());
            result.setTargetInstances(instances);
            result.setFrom(instanceResults.getFrom());
            result.setSize(instanceResults.getSize());
            result.setTotal(instanceResults.getTotal());
            if (instanceResults.getErrors() != null && !instanceResults.getErrors().isEmpty()) {
                result.setErrors(instanceResults.getErrors());
            }
        }
        return result;
    }


    public <Source, Target extends TargetInstance> Target translateToTargetInstanceForLiveMode(KG kg, Translator<Source, Target, ? extends ResultsOfKG<Source>> translator, String queryId, DataStage dataStage, String id, boolean useSourceType, boolean checkReferences) throws TranslationException {
        logger.info(String.format("Starting to query id %s from %s for live mode", id, translator.getSourceType().getSimpleName()));
        Source source;
        if (useSourceType) {
            source = kg.executeQueryForInstance(translator.getSourceType(), dataStage, queryId, id, false);
        } else {
            final ResultsOfKG<Source> resultsOfKG = kg.executeQueryForInstance(translator.getResultType(), dataStage, queryId, id, false);
            if (resultsOfKG.getData() != null) {
                if (resultsOfKG.getData().isEmpty()) {
                    return null;
                } else if (resultsOfKG.getData().size() == 1) {
                    source = resultsOfKG.getData().get(0);
                } else {
                    throw new RuntimeException(String.format("Too many (%d) results when querying query id %s for id %s of type %s", resultsOfKG.getData().size(), queryId, id, translator.getSourceType().getSimpleName()));
                }
            } else {
                throw new RuntimeException(String.format("Unexpected response when querying query id %s for id %s of type %s", queryId, id, translator.getSourceType().getSimpleName()));
            }
        }
        logger.info(String.format("Done querying id %s from %s for live mode", id, translator.getSourceType().getSimpleName()));
        if (source == null) {
            return null;
        }
        translator.setConfiguration(configuration);
        final Target translateResult = translator.translate(source, dataStage, true, new TranslatorUtils(doiCitationFormatter, esServiceClient, null));
        if (checkReferences) {
            checkReferences(dataStage, useSourceType, translateResult);
        }
        return translateResult;
    }

    private void checkReferences(DataStage dataStage, boolean useSourceType, Object result) {
        //To allow the live preview to hide references to non-existent instances, we need to query them too.
        final List<TargetInternalReference> references = new ArrayList<>();
        Helpers.collectAllTargetInternalReferences(result, references);
        Map<String, Boolean> cachedReferences = new HashMap<>();
        references.forEach(t -> {
            boolean reset = false;
            if (t.getReference() != null) {
                final Boolean fromCache = cachedReferences.get(t.getReference());
                if (fromCache != null) {
                    if (fromCache) {
                        reset = true;
                    }
                } else {
                    TargetInstance reference = null;
                    try {
                        final List<String> typesOfReference = kgV3.getTypesOfInstance(t.getReference(), DataStage.IN_PROGRESS, false);
                        if (typesOfReference != null) {
                            final TranslatorModel<?, ?> referenceTranslatorModel = TranslatorModel.MODELS.stream().filter(m -> m.getV3translator() != null && m.getV3translator().semanticTypes().stream().anyMatch(typesOfReference::contains)).findFirst().orElse(null);
                            if (referenceTranslatorModel != null) {
                                final String referenceQueryId = typesOfReference.stream().map(type -> referenceTranslatorModel.getV3translator().getQueryIdByType(type)).findFirst().orElse(null);
                                reference = translateTargetInstance(dataStage, useSourceType, t, reference, referenceTranslatorModel, referenceQueryId);
                            }
                        }
                        reset = reference == null;
                        cachedReferences.put(t.getReference(), reset);
                    } catch (WebClientResponseException ignored) {
                        logger.error("A web client exception occurred - ignoring");
                    }
                }
            }
            if (reset) {
                t.setReference(null);
            }
        });
    }

    private TargetInstance translateTargetInstance(DataStage dataStage, boolean useSourceType, TargetInternalReference t, TargetInstance reference, TranslatorModel<?, ?> referenceTranslatorModel, String referenceQueryId) {
        try {
            reference = translateToTargetInstanceForLiveMode(kgV3, referenceTranslatorModel.getV3translator(), referenceQueryId, dataStage, t.getReference(), useSourceType, false);
        } catch (TranslationException ignored) {
            logger.error("A translation exception occurred - ignoring");
        }
        return reference;
    }


}
