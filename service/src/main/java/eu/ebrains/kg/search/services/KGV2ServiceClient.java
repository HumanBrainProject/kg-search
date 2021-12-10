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

package eu.ebrains.kg.search.services;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.DatabaseScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Component
public class KGV2ServiceClient extends KGServiceClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String kgQueryEndpoint;
    private final String kgCoreEndpoint;


    public KGV2ServiceClient(@Qualifier("asServiceAccount") WebClient serviceAccountWebClient, @Qualifier("asUser") WebClient userWebClient, @Value("${kgquery.endpoint}") String kgQueryEndpoint, @Value("${kgcore.endpoint}") String kgCoreEndpoint) {
        super(serviceAccountWebClient, userWebClient);
        this.kgQueryEndpoint = kgQueryEndpoint;
        this.kgCoreEndpoint = kgCoreEndpoint;
    }

    private static final String vocab = "https://schema.hbp.eu/search/";

    @Cacheable(value = "authEndpoint", unless = "#result == null")
    public String getAuthEndpoint() {
        String url = String.format("%s/users/authorization", kgCoreEndpoint);
        try {
            Map result = serviceAccountWebClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            Map data = (Map) result.get("data");
            return data.get("endpoint").toString();
        } catch (Exception e) {
            logger.info(e.getMessage());
            return null;
        }
    }

    public <T> T executeQuery(String query, DataStage dataStage, Class<T> clazz) {
        DatabaseScope databaseScope = dataStage.equals(DataStage.IN_PROGRESS) ? DatabaseScope.INFERRED: DatabaseScope.RELEASED;
        String url = String.format("%s/query/%s/instances/?databaseScope=%s&vocab=%s", kgQueryEndpoint, query, databaseScope, vocab);
        return executeCallForIndexing(clazz, url);
    }

    public <T> T executeQuery(String query, DataStage dataStage, Class<T> clazz, int from, int size) {
        DatabaseScope databaseScope = dataStage.equals(DataStage.IN_PROGRESS) ? DatabaseScope.INFERRED: DatabaseScope.RELEASED;
        String url = String.format("%s/query/%s/search/instances/?databaseScope=%s&vocab=%s&start=%d&size=%d", kgQueryEndpoint, query, databaseScope, vocab, from, size);
        return executeCallForIndexing(clazz, url);
    }

    public <T> T executeQueryForInstance(String query, String id, DataStage dataStage, Class<T> clazz, boolean asServiceAccount) {
        DatabaseScope databaseScope = dataStage.equals(DataStage.IN_PROGRESS) ? DatabaseScope.INFERRED : DatabaseScope.RELEASED;
        String url = String.format("%s/query/%s/search/instances/%s?databaseScope=%s&vocab=%s", kgQueryEndpoint, query, id, databaseScope, vocab);
        try {
            return executeCallForInstance(clazz, url, asServiceAccount);
        } catch (WebClientResponseException.NotFound e){
            return null;
        }
    }

}
