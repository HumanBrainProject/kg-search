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

package eu.ebrains.kg.search.controller.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.ebrains.kg.search.controller.authentication.UserInfoRoles;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchResult;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.File;
import eu.ebrains.kg.search.services.ESServiceClient;
import eu.ebrains.kg.search.utils.ESHelper;
import eu.ebrains.kg.search.utils.MetaModelUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class SearchController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String parentCountLabel = "temporary_parent_doc_count";
    private final String docCountLabel = "doc_count";
    private final JsonNode parentDocCountObj = objectMapper.readTree(
            String.format("{\"%s\": {\"reverse_nested\": {}}}", parentCountLabel)
    );
    private final ESServiceClient esServiceClient;
    private final MetaModelUtils utils;
    private final UserInfoRoles userInfoRoles;

    public SearchController(ESServiceClient esServiceClient, MetaModelUtils utils, UserInfoRoles userInfoRoles) throws JsonProcessingException {
        this.esServiceClient = esServiceClient;
        this.utils = utils;
        this.userInfoRoles = userInfoRoles;
    }

    public boolean isInInProgressRole(Principal principal) {
        return userInfoRoles.isInAnyOfRoles((KeycloakAuthenticationToken) principal, "team", "collab-kg-search-in-progress-administrator", "collab-kg-search-in-progress-editor", "collab-kg-search-in-progress-viewer");
    }

    private Map<String, Object> formatAggregation(ElasticSearchResult esResult, String aggregation) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isNotBlank(aggregation) &&
                esResult != null &&
                esResult.getAggregations() != null &&
                esResult.getAggregations().containsKey(aggregation) &&
                esResult.getAggregations().get(aggregation) != null &&
                esResult.getAggregations().get(aggregation).getBuckets() != null) {

            List<String> formats = esResult.getAggregations().get(aggregation).getBuckets().stream()
                    .map(bucket -> bucket.getKey())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            result.put("total", formats.size());
            result.put("data", formats);
        } else {
            result.put("total", 0);
            result.put("data", Collections.emptyList());
        }
        return result;
    }

    private Map<String, Object> formatFilesResponse(ElasticSearchResult filesFromRepo) {
        Map<String, Object> result = new HashMap<>();
        if (filesFromRepo != null && filesFromRepo.getHits() != null && filesFromRepo.getHits().getHits() != null) {
            List<ElasticSearchDocument> hits = filesFromRepo.getHits().getHits();
            List<Object> data = hits.stream().map(e -> e.getSource()).filter(Objects::nonNull).collect(Collectors.toList());
            ElasticSearchResult.Total total = filesFromRepo.getHits().getTotal();
            result.put("total", total.getValue());
            result.put("data", data);
            if (hits.size() > 0) {
                result.put("searchAfter", hits.get(hits.size() - 1).getId());
            }
        } else {
            result.put("total", 0);
            result.put("data", Collections.emptyList());
        }
        return result;
    }

    private ResponseEntity<?> getAggregationFromRepo(DataStage stage, String id, String field) {
        try {
            String fileIndex = ESHelper.getAutoReleasedIndex(stage, File.class);
            Map<String, String> aggs = Map.of("patterns", field);
            ElasticSearchResult esResult = esServiceClient.getAggregationsFromRepo(fileIndex, id, aggs);
            Map<String, Object> result = formatAggregation(esResult, "patterns");
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    public ResponseEntity<?> getFileBundlesFromRepo(DataStage stage, String id) {
        return getAggregationFromRepo(stage, id, "groupingTypes.keyword");
    }

    public ResponseEntity<?> getFileFormatsFromRepo(DataStage stage, String id) {
        return getAggregationFromRepo(stage, id, "format.value.keyword");
    }

    public ResponseEntity<?> getFilesFromRepo(DataStage stage, String id, String searchAfter, int size, String format, String fileBundle) {
        try {
            String fileIndex = ESHelper.getAutoReleasedIndex(stage, File.class);
            ElasticSearchResult filesFromRepo = esServiceClient.getFilesFromRepo(fileIndex, id, searchAfter, size, format, fileBundle);
            Map<String, Object> result = formatFilesResponse(filesFromRepo);
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }


    public JsonNode getResult(String payload, DataStage dataStage) throws JsonProcessingException {
        String index = ESHelper.getIndexesForSearch(dataStage);
        JsonNode jsonNode = adaptEsQueryForNestedDocument(payload);
        String result = esServiceClient.searchDocuments(index, jsonNode);
        JsonNode resultJson = objectMapper.readTree(result);
        return updateEsResponseWithNestedDocument(resultJson);
    }

    private JsonNode adaptEsQueryForNestedDocument(String payload) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(payload);
        List<String> paths = findPathForKey(jsonNode, "", "nested");
        if (!CollectionUtils.isEmpty(paths)) {
            paths.forEach(path -> {
                String aggsPath = String.format("%s/aggs", path);
                JsonNode aggs = jsonNode.at(aggsPath);
                if (!aggs.isEmpty()) {
                    aggs.fields().forEachRemaining(i -> {
                        if (i.getValue().has("terms")) {
                            ((ObjectNode) i.getValue()).set("aggs", parentDocCountObj);
                        }
                    });
                }
            });
        }
        return jsonNode;
    }

    private JsonNode updateEsResponseWithNestedDocument(JsonNode jsonSrc) {
        try {
            JsonNode json = jsonSrc.deepCopy();
            List<String> innerList = findPathForKey(json, "", parentCountLabel);
            List<String> buckets = innerList.stream().map(this::trimLastSegment)
                    .distinct()
                    .collect(Collectors.toList());

            buckets.forEach(path -> {
                ArrayNode arrayNode = (ArrayNode) json.at(path);
                int sum = 0;
                for (int i = 0; i < arrayNode.size(); i++) {
                    ObjectNode objectNode = (ObjectNode) arrayNode.get(i);
                    int count = objectNode.at(String.format("/%s/%s", parentCountLabel, docCountLabel)).asInt();
                    sum += count;
                    objectNode.remove(parentCountLabel);
                    objectNode.set(docCountLabel, new IntNode(count));
                }

                String inner = trimLastSegment(trimLastSegment(path)); // Remove everything after slash(/) before last
                ObjectNode innerObject = (ObjectNode) json.at(inner);
                innerObject.set(docCountLabel, new IntNode(sum));
            });
            return json;
        } catch (Exception e) {
            logger.info(String.format("Exception in json response update. Error:\n%s\nInput is used:\n%s", e.getMessage(), jsonSrc.asText()));
            return jsonSrc;
        }
    }

    private String trimLastSegment(String path) {
        int index = path.lastIndexOf('/');
        return path.substring(0, index);
    }

    private static List<String> findPathForKey(JsonNode jsonNode, String path, String key) {
        List<String> result = new ArrayList<>();
        if (jsonNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> next = it.next();
                if (next.getKey().equals(key)) {
                    result.add(path);
                }
                String childPath = String.format("%s/%s", path, next.getKey());
                result.addAll(findPathForKey(next.getValue(), childPath, key));
            }
        } else if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (int i = 0; i < arrayNode.size(); i++) {
                String childPath = String.format("%s/%s", path, i);
                result.addAll(findPathForKey(arrayNode.get(i), childPath, key));
            }
        }
        return result;
    }
}
