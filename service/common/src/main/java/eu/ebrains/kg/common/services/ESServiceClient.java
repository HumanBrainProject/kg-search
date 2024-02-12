/*
 *  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *  Copyright 2021 - 2023 EBRAINS AISBL
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This open source software code was developed in part or in whole in the
 *  Human Brain Project, funded from the European Union's Horizon 2020
 *  Framework Programme for Research and Innovation under
 *  Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 *  (Human Brain Project SGA1, SGA2 and SGA3).
 */

package eu.ebrains.kg.common.services;

import eu.ebrains.kg.common.model.elasticsearch.Document;
import eu.ebrains.kg.common.model.elasticsearch.Result;
import eu.ebrains.kg.common.utils.MetaModelUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("java:S1192")
@Component
public class ESServiceClient {

    public final static Integer ES_QUERY_SIZE = 10000;

    private final static String QUERY = "query";

    private final static String INDEX = "index";

    private final static String IDENTIFIER = "identifier";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final WebClient webClient;

    private final String elasticSearchEndpoint;

    private static String metricsQuery(int size) {
        return "{\n" +
                "  \"fields\": [\n" +
                "    \"last30DaysViews\"\n" +
                "  ],\n" +
                "  \"sort\": {\n" +
                "    \"last30DaysViews\": \"desc\"\n" +
                "  },\n" +
                "  \"size\": " + size + ",\n" +
                "  \"_source\": false\n" +
                "}";
    }

    public ESServiceClient(WebClient webClient, @Value("${es.endpoint}") String elasticSearchEndpoint) {
        this.webClient = webClient;
        this.elasticSearchEndpoint = elasticSearchEndpoint;
    }

    private String getQuery(String id) {
        String normalizeId = id.replace("/", "\\/");
        return String.format("""
                {
                  "query": {
                    "bool": {
                      "must": [
                        {
                          "term": {
                            "identifier": "%s"
                          }
                        }
                      ]
                    }
                  }
                }""", normalizeId);
    }


    private String getPaginatedQueryForSitemap(String id, Set<String> relevantTypes) {
        final String types = String.join(", ", relevantTypes.stream().filter(r -> r.matches("[a-zA-Z()\\d ]*")).map(r -> String.format("\"%s\"", r)).collect(Collectors.toSet()));
        if (id == null) {
            return String.format("""
                    {
                     "size": %d,
                     "sort": [
                        {"_id": "asc"}
                     ],
                     "query": {
                        "terms":{
                            "type.value":[%s]
                        }
                     },
                     "_source": false
                    }""", ES_QUERY_SIZE, types);
        }
        return String.format("""
                {
                 "size": %d,
                 "sort": [
                    {"_id": "asc"}
                  ],
                  "search_after": [
                      "%s"
                   ],
                   "query": {
                        "terms":{
                            "type.value":[%s]
                        }
                     },
                     "_source": false
                }""", ES_QUERY_SIZE, id, relevantTypes);
    }

    private String getAgg(String field) {
        if (StringUtils.isBlank(field)) {
            return null;
        }
        return String.format(
                """
                        {
                          "terms": {
                            "field": "%s",
                            "size": 1000000000
                          }
                        }""", field);
    }

