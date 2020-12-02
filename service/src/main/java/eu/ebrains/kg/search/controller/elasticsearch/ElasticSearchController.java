package eu.ebrains.kg.search.controller.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchResult;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.services.ServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
public class ElasticSearchController {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ServiceClient serviceClient;

    public ElasticSearchController(ServiceClient serviceClient) {
        this.serviceClient = serviceClient;
    }

    private String getIndexPrefix(DatabaseScope databaseScope) {
        return databaseScope == DatabaseScope.INFERRED ? "in_progress" : "publicly_released";
    }

    public void recreateIndex(Map<String, Object> mapping, String type, DatabaseScope databaseScope) {
        String indexPrefix = this.getIndexPrefix(databaseScope);
        String index = String.format("%s_%s", indexPrefix, type.toLowerCase());
        try {
            serviceClient.deleteIndex(index);
        } catch (WebClientResponseException e) {
            if(e.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw  e;
            }
        }
        serviceClient.createIndex(index, mapping);
    }

    public void indexDocuments(List<TargetInstance> instances, String type, DatabaseScope databaseScope) {
        String indexPrefix = this.getIndexPrefix(databaseScope);
        String index = String.format("%s_%s", indexPrefix, type.toLowerCase());
        String operations = instances.stream().reduce("", (acc, instance) -> {
            acc += String.format("{ \"index\" : { \"_id\" : \"%s\" } } \n", instance.getIdentifier().getValue());
            try {
                acc += objectMapper.writeValueAsString(instance) + "\n";
                return acc;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }, String::concat);
        serviceClient.updateIndex(index, operations);
    }

    public void updateIndex(List<TargetInstance> instances, String type, DatabaseScope databaseScope) {
        String indexPrefix = this.getIndexPrefix(databaseScope);
        String index = String.format("%s_%s", indexPrefix, type.toLowerCase());
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

        ElasticSearchResult documents = serviceClient.getDocuments(index);
        String deleteOperations = documents.getHits().getHits().stream().reduce("", (acc, document) -> {
            if(!ids.contains(document.getId())) {
                acc += String.format("{ \"delete\" : { \"_id\" : \"%s\" } } \n", document.getId());
            }
            return acc;
        }, String::concat);

        serviceClient.updateIndex(index, updateOperations + deleteOperations);
    }

}

