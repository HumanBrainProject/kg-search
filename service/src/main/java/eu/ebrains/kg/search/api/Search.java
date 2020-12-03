package eu.ebrains.kg.search.api;


import eu.ebrains.kg.search.controller.labels.LabelsController;
import eu.ebrains.kg.search.services.ESServiceClient;
import eu.ebrains.kg.search.utils.ESHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class Search {
    private final ESServiceClient esServiceClient;
    private final LabelsController labelsController;

    public Search(ESServiceClient esServiceClient, LabelsController labelsController) {
        this.esServiceClient = esServiceClient;
        this.labelsController = labelsController;
    }

    @GetMapping("/labels")
    public Map<String, Object> getLabels() {
        return labelsController.generateLabels();
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
}
