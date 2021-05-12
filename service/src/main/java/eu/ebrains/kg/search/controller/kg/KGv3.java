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
import eu.ebrains.kg.search.model.source.ResultsOfKGv3;
import eu.ebrains.kg.search.model.source.openMINDSv3.SourceInstanceV3;
import eu.ebrains.kg.search.services.KGV3ServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
public class KGv3 {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final KGV3ServiceClient kgServiceClient;

    private final int PAGE_SIZE = 20;

    public KGv3(KGV3ServiceClient kgServiceClient) {
        this.kgServiceClient = kgServiceClient;
    }

    public <T> T executeQueryForIndexing(Class<T> clazz, DataStage dataStage, String queryId, int from, int size){
        return kgServiceClient.executeQueryForIndexing(clazz, dataStage, queryId, from, size);
    }

    public <T> T executeQueryForIndexing(Class<T> clazz, DataStage dataStage, String queryId, int from, int size, Map<String, String> params){
        return kgServiceClient.executeQueryForIndexing(clazz, dataStage, queryId, from, size, params);
    }

    private static class ResultsOfKGV3Source extends ResultsOfKGv3<SourceInstanceV3> {}

    public List<SourceInstanceV3> executeQueryForIndexing(DataStage dataStage, String queryId){
        List<SourceInstanceV3> result = new ArrayList<>();
        boolean findMore = true;
        int from = 0;
        while (findMore) {
            logger.debug(String.format("Starting to query %d instances from %d for v3", PAGE_SIZE, from));
            ResultsOfKGV3Source page = kgServiceClient.executeQueryForIndexing(ResultsOfKGV3Source.class, dataStage, queryId, from, PAGE_SIZE);
            logger.debug(String.format("Successfully queried %d instances from %d of a total of %d for v3", page.getData().size(), from, page.getTotal()));
            result.addAll(page.getData());
            from = page.getFrom() + page.getSize();
            findMore = from < page.getTotal();
        }
        return result;
    }

    public <T> T executeQueryForIndexing(Class<T> clazz, DataStage dataStage, String queryId, String id) {
        return  kgServiceClient.executeQueryForIndexing(clazz, dataStage, queryId, id);
    }

    public <T> T executeQuery(Class<T> clazz, DataStage dataStage, String queryId, String id) {
        return  kgServiceClient.executeQuery(clazz, dataStage, queryId, id);
    }

    public Map fetchInstance(String id, DataStage dataStage) {
        return  kgServiceClient.getInstance(id, dataStage);
    }

}
