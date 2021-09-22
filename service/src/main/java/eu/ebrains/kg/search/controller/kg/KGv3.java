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

import java.util.*;


@Component
public class KGv3 implements KG {
    private final KGV3ServiceClient kgServiceClient;

    public KGv3(KGV3ServiceClient kgServiceClient) {
        this.kgServiceClient = kgServiceClient;
    }

    @Override
    public <T> T executeQuery(Class<T> clazz, DataStage dataStage, String queryId, int from, int size) {
        return kgServiceClient.executeQueryForIndexing(clazz, dataStage, queryId, from, size);
    }

    @Override
    public <T> T executeQuery(Class<T> clazz, DataStage dataStage, String queryId, String id) {
        return kgServiceClient.executeQuery(clazz, dataStage, queryId, id);
    }

    public List<String> getTypesOfInstance(String instanceId, DataStage stage) {
        final Map instance = kgServiceClient.getInstance(instanceId, stage);
        if (instance != null) {
            final Object data = instance.get("data");
            if (data instanceof Map) {
                final Object type = ((Map) data).get("@type");
                return type instanceof String ? Collections.singletonList((String) type) : type instanceof List ? (List<String>) type : null;
            }
        }
        return null;
    }


}
