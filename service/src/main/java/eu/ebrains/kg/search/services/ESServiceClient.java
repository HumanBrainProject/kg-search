/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

package eu.ebrains.kg.search.services;

import com.fasterxml.jackson.databind.JsonNode;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchResult;
import eu.ebrains.kg.search.utils.MetaModelUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("java:S1192")
@Component
public class ESServiceClient {

    private final static Integer ES_QUERY_SIZE = 10000;

    private final static String QUERY = "query";

    private final static String INDEX = "index";

    private final static String IDENTIFIER = "identifier";

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
                    "}", ES_QUERY_SIZE);
        }
        return String.format("{\n" +
                " \"size\": %d,\n" +
                " \"sort\": [\n" +
                "    {\"_id\": \"asc\"}\n" +
                "  ],\n" +
                "  \"search_after\": [\n" +
                "      \"%s\"\n" +
                "   ]\n" +
                "}", ES_QUERY_SIZE, id);
    }

    private String getAgg(String field) {
        if(StringUtils.isBlank(field)) {
            return null;
        }
        return String.format(
            "{\n" +
            "  \"terms\": {\n" +
            "    \"field\": \"%s\",\n" +
            "    \"size\": 1000000000\n" +
            "  }\n" +
            "}", field);
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
        if(StringUtils.isBlank(value)) {
            return null;
        }
        return String.format(
                "      {\n" +
                "        \"term\": {\n" +
                "          \"%s\": \"%s\"\n" +
                "        }\n" +
                "      }", term, value);
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
                "  {\n" +
                "    \"bool\": {\n" +
                "      \"must\": %s\n" +
                "    }\n" +
                "  }", must);
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

    private String getPaginatedFilesQuery(String fileRepositoryId, String searchAfter, int size, String format, String groupingType) {

        String sizeValue = String.format("%d", size);

        String sortValue =
            "[\n" +
            "    {\"_id\": \"asc\"}\n" +
            "]";

        String searchAfterValue = "";
        if (StringUtils.isNotBlank(searchAfter)) {
            searchAfterValue = String.format(
                "  [\n" +
                "    \"%s\"\n" +
                "  ]", searchAfter);
        }

        Map<String, String> terms = Map.of(
                "fileRepository", fileRepositoryId,
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

    private String getAggregationsQuery(String fileRepositoryId, Map<String, String> aggs) {

        String aggsValue = getAggs(aggs);

        Map<String, String> terms = Map.of(
                "fileRepository", fileRepositoryId
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
            return String.format("{\n" +
                    " \"size\": %d,\n" +
                    " \"sort\": [\n" +
                    "    {\"_id\": \"asc\"}\n" +
                    " ],\n" +
                    "  \"query\": {\n" +
                    "    \"bool\": {\n" +
                    "      \"must\": {\n" +
                    "          \"term\": {\n" +
                    "            \"type.value\": \"%s\"\n" +
                    "          }\n" +
                    "        }\n" +
                    "    }\n" +
                    "  },\n" +
                    " \"_source\": \"false\"\n" +
                    "}", ES_QUERY_SIZE, type);
        }
        return String.format("{\n" +
                " \"size\": %d,\n" +
                " \"sort\": [\n" +
                "    {\"_id\": \"asc\"}\n" +
                "  ],\n" +
                "  \"search_after\": [\n" +
                "      \"%s\"\n" +
                "  ],\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must\": {\n" +
                "          \"term\": {\n" +
                "            \"type.value\": \"%s\"\n" +
                "          }\n" +
                "        }\n" +
                "    }\n" +
                "  },\n" +
                " \"_source\": \"false\"\n" +
                "}", ES_QUERY_SIZE, id, type);
    }

    @Getter
    @Setter
    public static class ESCountResult {
        private Long count;
    }


    public ElasticSearchDocument getDocument(String index, String id) {
        ElasticSearchResult result = webClient.post()
                .uri(String.format("%s/%s/_search", elasticSearchEndpoint, index))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(getQuery(id)))
                .retrieve()
                .bodyToMono(ElasticSearchResult.class)
                .block();
        ElasticSearchResult.Hits hits = result == null ? null:result.getHits();
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
        boolean continueSearch = true;
        while (continueSearch) {
            ElasticSearchResult documents = getPaginatedDocuments(index, searchAfter);
            List<ElasticSearchDocument> hits = documents.getHits().getHits();
            result.addAll(hits);
            searchAfter = hits.size() < ES_QUERY_SIZE ? null:hits.get(hits.size()-1).getId();
            continueSearch = searchAfter != null;
        }
        return result;
    }

    public List<String> getDocumentIds(String index, Class<?> type) {
        List<String> result = new ArrayList<>();
        String searchAfter = null;
        boolean continueSearch = true;
        while (continueSearch) {
            ElasticSearchResult documents = getPaginatedDocumentIds(index, searchAfter, type);
            List<ElasticSearchDocument> hits = documents.getHits().getHits();
            hits.forEach(hit -> result.add(hit.getId()));
            searchAfter = hits.size() < ES_QUERY_SIZE ? null:hits.get(hits.size()-1).getId();
            continueSearch = searchAfter != null;
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

    public ElasticSearchResult getAggregationsFromRepo(String index, String fileRepositoryId, Map<String, String> aggs) {
        String query = getAggregationsQuery(fileRepositoryId, aggs);
        try {
            return webClient.post()
                    .uri(String.format("%s/%s/_search", elasticSearchEndpoint, index))
                    .body(BodyInserters.fromValue(query))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_NDJSON_VALUE)
                    .retrieve()
                    .bodyToMono(ElasticSearchResult.class)
                    .block();
        } catch(WebClientResponseException e){
            if(e.getStatusCode() == HttpStatus.NOT_FOUND){
                return null;
            } else {
                throw e;
            }
        }
    }

    public ElasticSearchResult getFilesFromRepo(String index, String fileRepositoryId, String searchAfter, int size, String format, String groupingType) {
        String paginatedQuery = getPaginatedFilesQuery(fileRepositoryId, searchAfter, size, format, groupingType);
        try {
            return webClient.post()
                    .uri(String.format("%s/%s/_search", elasticSearchEndpoint, index))
                    .body(BodyInserters.fromValue(paginatedQuery))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_NDJSON_VALUE)
                    .retrieve()
                    .bodyToMono(ElasticSearchResult.class)
                    .block();
        } catch(WebClientResponseException e){
            if(e.getStatusCode() == HttpStatus.NOT_FOUND){
                return null;
            } else {
                throw e;
            }
        }
    }

    private ElasticSearchResult getPaginatedDocumentIds(String index, String searchAfter, Class<?> type) {
        String paginatedQuery = getIdsOfPaginatedQuery(searchAfter, MetaModelUtils.getNameForClass(type));
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

    public void reindex(String source, String target){
        final Map<String, Map<String, String>> payload = Map.of("source", Map.of(INDEX, source), "dest", Map.of(INDEX, target));
         webClient.post().uri(String.format("%s/_reindex", elasticSearchEndpoint))
                .body(BodyInserters.fromValue(payload)).retrieve().bodyToMono(Void.class).block();
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

    public Set<String> existingDocuments(String index, List<String> identifiers){
        int pageSize = 2000;
        int numberOfPages = (identifiers.size()/pageSize)+1;
        Set<String> result = new HashSet<>();
        for(int p = 0; p<numberOfPages; p++){
            Object query = Map.of(QUERY,
                    Map.of("terms",
                            Map.of(IDENTIFIER, identifiers.subList(p*pageSize, Math.min(identifiers.size(), (p+1)*pageSize)))
                    ),
                    "_source", Collections.singletonList(IDENTIFIER));
            ElasticSearchResult r = webClient.post()
                    .uri(String.format("%s/%s/_search?size=%d", elasticSearchEndpoint, index, pageSize))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(BodyInserters.fromValue(query))
                    .retrieve()
                    .bodyToMono(ElasticSearchResult.class)
                    .block();
            if(r!=null && r.getHits()!=null && r.getHits().getHits()!=null){
                r.getHits().getHits().forEach(esDocument -> {
                    if(esDocument != null && esDocument.getSource() !=null) {
                        result.addAll((List)esDocument.getSource().get(IDENTIFIER));
                    }
                });
            }
            else{
                throw new RuntimeException("Wasn't able to read existing documents from elasticsearch");
            }
        }
        return result;
    }

}
