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

package eu.ebrains.kg.common.services;

import eu.ebrains.kg.common.configuration.GracefulDeserializationProblemHandler;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.utils.MetaModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class KGV3ServiceClient  {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int MAX_RETRIES = 5;
    private final String kgCoreEndpoint;
    private final WebClient serviceAccountWebClient;
    private final WebClient userWebClient;


    public KGV3ServiceClient(@Qualifier("asServiceAccount") WebClient serviceAccountWebClient, @Qualifier("asUser") WebClient userWebClient, @Value("${kgcore.endpoint}") String kgCoreEndpoint) {
        this.kgCoreEndpoint = kgCoreEndpoint;
        this.serviceAccountWebClient = serviceAccountWebClient;
        this.userWebClient = userWebClient;
    }

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
            return null;
        }
    }

    public Set<UUID> getInvitationsFromKG(){
        String url = String.format("%s/users/me/roles", kgCoreEndpoint);
        final Map<?,?> result = userWebClient.get()
                .uri(url)
                .headers(h -> h.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        if(result!=null) {
            final Object data = result.get("data");
            if (data instanceof Map) {
                final Object invitations = ((Map<?, ?>) data).get("invitations");
                if (invitations instanceof List) {
                    return ((List<?>) invitations).stream().filter(i -> i instanceof String).map(i -> MetaModelUtils.castToUUID((String) i)).filter(Objects::nonNull).collect(Collectors.toSet());
                }
            }
        }
        return Collections.emptySet();
    }


    public <T> T executeQueryForIndexing(Class<T> clazz, DataStage dataStage, String queryId, int from, int size) {
        String url = String.format("%s/queries/%s/instances?stage=%s&from=%d&size=%d", kgCoreEndpoint, queryId, dataStage, from, size);
        return executeCallForIndexing(clazz, url);
    }


    public <T> T executeQueryForInstance(Class<T> clazz, DataStage dataStage, String queryId, String id, boolean asServiceAccount) {
        String url = String.format("%s/queries/%s/instances?stage=%s&instanceId=%s", kgCoreEndpoint, queryId, dataStage, id);
        try {
            return executeCallForInstance(clazz, url, asServiceAccount);
        } catch (WebClientResponseException.NotFound e){
            return null;
        }
    }

    @SuppressWarnings("java:S3740")
    public Map getInstance(String id, DataStage dataStage, boolean asServiceAccount) { 
        String url = String.format("%s/instances/%s?stage=%s", kgCoreEndpoint, id, dataStage);
        return executeCallForInstance(Map.class, url, asServiceAccount);
    }

    public void uploadQuery(String queryId, String payload) {
        String url = String.format("%s/queries/%s?space=kg-search", kgCoreEndpoint, queryId);
        serviceAccountWebClient.put()
                .uri(url)
                .body(BodyInserters.fromValue(payload))
                .headers(h -> h.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private <T> T executeCallForInstance(Class<T> clazz, String url, boolean asServiceAccount) {
        WebClient webClient = asServiceAccount ? this.serviceAccountWebClient : this.userWebClient;
        return webClient.get()
                .uri(url)
                .headers(h -> h.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .retrieve()
                .bodyToMono(clazz)
                .doOnSuccess(GracefulDeserializationProblemHandler::parsingErrorHandler)
                .doFinally(t -> GracefulDeserializationProblemHandler.ERROR_REPORTING_THREAD_LOCAL.remove())
                .block();
    }

    private <T> T executeCallForIndexing(Class<T> clazz, String url) {
        return doExecuteCallForIndexing(clazz, url, 0);
    }

    private <T> T doExecuteCallForIndexing(Class<T> clazz, String url, int currentTry) {
        try{
            return serviceAccountWebClient.get()
                    .uri(url)
                    .headers(h -> h.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                    .retrieve()
                    .bodyToMono(clazz).doOnSuccess(GracefulDeserializationProblemHandler::parsingErrorHandler)
                    .doFinally(t -> GracefulDeserializationProblemHandler.ERROR_REPORTING_THREAD_LOCAL.remove())
                    .block();
        }
        catch (WebClientResponseException e){
            logger.warn("Was not able to execute call for indexing", e);
            if(currentTry<MAX_RETRIES){
                final long waitingTime = currentTry * currentTry * 10000L;
                logger.warn("Retrying to execute call for indexing for max {} more times - next time in {} seconds", MAX_RETRIES-currentTry, waitingTime/1000);
                try{
                    Thread.sleep(waitingTime);
                    return doExecuteCallForIndexing(clazz, url, currentTry+1);
                }
                catch (InterruptedException ie){
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            else{
                logger.error("Was not able to execute the call for indexing. Going to skip it.", e);
                return null;
            }
        }
    }
}
