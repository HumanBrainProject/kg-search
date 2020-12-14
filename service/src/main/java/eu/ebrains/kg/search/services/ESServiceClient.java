package eu.ebrains.kg.search.services;

import com.fasterxml.jackson.databind.JsonNode;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
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
    private final Integer querySize = 10000; //TODO Pagination?

    private final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1000000)).build();
    private final WebClient webClient = WebClient.builder().exchangeStrategies(exchangeStrategies).build();


    @Value("${es.endpoint}")
    String elasticSearchEndpoint;

    public ElasticSearchDocument getDocument(String index, String id) {
        return webClient.get()
                .uri(String.format("%s/%s/_doc/%s", elasticSearchEndpoint, index, id))
                .retrieve()
                .bodyToMono(ElasticSearchDocument.class)
                .block();
    }

    public ElasticSearchResult getDocuments(String index) {
        return webClient.get()
                .uri(String.format("%s/%s/_search?size=%d", elasticSearchEndpoint, index, querySize))
                .retrieve()
                .bodyToMono(ElasticSearchResult.class)
                .block();
    }

    public String searchDocuments(String index, JsonNode payload) {
        return webClient.post()
                .uri(String.format("%s/%s/_search", elasticSearchEndpoint, index))
                .body(BodyInserters.fromValue(payload))
                .retrieve()
                .bodyToMono(String.class)
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
}
