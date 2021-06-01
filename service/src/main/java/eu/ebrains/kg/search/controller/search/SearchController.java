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
import eu.ebrains.kg.search.constants.Queries;
import eu.ebrains.kg.search.controller.kg.KGv3;
import eu.ebrains.kg.search.controller.translators.FileOfKGV3Translator;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv3;
import eu.ebrains.kg.search.model.source.openMINDSv3.FileV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.File;
import eu.ebrains.kg.search.services.ESServiceClient;
import eu.ebrains.kg.search.utils.ESHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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
    private final KGv3 kgV3;

    public SearchController(ESServiceClient esServiceClient, KGv3 kgV3) throws JsonProcessingException {
        this.esServiceClient = esServiceClient;
        this.kgV3 = kgV3;
    }

    private static class ResultsOfKGV3FileV3 extends ResultsOfKGv3<FileV3> {}
    public Map<String, Object> getFilesForLive(String repositoryId, int from, int size) {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> params = Map.of("fileRepositoryId", String.format("https://kg.ebrains.eu/api/instances/%s", repositoryId));
        ResultsOfKGv3<FileV3> queryResult = kgV3.executeQueryForIndexing(ResultsOfKGV3FileV3.class, DataStage.IN_PROGRESS, Queries.FILE_QUERY_ID, from, size, params);
        if (queryResult != null) {
            List<FileV3> files = queryResult.getData();
            if (!CollectionUtils.isEmpty(files)) {
                FileOfKGV3Translator translator = new FileOfKGV3Translator();
                List<File> translatedFiles = files.stream().map(file -> translator.translate(file, DataStage.IN_PROGRESS, true)).collect(Collectors.toList());
                List<Object> data = translatedFiles.stream().map(file -> {
                    Map<String, Object> item = new HashMap<>();
                    if (StringUtils.isNotBlank(file.getName())) {
                        item.put("value", file.getName());
                    }
                    if (StringUtils.isNotBlank(file.getIri())) {
                        item.put("url", file.getIri());
                    }
                    if (StringUtils.isNotBlank(file.getSize())) {
                        item.put("fileSize", file.getSize());
                    }
                    if (StringUtils.isNotBlank(file.getFormat())) {
                        item.put("format", file.getFormat());
                    }
                    return item;
                }).collect(Collectors.toList());
                result.put("total", queryResult.getTotal());
                result.put("data", data);
            } else {
                result.put("total", 0);
                result.put("data", Collections.emptyList());
            }
        } else {
            result.put("total", 0);
            result.put("data", Collections.emptyList());
        }
        return result;
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
