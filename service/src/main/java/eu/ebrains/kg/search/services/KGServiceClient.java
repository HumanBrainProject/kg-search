package eu.ebrains.kg.search.services;

import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class KGServiceClient {
    private final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1000000)).build();
    private final WebClient webClient = WebClient.builder().exchangeStrategies(exchangeStrategies).build();

    @Value("${kgquery.endpoint}")
    String kgQueryEndpoint;

    private static final String vocab = "https://schema.hbp.eu/search/";

    public <T> T executeQuery(String query, DatabaseScope databaseScope, Class<T> clazz, String token) {
        String url = String.format("%s/%s/instances/?databaseScope=%s&vocab=%s", kgQueryEndpoint, query, databaseScope, vocab);
        return executeCall(clazz, token, url);
    }

    public <T> T executeQuery(String query, String id, DatabaseScope databaseScope, Class<T> clazz, String token) {
        String url = String.format("%s/%s/instances/%s?databaseScope=%s&vocab=%s", kgQueryEndpoint, query, id, databaseScope, vocab);
        return executeCall(clazz, token, url);
    }

    public <T> T executeQueryByIdentifier(String query, String identifier, DatabaseScope databaseScope, Class<T> clazz, String token) {
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
