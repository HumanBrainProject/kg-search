package eu.ebrains.kg.search.controller.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchResult;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.services.ESServiceClient;
import eu.ebrains.kg.search.utils.ESHelper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
public class ElasticSearchController {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ESServiceClient esServiceClient;

    public ElasticSearchController(ESServiceClient esServiceClient) {
        this.esServiceClient = esServiceClient;
    }

    public void recreateIndex(Map<String, Object> mapping, String type, DatabaseScope databaseScope) {
        String index = ESHelper.getIndex(type, databaseScope);
        try {
            esServiceClient.deleteIndex(index);
        } catch (WebClientResponseException e) {
            if(e.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw  e;
            }
        }
        esServiceClient.createIndex(index, mapping);
    }

    public void indexDocuments(List<TargetInstance> instances, String type, DatabaseScope databaseScope) {
        String index = ESHelper.getIndex(type, databaseScope);
        String operations = instances.stream().reduce("", (acc, instance) -> {
            acc += String.format("{ \"index\" : { \"_id\" : \"%s\" } } \n", instance.getIdentifier().getValue());
            try {
                acc += objectMapper.writeValueAsString(instance) + "\n";
                return acc;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }, String::concat);
        esServiceClient.updateIndex(index, operations);
    }

    public void updateIndex(List<TargetInstance> instances, String type, DatabaseScope databaseScope) {
        String index = ESHelper.getIndex(type, databaseScope);
        HashSet<String> ids = new HashSet<>();

        String updateOperations = instances.stream().reduce("", (acc, instance) -> {
            String identifier = instance.getIdentifier().getValue();
            ids.add(identifier);
            acc += String.format("{ \"index\" : { \"_id\" : \"%s\" } } \n", identifier);
            try {
                acc += objectMapper.writeValueAsString(instance) + "\n";
                return acc;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }, String::concat);

        ElasticSearchResult documents = esServiceClient.getDocuments(index);
        String deleteOperations = documents.getHits().getHits().stream().reduce("", (acc, document) -> {
            if(!ids.contains(document.getId())) {
                acc += String.format("{ \"delete\" : { \"_id\" : \"%s\" } } \n", document.getId());
            }
            return acc;
        }, String::concat);

        esServiceClient.updateIndex(index, updateOperations + deleteOperations);
    }

}

