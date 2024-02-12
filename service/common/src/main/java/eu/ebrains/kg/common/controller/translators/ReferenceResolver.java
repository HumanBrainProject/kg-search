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

package eu.ebrains.kg.common.controller.translators;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.elasticsearch.Document;
import eu.ebrains.kg.common.model.elasticsearch.Result;
import eu.ebrains.kg.common.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.services.ESServiceClient;
import eu.ebrains.kg.common.utils.ESHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.*;

import static eu.ebrains.kg.common.controller.translators.Helpers.*;

@Component
public class ReferenceResolver {
    private final ESServiceClient esServiceClient;

    public ReferenceResolver(ESServiceClient esServiceClient) {
        this.esServiceClient = esServiceClient;
    }

    public Set<String> loadAllExistingIdentifiers(DataStage stage) {
        Set<String> collector = new HashSet<>();
        doLoadAllExistingIdentifiers(null, ESHelper.getIndexesForDocument(stage), collector);
        return collector;
    }

    private void doLoadAllExistingIdentifiers(String lastId, String index, Set<String> collector) {
        final String query = createQueryForLoadingAllIdentifiers(lastId);
        final Result result = esServiceClient.searchDocuments(index, "hits.hits._source.identifier,hits.hits._id", BodyInserters.fromValue(query));
        final Result.Hits hits = result.getHits();
        if (hits != null) {
            final List<Document> documents = hits.getHits();
            if (!CollectionUtils.isEmpty(documents)) {
                documents.forEach(document -> {
                    final Map<String, Object> docSource = document.getSource();
                    final Object identifier = docSource.get("identifier");
                    if(identifier instanceof List){
                        collector.addAll((List<String>)identifier);
                    }
                });
                final Document lastDocument = documents.get(documents.size() - 1);
                if(lastDocument!=null){
                    final String nextId = lastDocument.getId();
                    if (nextId != null && !nextId.equals(lastId)) {
                        doLoadAllExistingIdentifiers(nextId, index, collector);
                    }
                }
            }
        }


    }

    private String createQueryForLoadingAllIdentifiers(String searchAfterId) {
        return String.format("""
                {
                  "size": %d,
                  "_source": ["identifier"],
                  "sort": [
                    {"_id": "asc"}
                  ],%s
                  "query": {
                      "exists": {
                        "field": "identifier"
                      }
                   }
                }
                """, ESServiceClient.ES_QUERY_SIZE, searchAfterId == null ? "" : String.format("""
                                
                  "search_after": [
                      "%s"
                  ],
                """, searchAfterId));
    }

    public <Target extends TargetInstance> void clearNonResolvableReferences(List<Target> instances, Set<String> existingIdentifiers) {
        List<TargetInternalReference> references = new ArrayList<>();
        instances.forEach(i -> collectAllTargetInternalReferences(i, references));
        references.forEach(r -> {
            if (r.getReference() != null && !existingIdentifiers.contains(r.getReference())) {
                r.setReference(null);
            }
        });
    }
}
