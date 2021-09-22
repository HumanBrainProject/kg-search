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

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ebrains.kg.search.configuration.OauthClient;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.ErrorReport;
import eu.ebrains.kg.search.model.source.ResultsOfKG;
import eu.ebrains.kg.search.model.source.SourceInstanceV1andV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Component
public class KGV2ServiceClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final WebClient webClient;
    private final String kgQueryEndpoint;
    private final String kgCoreEndpoint;


    public KGV2ServiceClient(@Qualifier("asUser") WebClient userWebClient, @Value("${kgquery.endpoint}") String kgQueryEndpoint, @Value("${kgcore.endpoint}") String kgCoreEndpoint) {
        this.webClient = userWebClient;
        this.kgQueryEndpoint = kgQueryEndpoint;
        this.kgCoreEndpoint = kgCoreEndpoint;
    }

    private static final String vocab = "https://schema.hbp.eu/search/";

    @Cacheable(value = "authEndpoint", unless = "#result == null")
    public String getAuthEndpoint() {
        String url = String.format("%s/users/authorization", kgCoreEndpoint);
        try {
            Map result = webClient.get()
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

    public <T> T executeQueryForIndexing(String query, DataStage dataStage, Class<T> clazz) {
        DatabaseScope databaseScope = dataStage.equals(DataStage.IN_PROGRESS) ? DatabaseScope.INFERRED: DatabaseScope.RELEASED;
        String url = String.format("%s/query/%s/instances/?databaseScope=%s&vocab=%s", kgQueryEndpoint, query, databaseScope, vocab);
        return executeCall(clazz, url);
    }

    public <T> T executeQueryForIndexing(String query, DataStage dataStage, Class<T> clazz, int from, int size) {
        DatabaseScope databaseScope = dataStage.equals(DataStage.IN_PROGRESS) ? DatabaseScope.INFERRED: DatabaseScope.RELEASED;
        String url = String.format("%s/query/%s/search/instances/?databaseScope=%s&vocab=%s&start=%d&size=%d", kgQueryEndpoint, query, databaseScope, vocab, from, size);
        return executeCall(clazz, url);
    }

    public <T> T executeQuery(String query, String id, DataStage dataStage, Class<T> clazz) {
        DatabaseScope databaseScope = dataStage.equals(DataStage.IN_PROGRESS) ? DatabaseScope.INFERRED: DatabaseScope.RELEASED;
        String url = String.format("%s/query/%s/search/instances/%s?databaseScope=%s&vocab=%s", kgQueryEndpoint, query, id, databaseScope, vocab);
        return executeCall(clazz, url);
    }

    private <T> T executeCall(Class<T> clazz, String url) {
        return webClient.get()
                .uri(url)
                .headers(h -> h.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .retrieve()
                .bodyToMono(clazz)
                .doOnSuccess(KGServiceUtils::parsingErrorHandler).doFinally(t -> OauthClient.ERROR_REPORTING_THREAD_LOCAL.remove())
                .block();
    }
}
