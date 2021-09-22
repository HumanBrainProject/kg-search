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

package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.configuration.OauthClient;
import eu.ebrains.kg.search.controller.kg.KG;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.*;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.Helpers.getStats;

@Component
public class TranslationController {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    public <Source, Target> TargetInstancesResult<Target> translateToTargetInstances(KG kg, Translator<Source, Target, ? extends ResultsOfKG<Source>> translator, String queryId, DataStage dataStage, int from, int size) {
        logger.info(String.format("Starting to query %d %s from %d", size, translator.getSourceType().getSimpleName(), from));
        final ResultsOfKG<Source> instanceResults = kg.executeQuery(translator.getResultType(), dataStage, queryId, from, size);
        Stats stats = getStats(instanceResults, from);
        logger.info(String.format("Queried %d %s (%s)", stats.getPageSize(), translator.getSourceType().getSimpleName(), stats.getInfo()));
        List<Target> instances = instanceResults.getData().stream().map(s -> translator.translate(s, dataStage, false)).filter(Objects::nonNull).collect(Collectors.toList());
        TargetInstancesResult<Target> result = new TargetInstancesResult<>();
        result.setTargetInstances(instances);
        result.setFrom(instanceResults.getFrom());
        result.setSize(instanceResults.getSize());
        result.setTotal(instanceResults.getTotal());
        if(instanceResults.getErrors()!=null){
            result.setErrors(instanceResults.getErrors());
        }
        return result;
    }

    public <Source, Target extends TargetInstance> Target translateToTargetInstanceForLiveMode(KG kg, Translator<Source, Target, ? extends ResultsOfKG<Source>> translator, String queryId, DataStage dataStage, String id, boolean useSourceType) {
        logger.info(String.format("Starting to query id %s from %s for live mode", id, translator.getSourceType().getSimpleName()));
        Source source;
        if(useSourceType) {
           source = kg.executeQuery(translator.getSourceType(), dataStage, queryId, id);
        }
        else{
            final ResultsOfKG<Source> resultsOfKG = kg.executeQuery(translator.getResultType(), dataStage, queryId, id);
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
        return source != null ? translator.translate(source, dataStage, true) : null;
    }


}
