package eu.ebrains.kg.search.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.ebrains.kg.search.controller.labels.LabelsController;
import eu.ebrains.kg.search.services.ESServiceClient;
import eu.ebrains.kg.search.utils.ESHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class Search {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String parentCountLabel = "temporary_parent_doc_count";
    private final String docCountLabel = "doc_count";
    private final JsonNode parentDocCountObj = objectMapper.readTree(
            String.format("{\"%s\": {\"reverse_nested\": {}}}", parentCountLabel)
    );

    private final ESServiceClient esServiceClient;
    private final LabelsController labelsController;

    public Search(ESServiceClient esServiceClient, LabelsController labelsController) throws JsonProcessingException {
        this.esServiceClient = esServiceClient;
        this.labelsController = labelsController;
    }

    @GetMapping("/labels")
    public Map<String, Object> getLabels() throws URISyntaxException, IOException {
        Map<String, Object> labels = new HashMap<>();
        labels.put("_source", labelsController.generateLabels());
        return labels;
    }

    @GetMapping("/groups/{group}/types/{type}/documents/{id}")
    public ResponseEntity<?> getDocument(@PathVariable("group") String group,
                                         @PathVariable("type") String type,
                                         @PathVariable("id") String id,
                                         @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        String index = ESHelper.getIndexFromGroup(type, group);
        try {
            return ResponseEntity.ok(esServiceClient.getDocument(index, id));
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/groups/{group}/search")
    public ResponseEntity<?> search(@PathVariable("group") String group,
                                    @RequestBody String payload) throws JsonProcessingException {
        JsonNode jsonNode = adaptEsQueryForNestedDocument(payload);
        String index = ESHelper.getIndexFromGroup("*", group);
        String result = esServiceClient.searchDocuments(index, jsonNode);
        JsonNode resultJson = objectMapper.readTree(result);
        JsonNode updatedJson = updateEsResponseWithNestedDocument(resultJson);
        return ResponseEntity.ok(updatedJson);
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
