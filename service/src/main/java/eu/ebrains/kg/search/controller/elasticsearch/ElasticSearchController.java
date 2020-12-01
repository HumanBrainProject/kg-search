package eu.ebrains.kg.search.controller.elasticsearch;

import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.services.ServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.xml.crypto.Data;
import java.util.List;
import java.util.Map;

@Component
public class ElasticSearchController {
    private final String publicIndexPrefix = "publicly_released";
    private final String curatedIndexPrefix = "in_progress";

    private final ServiceClient serviceClient;

    public ElasticSearchController(ServiceClient serviceClient) {
        this.serviceClient = serviceClient;
    }


    public String getPublicIndexPrefix() {
        return publicIndexPrefix;
    }

    public String getCuratedIndexPrefix() {
        return curatedIndexPrefix;
    }

    private String getIndexPrefix(DatabaseScope databaseScope) {
        return databaseScope == DatabaseScope.INFERRED ? curatedIndexPrefix : publicIndexPrefix;
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

    public void addDocuments(List<TargetInstance> instances, String type, DatabaseScope databaseScope) {
        String indexPrefix = this.getIndexPrefix(databaseScope);
        String index = String.format("%s_%s", indexPrefix, type.toLowerCase());

    }


}

