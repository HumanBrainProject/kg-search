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
public class ESServiceClient {

    private final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1000000)).build();
    private final WebClient webClient = WebClient.builder().exchangeStrategies(exchangeStrategies).build();


    @Value("${es.endpoint}")
    String elasticSearchEndpoint;

    private static final Pattern editorIdPattern = Pattern.compile("(.+)/(.+)/(.+)/(.+)/(.+)");

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
                .uri(String.format("%s%s", elasticSearchEndpoint, uri))
                .headers(h ->
                {
                    h.add("Authorization", "Bearer " + token);
                    h.add("Accept", "application/json");
                })
                .retrieve()
                .bodyToMono(clazz)
                .block();
    }

    public ElasticSearchResult getDocuments(String index) {
        return webClient.get()
                .uri(String.format("%s/%s/_search", elasticSearchEndpoint, index))
                .retrieve()
                .bodyToMono(ElasticSearchResult.class)
                .block();
    }

    public void deleteIndex(String index) {
        webClient.delete()
                .uri(String.format("%s/%s", elasticSearchEndpoint, index))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void createIndex(String index, Map<String, Object> mapping) {
        webClient.put()
                .uri(String.format("%s/%s", elasticSearchEndpoint, index))
                .body(BodyInserters.fromValue(mapping))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void updateIndex(String index, String operations) {
        webClient.post()
                .uri(String.format("%s/%s/_bulk", elasticSearchEndpoint, index))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_NDJSON_VALUE)
                .body(BodyInserters.fromValue(operations))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public String getIndex(String type, DatabaseScope databaseScope) {
        String indexPrefix = databaseScope == DatabaseScope.INFERRED ? "in_progress" : "publicly_released";
        return String.format("%s_%s", indexPrefix, type.toLowerCase());
    }

}
