/*
 *  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *  Copyright 2021 - 2023 EBRAINS AISBL
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This open source software code was developed in part or in whole in the
 *  Human Brain Project, funded from the European Union's Horizon 2020
 *  Framework Programme for Research and Innovation under
 *  Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 *  (Human Brain Project SGA1, SGA2 and SGA3).
 */

package eu.ebrains.kg.indexing.controller.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.common.services.ESServiceClient;
import eu.ebrains.kg.common.utils.ESHelper;
import eu.ebrains.kg.common.utils.MetaModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;

@Component
public class ElasticSearchController {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ESServiceClient esServiceClient;
    private final int ESOperationsMaxCharPayload = 1000000;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ElasticSearchController(ESServiceClient esServiceClient) {
        this.esServiceClient = esServiceClient;
    }


    public void reindexTemporaryToRealIndex(Class<?> type, DataStage dataStage, boolean autorelease){
        String source =  autorelease ? ESHelper.getAutoReleasedIndex(dataStage, type, true) : ESHelper.getSearchableIndex(dataStage, type, true);
        String target = autorelease ? ESHelper.getAutoReleasedIndex(dataStage, type, false) :ESHelper.getSearchableIndex(dataStage, type, false);
        esServiceClient.reindex(source, target);
        //We remove the temporary index after it has been reindexed.
        esServiceClient.deleteIndex(source);
    }

    public void recreateSearchIndex(Map<String, Object> mapping, Class<?> type, DataStage dataStage, boolean temporary) {
        String index = ESHelper.getSearchableIndex(dataStage, type, temporary);
        logger.info(String.format("Creating index Was %s for %s", index, MetaModelUtils.getNameForClass(type)));
        try {
            esServiceClient.deleteIndex(index);
        } catch (WebClientResponseException e) {
            if(e.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw  e;
            }
        }
        esServiceClient.createIndex(index, mapping);
        logger.info(String.format("Successfully created index %s for %s", index, MetaModelUtils.getNameForClass(type)));
    }

    public void recreateIdentifiersIndex(Map<String, Object> mapping, DataStage dataStage) {
        String index = ESHelper.getIdentifierIndex(dataStage);
        logger.info(String.format("Creating identifier index %s", index));
        try {
            esServiceClient.deleteIndex(index);
        } catch (WebClientResponseException e) {
            if(e.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw  e;
            }
        }
        esServiceClient.createIndex(index, mapping);
        logger.info(String.format("Successfully created identifier index %s", index));
    }

    public void recreateAutoReleasedIndex(DataStage stage, Map<String, Object> mapping, Class<?> type, boolean temporary) {
        String index = ESHelper.getAutoReleasedIndex(stage, type, temporary);
        logger.info(String.format("Creating index %s for %s", index, MetaModelUtils.getNameForClass(type)));
        try {
            esServiceClient.deleteIndex(index);
        } catch (WebClientResponseException e) {
            if(e.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw  e;
            }
        }
        esServiceClient.createIndex(index, mapping);
        logger.info(String.format("Successfully created index %s for %s", index, MetaModelUtils.getNameForClass(type)));
    }

    private List<StringBuilder> getInsertOperations(List<? extends TargetInstance> instances) {
        List<StringBuilder> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(instances)) {
            return result;
        }
        result.add(new StringBuilder());
        instances.forEach(instance -> {
            StringBuilder operations = result.get(result.size() - 1);
            if (operations.length() > ESOperationsMaxCharPayload) {
                operations = new StringBuilder();
                result.add(operations);
            }
            operations.append(String.format("{ \"index\" : { \"_id\" : \"%s\" } } \n", instance.getId()));
            try {
                operations.append(objectMapper.writeValueAsString(instance)).append("\n");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        return result;
    }

    private List<StringBuilder> getDeleteOperations(String index, Class<?> type, Set<String> idsToKeep) {
        List<StringBuilder> result = new ArrayList<>();
        result.add(new StringBuilder());
        List<String> ids = esServiceClient.getDocumentIds(index, type);
        ids.forEach(id -> {
            StringBuilder operations = result.get(result.size() - 1);
            if (operations.length() > ESOperationsMaxCharPayload) {
                operations = new StringBuilder();
                result.add(operations);
            }
            if(!idsToKeep.contains(id)) {
                operations.append(String.format("{ \"delete\" : { \"_id\" : \"%s\" } } \n", id));
            }
        });
        if (result.get(result.size() - 1).length() == 0) {
            result.remove(result.size() - 1);
        };
        return result;
    }

    private void updateIndex(String index, List<? extends TargetInstance> instances) {
        List<StringBuilder> operationsList = getInsertOperations(instances);
        if (!CollectionUtils.isEmpty(operationsList)) {
            esServiceClient.updateIndex(index, operationsList);
        }
    }

    private void removeDeprecatedDocuments(String index,  Class<?> type, Set<String> idsToKeep) {
        List<StringBuilder> operationsList = getDeleteOperations(index, type, idsToKeep);
        if (!CollectionUtils.isEmpty(operationsList)) {
            esServiceClient.updateIndex(index, operationsList);
        }
    }

    public void ensureResourcesIndex(){
        if(!esServiceClient.checkIfIndexExists(ESHelper.getResourcesIndex())) {
            esServiceClient.createIndex(ESHelper.getResourcesIndex(), Collections.emptyMap());
        }
    }

    public void addResource(String id, Map<String, Object> instance){
        StringBuilder op = new StringBuilder();
        op.append(String.format("{ \"index\" : { \"_id\" : \"%s\" } } \n", id));
        try {
            op.append(objectMapper.writeValueAsString(instance));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        op.append('\n');
        esServiceClient.updateIndex(ESHelper.getResourcesIndex(), op.toString());
    }

    public void deleteResource(String id){
        StringBuilder op = new StringBuilder();
        op.append(String.format("{ \"delete\" : { \"_id\" : \"%s\" } } \n", id));
        esServiceClient.updateIndex(ESHelper.getResourcesIndex(), op.toString());
    }


    public void updateSearchIndex(List<? extends TargetInstance> instances, Class<?> type, DataStage dataStage, boolean temporary) {
        updateIndex(ESHelper.getSearchableIndex(dataStage, type, temporary), instances);
    }

    public void updateIdentifiersIndex(List<? extends TargetInstance> instances, DataStage dataStage) {
        updateIndex(ESHelper.getIdentifierIndex(dataStage), instances);
    }

    public void updateAutoReleasedIndex(List<? extends TargetInstance> instances, DataStage dataStage, Class<?> type, boolean temporary) {
        updateIndex(ESHelper.getAutoReleasedIndex(dataStage, type, temporary), instances);
    }

    public void removeDeprecatedDocumentsFromSearchIndex( Class<?> type, DataStage dataStage, Set<String> idsToKeep, boolean temporary) {
        removeDeprecatedDocuments(ESHelper.getSearchableIndex(dataStage, type, temporary), type, idsToKeep);
    }

    public void removeDeprecatedDocumentsFromIdentifiersIndex(Class<?> type, DataStage dataStage, Set<String> idsToKeep) {
        removeDeprecatedDocuments(ESHelper.getIdentifierIndex(dataStage), type, idsToKeep);
    }

    public void removeDeprecatedDocumentsFromAutoReleasedIndex(Class<?> type, DataStage dataStage, Set<String> idsToKeep, boolean temporary) {
        removeDeprecatedDocuments(ESHelper.getAutoReleasedIndex(dataStage, type, temporary), type, idsToKeep);
    }



}

