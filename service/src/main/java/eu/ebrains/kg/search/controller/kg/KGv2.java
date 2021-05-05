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

package eu.ebrains.kg.search.controller.kg;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv2;
import eu.ebrains.kg.search.model.source.SourceInstanceIdentifierV1andV2;
import eu.ebrains.kg.search.services.KGServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KGv2 {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final KGServiceClient kgServiceClient;

    private final int PAGE_SIZE = 20;

    public KGv2(KGServiceClient kgServiceClient) {
        this.kgServiceClient = kgServiceClient;
    }

    public <T> T executeQueryForIndexing(Class<T> clazz, DataStage dataStage, String query, String authorization){
        return kgServiceClient.executeQueryForIndexing(query, dataStage, clazz, authorization);
    }

    private static class ResultsOfKGV2Source extends ResultsOfKGv2<SourceInstanceIdentifierV1andV2> {}

    public List<SourceInstanceIdentifierV1andV2> executeQueryForIndexing(DataStage dataStage, String query, String authorization){
        List<SourceInstanceIdentifierV1andV2> result = new ArrayList<>();
        boolean findMore = true;
        int from = 0;
        while (findMore) {
            logger.debug(String.format("Starting to query %d instances from %d for v1", PAGE_SIZE, from));
            ResultsOfKGV2Source page = kgServiceClient.executeQueryForIndexing(query, dataStage, ResultsOfKGV2Source.class, authorization, from, PAGE_SIZE);
            logger.debug(String.format("Successfully queried %d instances from %d of a total of %d for v1", page.getResults().size(), from, page.getTotal()));
            result.addAll(page.getResults());
            from = page.getStart() + page.getSize();
            findMore = from < page.getTotal();
        }
        return result;
    }

    public <T> T executeQuery(Class<T> clazz, DataStage dataStage, String query, String id, String authorization) {
        return  kgServiceClient.executeQuery(query, id, dataStage, clazz, authorization);
    }

    public <T> T executeQueryByIdentifier(Class<T> clazz, DataStage dataStage, String query, String identifier, String authorization) {
        return  kgServiceClient.executeQueryByIdentifier(query, identifier, dataStage, clazz, authorization);
    }

}
