package eu.ebrains.kg.search.services;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.DatabaseScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class KGServiceClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1000000)).build();
    private final WebClient webClient = WebClient.builder().exchangeStrategies(exchangeStrategies).build();

    @Value("${kgquery.endpoint}")
    String kgQueryEndpoint;

    @Value("${kgcore.endpoint}")
    String kgCoreEndpoint;

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

    public <T> T executeQuery(String query, DataStage dataStage, Class<T> clazz, String token) {
        DatabaseScope databaseScope = dataStage.equals(DataStage.IN_PROGRESS) ? DatabaseScope.INFERRED: DatabaseScope.RELEASED;
        String url = String.format("%s/%s/instances/?databaseScope=%s&vocab=%s", kgQueryEndpoint, query, databaseScope, vocab);
        return executeCall(clazz, token, url);
    }

    public <T> T executeQuery(String query, String id, DataStage dataStage, Class<T> clazz, String token) {
        DatabaseScope databaseScope = dataStage.equals(DataStage.IN_PROGRESS) ? DatabaseScope.INFERRED: DatabaseScope.RELEASED;
        String url = String.format("%s/%s/instances/%s?databaseScope=%s&vocab=%s", kgQueryEndpoint, query, id, databaseScope, vocab);
        return executeCall(clazz, token, url);
    }

    public <T> T executeQueryByIdentifier(String query, String identifier, DataStage dataStage, Class<T> clazz, String token) {
        DatabaseScope databaseScope = dataStage.equals(DataStage.IN_PROGRESS) ? DatabaseScope.INFERRED: DatabaseScope.RELEASED;
        String url = String.format("%s/%s/instances?databaseScope=%s&vocab=%s&identifier=%s", kgQueryEndpoint, query, databaseScope, vocab, identifier);
        return executeCall(clazz, token, url);
    }

    private <T> T executeCall(Class<T> clazz, String token, String url) {
        return webClient.get()
                .uri(url)
                .headers(h ->
                {
                    h.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                    h.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                })
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }
}
