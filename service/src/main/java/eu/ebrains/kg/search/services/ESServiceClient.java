package eu.ebrains.kg.search.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchResult;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ESServiceClient {
    private final Integer querySize = 10000; //TODO Pagination?

    private final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1000000)).build();
    private final WebClient webClient = WebClient.builder().exchangeStrategies(exchangeStrategies).build();

    @Value("${es.endpoint}")
    String elasticSearchEndpoint;

    private String getQuery(String id) {
        String normalizeId = id.replace("/", "\\/");
        return String.format("{\n" +
                "\"track_total_hits\": true,\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"term\": {\n" +
                "            \"identifier\": \"%s\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}", normalizeId);
    }

    public ElasticSearchDocument getDocument(String index, String id) {
        ElasticSearchResult result =  webClient.post()
                .uri(String.format("%s/%s/_search", elasticSearchEndpoint, index))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(getQuery(id)))
                .retrieve()
                .bodyToMono(ElasticSearchResult.class)
                .block();
        ElasticSearchResult.Hits hits = result.getHits();
        List<ElasticSearchDocument> documents = hits == null? Collections.emptyList():hits.getHits();
        if (documents.isEmpty()) {
            ElasticSearchDocument doc = new ElasticSearchDocument();
            doc.setId(id);
            doc.setType("_doc");
            doc.setIndex(index);
            return doc;
        }
        ElasticSearchDocument doc = documents.get(0);
        doc.setId(id);
        return doc;
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