    private String getAggs(Map<String, String> aggs) {
        List<String> aggList = aggs.entrySet().stream().map(entry -> {
                    String name = entry.getKey();
                    String value = getAgg(entry.getValue());
                    if (StringUtils.isBlank(name) || StringUtils.isBlank(value)) {
                        return null;
                    }
                    return String.format("\"%s\": %s", name, value);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(aggList)) {
            return null;
        }

        return "{\n" + StringUtils.join(aggList, ",\n ") + "\n}";
    }

    private String getQueryTerm(String term, String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return String.format(
                """
                        {
                          "term": {
                            "%s": "%s"
                          }
                        }""".indent(6), term, value);
    }

    private String getQuery(Map<String, String> terms) {
        List<String> termList = terms.entrySet().stream().map(entry -> getQueryTerm(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(termList)) {
            return "";
        }

        String must = null;
        if (termList.size() == 1) {
            must = termList.get(0);
        } else {
            must = "[\n" + StringUtils.join(termList, ",\n ") + "\n]";
        }

        return String.format(
                """
                        {
                          "bool": {
                            "must": %s
                          }
                        }""".indent(2), must);
    }

    private String getPayload(Map<String, String> parameters) {
        List<String> list = parameters.entrySet().stream().map(entry -> {
                    if (StringUtils.isBlank(entry.getValue())) {
                        return null;
                    }
                    return String.format("\"%s\": %s", entry.getKey(), entry.getValue());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return "{\n" + StringUtils.join(list, ",\n ") + "\n}";
    }

    private String getPaginatedFilesQuery(UUID fileRepositoryId, UUID searchAfter, int size, String format, String groupingType) {

        String sizeValue = String.format("%d", size);

        String sortValue =
                """
                        [
                            {"_id": "asc"}
                        ]""";

        String searchAfterValue = "";
        if (searchAfter != null) {
            searchAfterValue = String.format(
                    """
                            [
                              "%s"
                            ]""".indent(2), searchAfter);
        }

        Map<String, String> terms = Map.of(
                "fileRepository", fileRepositoryId.toString(),
                "format.value.keyword", format,
                "groupingTypes.name.keyword", groupingType
        );
        String queryValue = getQuery(terms);

        Map<String, String> parameters = Map.of(
                "size", sizeValue,
                "sort", sortValue,
                "search_after", searchAfterValue,
                QUERY, queryValue,
                "track_total_hits", "true"
        );

        return getPayload(parameters);
    }

    private String getAggregationsQuery(UUID fileRepositoryId, Map<String, String> aggs) {

        String aggsValue = getAggs(aggs);

        Map<String, String> terms = Map.of(
                "fileRepository", fileRepositoryId.toString()
        );
        String queryValue = getQuery(terms);

        Map<String, String> parameters = Map.of(
                "size", "0",
                "aggs", aggsValue,
                QUERY, queryValue
        );

        return getPayload(parameters);
    }

    private String getIdsOfPaginatedQuery(String id, String type) {
        if (id == null) {
            return String.format("""
                    {
                     "size": %d,
                     "sort": [
                        {"_id": "asc"}
                     ],
                      "query": {
                        "bool": {
                          "must": {
                              "term": {
                                "type.value": "%s"
                              }
                            }
                        }
                      },
                     "_source": "false"
                    }""", ES_QUERY_SIZE, type);
        }
        return String.format("""
                {
                 "size": %d,
                 "sort": [
                    {"_id": "asc"}
                  ],
                  "search_after": [
                      "%s"
                  ],
                  "query": {
                    "bool": {
                      "must": {
                          "term": {
                            "type.value": "%s"
                          }
                        }
                    }
                  },
                 "_source": "false"
                }""", ES_QUERY_SIZE, id, type);
    }

    @Getter
    @Setter
    public static class ESCountResult {
        private Long count;
    }


    public Document getDocumentByNativeId(String index, String id) {
        return webClient.get()
                .uri(String.format("%s/%s/_doc/%s", elasticSearchEndpoint, index, id))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(Document.class)
                .block();

    }

    public Document getDocument(String index, String id) {
        Result result = webClient.post()
                .uri(String.format("%s/%s/_search", elasticSearchEndpoint, index))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(getQuery(id)))
                .retrieve()
                .bodyToMono(Result.class)
                .block();
        Result.Hits hits = result == null ? null : result.getHits();
        List<Document> documents = hits == null ? Collections.emptyList() : hits.getHits();
        if (documents.isEmpty()) {
            Document doc = new Document();
            doc.setId(id);
            doc.setType("_doc");
            doc.setIndex(index);
            return doc;
        }
        Document doc = documents.get(0);
        doc.setId(id);
        return doc;
    }

    public List<Document> getDocumentsForSitemap(String index, Set<String> relevantTypes) {
        List<Document> result = new ArrayList<>();
        String searchAfter = null;
        boolean continueSearch = true;
        while (continueSearch) {
            Result documents = getPaginatedDocumentsForSitemap(index, searchAfter, relevantTypes);
            if (documents != null && documents.getHits() != null) {
                List<Document> hits = documents.getHits().getHits();
                result.addAll(hits);
                searchAfter = hits.size() < ES_QUERY_SIZE ? null : hits.get(hits.size() - 1).getId();
                continueSearch = searchAfter != null;
            } else {
                searchAfter = null;
                continueSearch = false;
            }
        }
        return result;
    }

    public List<String> getDocumentIds(String index, Class<?> type) {
        List<String> result = new ArrayList<>();
        String searchAfter = null;
        boolean continueSearch = true;
        while (continueSearch) {
            Result documents = getPaginatedDocumentIds(index, searchAfter, type);
            if (documents!=null && documents.getHits() != null) {
                List<Document> hits = documents.getHits().getHits();
                hits.forEach(hit -> result.add(hit.getId()));
                searchAfter = hits.size() < ES_QUERY_SIZE ? null : hits.get(hits.size() - 1).getId();
                continueSearch = searchAfter != null;
            } else {
                searchAfter = null;
                continueSearch = false;
            }
        }
        return result;
    }


    private Result getPaginatedDocumentsForSitemap(String index, String searchAfter, Set<String> relevantTypes) {
        String paginatedQuery = getPaginatedQueryForSitemap(searchAfter, relevantTypes);
        return webClient.post()
                .uri(String.format("%s/%s/_search", elasticSearchEndpoint, index))
                .body(BodyInserters.fromValue(paginatedQuery))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_NDJSON_VALUE)
                .retrieve()
                .bodyToMono(Result.class)
                .block();
    }

    public Result getFilesAggregationsFromRepo(String index, UUID fileRepositoryId, Map<String, String> aggs) {
        String query = getAggregationsQuery(fileRepositoryId, aggs);
        try {
            return webClient.post()
                    .uri(String.format("%s/%s/_search", elasticSearchEndpoint, index))
                    .body(BodyInserters.fromValue(query))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_NDJSON_VALUE)
                    .retrieve()
                    .bodyToMono(Result.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            } else {
                throw e;
            }
        }
    }

    public Result getFilesFromRepo(String index, UUID fileRepositoryId, UUID searchAfter, int size, String format, String groupingType) {
        String paginatedQuery = getPaginatedFilesQuery(fileRepositoryId, searchAfter, size, format, groupingType);
        try {
            return webClient.post()
                    .uri(String.format("%s/%s/_search", elasticSearchEndpoint, index))
                    .body(BodyInserters.fromValue(paginatedQuery))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_NDJSON_VALUE)
                    .retrieve()
                    .bodyToMono(Result.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            } else {
                throw e;
            }
        }
    }

