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
import eu.ebrains.kg.search.services.KGV3ServiceClient;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class KGv3 {
    private final KGV3ServiceClient kgServiceClient;

    public KGv3(KGV3ServiceClient kgServiceClient) {
        this.kgServiceClient = kgServiceClient;
    }

    public <T> T executeQueryForIndexing(Class<T> clazz, String queryId, DataStage dataStage){
        return kgServiceClient.executeQueryForIndexing(queryId, dataStage, clazz);
    }

    public <T> T executeQueryForLive(Class<T> clazz, String queryId, String id, DataStage dataStage) {
        return  kgServiceClient.executeQueryForLiveMode(queryId, id, dataStage, clazz);
    }

    public Map fetchInstanceForLive(String id, DataStage dataStage) {
        return  kgServiceClient.getInstanceForLiveMode(id, dataStage);
    }

}
