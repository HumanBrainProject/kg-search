package eu.ebrains.kg.search.services;

import eu.ebrains.kg.search.model.DataStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class KGV3ServiceClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final WebClient serviceAccountWebClient;
    private final WebClient userWebClient;
    private final String kgCoreEndpoint;

    public KGV3ServiceClient(@Qualifier("asServiceAccount") WebClient serviceAccountWebClient, @Qualifier("asUser") WebClient userWebClient, @Value("${kgcore.endpoint}") String kgCoreEndpoint) {
        this.serviceAccountWebClient = serviceAccountWebClient;
        this.userWebClient = userWebClient;
        this.kgCoreEndpoint = kgCoreEndpoint;
    }

    public <T> T executeQueryForIndexing(String queryId, DataStage dataStage, Class<T> clazz) {
        String url = String.format("%s/queries/%s/instances?stage=%s", kgCoreEndpoint, queryId, dataStage);
        return executeCallForIndexing(clazz, url);
    }

    public <T> T executeQueryForLiveMode(String queryId, String id, DataStage dataStage, Class<T> clazz) {
        String url = String.format("%s/queries/%s/instances?stage=%s&instanceId=%s", kgCoreEndpoint, queryId, dataStage, id);
        return executeCallForLive(clazz, url);
    }

    public Map getInstanceForLiveMode(String id, DataStage dataStage) {
        String url = String.format("%s/instances/%s?stage=%s", kgCoreEndpoint, id, dataStage);
        return executeCallForLive(Map.class, url);
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

    private <T> T executeCallForLive(Class<T> clazz, String url) {
        return userWebClient.get()
                .uri(url)
                .headers(h -> h.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }

    private <T> T executeCallForIndexing(Class<T> clazz, String url) {
        return serviceAccountWebClient.get()
                .uri(url)
                .headers(h -> h.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }
}
