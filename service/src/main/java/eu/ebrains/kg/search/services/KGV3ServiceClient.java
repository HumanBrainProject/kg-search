package eu.ebrains.kg.search.services;

import eu.ebrains.kg.search.model.DataStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class KGV3ServiceClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final WebClient serviceAccountWebClient;
    private final WebClient userWebClient;
    private final String kgCoreEndpoint;
    private static final String vocab = "https://schema.hbp.eu/search/";

    public KGV3ServiceClient(@Qualifier("asServiceAccount") WebClient serviceAccountWebClient, @Qualifier("asUser") WebClient userWebClient,  @Value("${kgcore.endpoint}") String kgCoreEndpoint){
        this.serviceAccountWebClient = serviceAccountWebClient;
        this.userWebClient = userWebClient;
        this.kgCoreEndpoint = kgCoreEndpoint;
    }

    public <T> T executeQueryForIndexing(String queryId, DataStage dataStage, Class<T> clazz) {
        String url = String.format("%s/queries/%s/instances?stage=%s&vocab=%s", kgCoreEndpoint, queryId, dataStage, vocab);
        return executeCallForIndexing(clazz, url);
    }

    public <T> T executeQueryForLiveMode(String queryId, String id, DataStage dataStage, Class<T> clazz) {
        //TODO: Add endpoint in kg core to retrieve unique instance by queryId
//        String url = String.format("%s/queries/%s/instances/%s?stage=%s&vocab=%s", kgCoreEndpoint, queryId, id, dataStage, vocab);
        String url = String.format("%s/queries/%s/instances?stage=%s&vocab=%s", kgCoreEndpoint, queryId, dataStage, vocab);
        return executeCallForLive(clazz, url);
    }

    public Map getInstanceForLiveMode(String id, DataStage dataStage) {
        String url = String.format("%s/instances/%s?stage=%s", kgCoreEndpoint, id, dataStage);
        return executeCallForLive(Map.class, url);
    }

    private <T> T executeCallForLive(Class<T> clazz, String url) {
        return userWebClient.get()
                .uri(url)
                .headers(h ->
                {
                    h.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                })
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }

    private <T> T executeCallForIndexing(Class<T> clazz, String url) {
        return serviceAccountWebClient.get()
                .uri(url)
                .headers(h ->
                {
                    h.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                })
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }
}
