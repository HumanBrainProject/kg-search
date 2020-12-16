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
        StringBuilder operations = new StringBuilder();
        instances.forEach(instance -> {
            operations.append(String.format("{ \"index\" : { \"_id\" : \"%s\" } } \n", instance.getIdentifier().getValue()));
            try {
                operations.append(objectMapper.writeValueAsString(instance)).append("\n");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        esServiceClient.updateIndex(index, operations.toString());
    }

    public void updateIndex(List<TargetInstance> instances, String type, DatabaseScope databaseScope) {
        String index = ESHelper.getIndex(type, databaseScope);
        HashSet<String> ids = new HashSet<>();
        StringBuilder operations = new StringBuilder();
        instances.forEach( instance -> {
            String identifier = instance.getIdentifier().getValue();
            ids.add(identifier);
            operations.append(String.format("{ \"index\" : { \"_id\" : \"%s\" } } \n", identifier));
            try {
                operations.append(objectMapper.writeValueAsString(instance)).append("\n");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        ElasticSearchResult documents = esServiceClient.getDocuments(index);
        documents.getHits().getHits().forEach(document -> {
            if(!ids.contains(document.getId())) {
                operations.append(String.format("{ \"delete\" : { \"_id\" : \"%s\" } } \n", document.getId()));
            }
        });
        esServiceClient.updateIndex(index, operations.toString());
    }

}

