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
import eu.ebrains.kg.search.services.KGV2ServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KGv2 implements KG {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final KGV2ServiceClient kgV2ServiceClient;

    private final int PAGE_SIZE = 20;

    public KGv2(KGV2ServiceClient KGV2ServiceClient) {
        this.kgV2ServiceClient = KGV2ServiceClient;
    }

    @Override
    public <T> T executeQuery(Class<T> clazz, DataStage dataStage, String queryId, int from, int size){
        return kgV2ServiceClient.executeQuery(queryId, dataStage, clazz, from, size);
    }

    private static class ResultsOfKGV2Source extends ResultsOfKGv2<SourceInstanceIdentifierV1andV2> {}

    @Override
    public <T> T executeQueryForInstance(Class<T> clazz, DataStage dataStage, String query, String id, boolean asServiceAccount) {
        return  kgV2ServiceClient.executeQueryForInstance(query, id, dataStage, clazz, asServiceAccount);
    }

}
