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

package eu.ebrains.kg.search.controller.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
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

    public void recreateSearchIndex(Map<String, Object> mapping, String type, DataStage dataStage) {
        String index = ESHelper.getSearchIndex(type, dataStage);
        try {
            esServiceClient.deleteIndex(index);
        } catch (WebClientResponseException e) {
            if(e.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw  e;
            }
        }
        esServiceClient.createIndex(index, mapping);
    }

    public void recreateIdentifiersIndex(Map<String, Object> mapping, DataStage dataStage) {
        String index = ESHelper.getIdentifierIndex(dataStage);
        try {
            esServiceClient.deleteIndex(index);
        } catch (WebClientResponseException e) {
            if(e.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw  e;
            }
        }
        esServiceClient.createIndex(index, mapping);
    }

    public void indexSearchDocuments(List<TargetInstance> instances, String type, DataStage dataStage) {
        String index = ESHelper.getSearchIndex(type, dataStage);
        StringBuilder operations = new StringBuilder();
        instances.forEach(instance -> {
            operations.append(String.format("{ \"index\" : { \"_id\" : \"%s\" } } \n", instance.getId()));
            try {
                operations.append(objectMapper.writeValueAsString(instance)).append("\n");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        esServiceClient.updateIndex(index, operations.toString());
    }

    public void indexIdentifierDocuments(List<TargetInstance> instances, DataStage dataStage) {
        String index = ESHelper.getIdentifierIndex(dataStage);
        StringBuilder operations = new StringBuilder();
        instances.forEach(instance -> {
            operations.append(String.format("{ \"index\" : { \"_id\" : \"%s\" } } \n", instance.getId()));
            try {
                operations.append(objectMapper.writeValueAsString(instance)).append("\n");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        esServiceClient.updateIndex(index, operations.toString());
    }

    public void updateSearchIndex(List<TargetInstance> instances, String type, DataStage dataStage) {
        String index = ESHelper.getSearchIndex(type, dataStage);
        HashSet<String> ids = new HashSet<>();
        StringBuilder operations = new StringBuilder();
        instances.forEach( instance -> {
            String identifier = instance.getIdentifier().get(0);
            ids.add(identifier);
            operations.append(String.format("{ \"index\" : { \"_id\" : \"%s\" } } \n", identifier));
            try {
                operations.append(objectMapper.writeValueAsString(instance)).append("\n");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        List<ElasticSearchDocument> documents = esServiceClient.getDocuments(index);
        documents.forEach(document -> {
            if(!ids.contains(document.getId())) {
                operations.append(String.format("{ \"delete\" : { \"_id\" : \"%s\" } } \n", document.getId()));
            }
        });
        esServiceClient.updateIndex(index, operations.toString());
    }

    public void updateIdentifiersIndex(List<TargetInstance> instances, String type, DataStage dataStage) {
        String index = ESHelper.getIdentifierIndex(dataStage);
        HashSet<String> ids = new HashSet<>();
        StringBuilder operations = new StringBuilder();
        instances.forEach( instance -> {
            String identifier = instance.getId();
            ids.add(identifier);
            operations.append(String.format("{ \"index\" : { \"_id\" : \"%s\" } } \n", identifier));
            try {
                operations.append(objectMapper.writeValueAsString(instance)).append("\n");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        List<ElasticSearchDocument> documents = esServiceClient.getDocuments(index);
        documents.stream().filter(hit -> hit.getType().equals(type)).forEach(document -> {
            if(!ids.contains(document.getId())) {
                operations.append(String.format("{ \"delete\" : { \"_id\" : \"%s\" } } \n", document.getId()));
            }
        });
        esServiceClient.updateIndex(index, operations.toString());
    }

}

