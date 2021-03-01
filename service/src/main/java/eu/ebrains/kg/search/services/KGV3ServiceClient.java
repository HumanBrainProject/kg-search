package eu.ebrains.kg.search.services;

import eu.ebrains.kg.search.model.DataStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class KGV3ServiceClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1000000)).build();
    private final WebClient webClient = WebClient.builder().exchangeStrategies(exchangeStrategies).build();

    @Value("${kgcore.endpoint}")
    String kgCoreEndpoint;

    @Value("${kgsearch.clientId}")
    String kgSearchClientId;

    @Value("${kgsearch.clientSecret}")
    String kgSearchClientSecret;

    private static final String vocab = "https://schema.hbp.eu/search/";

    public <T> T executeQuery(String queryId, DataStage dataStage, Class<T> clazz) {
        String url = String.format("%s/queries/%s/instances?stage=%s&vocab=%s", kgCoreEndpoint, queryId, dataStage, vocab);
        return executeCall(clazz, url);
    }

    public <T> T executeQuery(String queryId, String id, DataStage dataStage, Class<T> clazz, String token) {
        //TODO: Add endpoint in kg core to retrieve unique instance by queryId
//        String url = String.format("%s/queries/%s/instances/%s?stage=%s&vocab=%s", kgCoreEndpoint, queryId, id, dataStage, vocab);
        String url = String.format("%s/queries/%s/instances?stage=%s&vocab=%s", kgCoreEndpoint, queryId, dataStage, vocab);
        return executeCall(clazz, token, url);
    }

    public Map getInstance(String id, DataStage dataStage, String token) {
        String url = String.format("%s/instances/%s?stage=%s", kgCoreEndpoint, id, dataStage);
        return executeCall(Map.class, token, url);
    }

    private <T> T executeCall(Class<T> clazz, String token, String url) {
        return webClient.get()
                .uri(url)
                .headers(h ->
                {
                    h.add(HttpHeaders.AUTHORIZATION, token);
                    h.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                })
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }

    private <T> T executeCall(Class<T> clazz, String url) {
        return webClient.get()
                .uri(url)
                .headers(h ->
                {
                    h.add("Client-Id", kgSearchClientId);
                    h.add("Client-SA-Secret", kgSearchClientSecret);
                    h.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                })
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }
}
