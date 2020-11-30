package eu.ebrains.kg.search.services;

import eu.ebrains.kg.search.model.DatabaseScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ServiceClient {
    private final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1000000)).build();
    private final WebClient webClient = WebClient.builder().exchangeStrategies(exchangeStrategies).build();

    @Value("${kgquery.endpoint}")
    String kgQueryEndpoint;

    private static final String vocab = "https://schema.hbp.eu/search/";

    private static final Pattern editorIdPattern = Pattern.compile("(.+)/(.+)/(.+)/(.+)/(.+)");

    public <T> T executeQuery(String query, DatabaseScope databaseScope, Class<T> clazz, String token) {
        return webClient.get()
                .uri(String.format("%s/%s/instances/?databaseScope=%s&vocab=%s", kgQueryEndpoint, query, databaseScope, vocab))
                .headers(h ->
                {
                    h.add("Authorization", "Bearer " + token);
                    h.add("Accept", "application/json");
                })
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }

    public <T> T executeQuery(String query, String id, DatabaseScope databaseScope, Class<T> clazz, String token) {
        return webClient.get()
                .uri(String.format("%s/%s/instances/%s?databaseScope=%s&vocab=%s", kgQueryEndpoint, query, id, databaseScope, vocab))
                .headers(h ->
                {
                    h.add("Authorization", "Bearer " + token);
                    h.add("Accept", "application/json");
                })
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }

    public <T> T getDocument(DatabaseScope databaseScope, String type, String id, Class<T> clazz, String token) {
        String group = databaseScope.equals(DatabaseScope.RELEASED) ? "public" : "curated";
        return getDocument(String.format("/search/api/groups/%s/types/%s/documents/%s", group, type, id), clazz, token);
    }

    public <T> T getLiveDocument(String editorId, Class<T> clazz, String token) {
        Matcher m = editorIdPattern.matcher(editorId);
        if (!m.find()) {
            return null;
        }
        String org = m.group(1);
        String domain = m.group(2);
        String schema = m.group(3);
        String version = m.group(4);
        String id = m.group(5);
        return getDocument(String.format("/search/api/types/%s/%s/%s/%s/documents/%s/preview", org, domain, schema, version, id), clazz, token);
    }

    private <T> T getDocument(String uri, Class<T> clazz, String token) {
        return webClient.get()
                .uri(String.format("%s%s", "", uri)) // TODO: Use ES endpoint
                .headers(h ->
                {
                    h.add("Authorization", "Bearer " + token);
                    h.add("Accept", "application/json");
                })
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }

}