    private Result getPaginatedDocumentIds(String index, String searchAfter, Class<?> type) {
        String paginatedQuery = getIdsOfPaginatedQuery(searchAfter, MetaModelUtils.getNameForClass(type));
        return webClient.post()
                .uri(String.format("%s/%s/_search", elasticSearchEndpoint, index))
                .body(BodyInserters.fromValue(paginatedQuery))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_NDJSON_VALUE)
                .retrieve()
                .bodyToMono(Result.class)
                .block();
    }

    public Result searchDocuments(String index, Map<String, Object> payload) {
        return searchDocuments(index, null, BodyInserters.fromValue(payload));
    }

    public Result searchDocuments(String index, String filterPath, BodyInserter<?, ? super ClientHttpRequest> payload) {
        return webClient.post()
                .uri(String.format("%s/%s/_search%s", elasticSearchEndpoint, index, filterPath == null ? "" : String.format("?filter_path=%s", filterPath)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .bodyToMono(Result.class)
                .block();
    }

    public void deleteIndex(String index) {
        webClient.delete()
                .uri(String.format("%s/%s", elasticSearchEndpoint, index))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void reindex(String source, String target) {
        final Map<String, Map<String, String>> payload = Map.of("source", Map.of(INDEX, source), "dest", Map.of(INDEX, target));
        webClient.post().uri(String.format("%s/_reindex", elasticSearchEndpoint))
                .body(BodyInserters.fromValue(payload)).retrieve().bodyToMono(Void.class).block();
    }

    public boolean checkIfIndexExists(String index) {
        try {
            final ResponseEntity<Void> response = webClient.head().uri(String.format("%s/%s", elasticSearchEndpoint, index)).retrieve().toBodilessEntity().block();
            return response!=null && response.getStatusCode().is2xxSuccessful();
        } catch (WebClientResponseException.NotFound exception) {
            return false;
        }
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
        Map result = webClient.post()
                .uri(String.format("%s/%s/_bulk", elasticSearchEndpoint, index))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_NDJSON_VALUE)
                .body(BodyInserters.fromValue(operations))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        if (result != null && ((boolean) result.get("errors"))) {
            ((List<Map<String, Object>>) result.get("items")).forEach(item -> {
                Map<String, Object> instance = (Map) item.get(INDEX);
                if ((int) instance.get("status") >= 400) {
                    logger.error(instance.toString());
                }
            });
        }
    }

    public void updateIndex(String index, List<StringBuilder> operationsList) {
        logger.info(String.format("Updating index %s with %s bulk operations", index, operationsList.size()));
        operationsList.forEach(operations -> this.updateIndex(index, operations.toString()));
        logger.info(String.format("Done updating index %s", index));
    }

    public Result getMetrics(String index, int size) {
        return webClient.post()
                .uri(String.format("%s/%s/_search", elasticSearchEndpoint, index))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(metricsQuery(size)))
                .retrieve()
                .bodyToMono(Result.class)
                .block();
    }

}
