package eu.ebrains.kg.search.services;

import com.fasterxml.jackson.databind.JsonNode;
import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final WebClient webClient;

    private final String elasticSearchEndpoint;

    public ESServiceClient(WebClient webClient, @Value("${es.endpoint}") String elasticSearchEndpoint) {
        this.webClient = webClient;
        this.elasticSearchEndpoint = elasticSearchEndpoint;
    }

    private String getQuery(String id) {
        String normalizeId = id.replace("/", "\\/");
        return String.format("{\n" +
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


    private String getPaginatedQuery(String id) {
        if (id == null) {
            return String.format("{\n" +
                    " \"size\": %d,\n" +
                    " \"sort\": [\n" +
                    "    {\"_id\": \"asc\"}\n" +
                    " ]\n" +
                    "}", Constants.esQuerySize);
        }
        return String.format("{\n" +
                " \"size\": %d,\n" +
                " \"sort\": [\n" +
                "    {\"_id\": \"asc\"}\n" +
                "  ],\n" +
                "  \"search_after\": [\n" +
                "      \"%s\"\n" +
                "   ]\n" +
                "}", Constants.esQuerySize, id);
    }

    public ElasticSearchDocument getDocument(String index, String id) {
        ElasticSearchResult result = webClient.post()
                .uri(String.format("%s/%s/_search", elasticSearchEndpoint, index))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(getQuery(id)))
                .retrieve()
                .bodyToMono(ElasticSearchResult.class)
                .block();
        ElasticSearchResult.Hits hits = result.getHits();
        List<ElasticSearchDocument> documents = hits == null ? Collections.emptyList() : hits.getHits();
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

    public List<ElasticSearchDocument> getDocuments(String index) {
        List<ElasticSearchDocument> result = new ArrayList<>();
        String searchAfter = null;
        while (result.isEmpty() || searchAfter != null) {
            ElasticSearchResult documents = getPaginatedDocuments(index, searchAfter);
            List<ElasticSearchDocument> hits = documents.getHits().getHits();
            result.addAll(hits);
            searchAfter = hits.size() < Constants.esQuerySize ? null:hits.get(hits.size()-1).getId();
        }
        return result;
    }

    private ElasticSearchResult getPaginatedDocuments(String index, String searchAfter) {
        String paginatedQuery = getPaginatedQuery(searchAfter);
        return webClient.post()
                .uri(String.format("%s/%s/_search", elasticSearchEndpoint, index))
                .body(BodyInserters.fromValue(paginatedQuery))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_NDJSON_VALUE)
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
        Map<String, Object> result = webClient.post()
                .uri(String.format("%s/%s/_bulk", elasticSearchEndpoint, index))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_NDJSON_VALUE)
                .body(BodyInserters.fromValue(operations))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        if ((boolean) result.get("errors")) {
            ((List<Map<String, Object>>) result.get("items")).forEach(item -> {
                Map<String, Object> instance = (Map) item.get("index");
                if ((int) instance.get("status") >= 400) {
                    logger.error(instance.toString());
                }
            });
        }
    }

    public void updateIndex(String index, List<StringBuilder> operationsList) {
        logger.info(String.format("updating index %s with %s bulk operations", index, operationsList.size()));
        operationsList.forEach(operations -> {
            this.updateIndex(index, operations.toString());
        });
    }
}
