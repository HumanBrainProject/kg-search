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

package eu.ebrains.kg.projects.ebrains.translators.utils;


import eu.ebrains.kg.common.model.DOIReference;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.elasticsearch.Document;
import eu.ebrains.kg.common.model.elasticsearch.Result;
import eu.ebrains.kg.common.services.ESServiceClient;
import eu.ebrains.kg.common.utils.ESHelper;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.*;
import java.util.stream.Collectors;

public class SpecimenResolver {

    private final ESServiceClient esServiceClient;
    private final ESHelper esHelper;

    public SpecimenResolver(ESServiceClient esServiceClient, ESHelper esHelper) {
        this.esServiceClient = esServiceClient;
        this.esHelper = esHelper;
    }

    public Map<String, Set<DOIReference>> loadSpecimenLookupMap(DataStage stage) {
        Map<String, Set<DOIReference>> collector = new HashMap<>();
        doLoadSpecimenLookupMap(null, esHelper.getIndexesForDocument(stage), collector);
        //Remove the entries with only one referenced product
        return collector.entrySet().stream().filter(e -> e.getValue().size()>1).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    private String createQueryForLoadingTheSpecimenLookupMap(UUID searchAfterId) {
        return String.format("""
                {
                  "size": %d,
                  "_source": ["specimenIds", "doi.value", "id"],
                  "sort": [
                    {"_id": "asc"}
                  ],%s
                  "query": {
                  "bool": {
                    "must": [
                        {
                            "exists": {
                                "field": "specimenIds"
                            }
                        },
                        {
                            "exists": {
                                "field": "doi.value"
                            }
                        }
                    ]
                   }
                  }
                }
                """, ESServiceClient.ES_QUERY_SIZE, searchAfterId == null ? "" : String.format("""
                                
                  "search_after": [
                      "%s"
                  ],
                """, searchAfterId));
    }

    private void doLoadSpecimenLookupMap(UUID lastId, String index, Map<String, Set<DOIReference>> collector) {
        final String query = createQueryForLoadingTheSpecimenLookupMap(lastId);
        final Result result = esServiceClient.searchDocuments(index, "hits.hits._source", BodyInserters.fromValue(query));
        final Result.Hits hits = result.getHits();
        if (hits != null) {
            final List<Document> documents = hits.getHits();
            if (!CollectionUtils.isEmpty(documents)) {
                documents.forEach(document -> {
                    final Map<String, Object> docSource = document.getSource();
                    final UUID id = getId(docSource);
                    final String doi = getDOI(docSource);
                    final List<String> specimenIds = getSpecimenIds(docSource);
                    if(!CollectionUtils.isEmpty(specimenIds)){
                        specimenIds.forEach(specimenId -> {
                            collector.computeIfAbsent(specimenId, s -> new HashSet<>());
                            collector.get(specimenId).add(new DOIReference(doi, id));
                        });
                    }
                });
                final Document lastDocument = documents.get(documents.size() - 1);
                if(lastDocument!=null){
                    UUID nextId = getId(lastDocument.getSource());
                    if (nextId != null && !nextId.equals(lastId)) {
                        doLoadSpecimenLookupMap(nextId, index, collector);
                    }
                }
            }
        }


    }

    private List<String> getSpecimenIds(Map<String, Object> docSource){
        if(docSource!=null){
            final Object object = docSource.get("specimenIds");
            if(object instanceof List){
                return (List<String>) object;
            }
        }
        return null;
    }

    private UUID getId(Map<String, Object> docSource){
        if(docSource!=null){
            final Object object = docSource.get("id");
            if(object instanceof String){
                return UUID.fromString((String) object);
            }
        }
        return null;
    }

    private String getDOI(Map<String, Object> docSource) {
        final Object object = docSource.get("doi");
        if(object instanceof Map){
            final Object doiValue = ((Map<?,?>) object).get("value");
            if(doiValue instanceof String){
                return (String) doiValue;
            }
        }
        return null;
    }




}
