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

package eu.ebrains.kg.search.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.ebrains.kg.common.controller.kg.KGv2;
import eu.ebrains.kg.common.controller.kg.KGv3;
import eu.ebrains.kg.common.controller.translators.TranslationController;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.TranslatorModel;
import eu.ebrains.kg.common.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.common.services.DOICitationFormatter;
import eu.ebrains.kg.common.services.ESServiceClient;
import eu.ebrains.kg.common.services.KGV2ServiceClient;
import eu.ebrains.kg.common.utils.MetaModelUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.search.controller.definition.DefinitionController;
import eu.ebrains.kg.search.controller.search.SearchController;
import eu.ebrains.kg.search.model.FacetValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.security.Principal;
import java.util.*;

@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@SuppressWarnings("java:S1452") // we keep the generics intentionally
public class Search {
    private final KGV2ServiceClient KGV2ServiceClient;
    private final ESServiceClient esServiceClient;
    private final DefinitionController definitionController;
    private final SearchController searchController;
    private final TranslationController translationController;
    private final KGv2 kgV2;
    private final KGv3 kgV3;
    private final DOICitationFormatter doiCitationFormatter;

    @Value("${eu.ebrains.kg.commit}")
    String commit;

    public Search(KGV2ServiceClient KGV2ServiceClient, ESServiceClient esServiceClient, DefinitionController definitionController, SearchController searchController, TranslationController translationController, KGv2 kgV2, KGv3 kgV3, DOICitationFormatter doiCitationFormatter) throws JsonProcessingException {
        this.KGV2ServiceClient = KGV2ServiceClient;
        this.esServiceClient = esServiceClient;
        this.definitionController = definitionController;
        this.searchController = searchController;
        this.translationController = translationController;
        this.kgV3 = kgV3;
        this.kgV2 = kgV2;
        this.doiCitationFormatter = doiCitationFormatter;
    }

    @GetMapping("/auth/endpoint")
    public Map<String, String> getAuthEndpoint() {
        Map<String, String> result = new HashMap<>();
        String authEndpoint = KGV2ServiceClient.getAuthEndpoint();
        result.put("authEndpoint", authEndpoint);
        return result;
    }

    @GetMapping("/citation")
    public String getCitation(@RequestParam("doi") String doiWithoutPrefix, @RequestParam("style") String style, @RequestParam("contentType") String contentType) {
        String doi = String.format("https://doi.org/%s", doiWithoutPrefix);
        return doiCitationFormatter.getDOICitation(doi, style, contentType);
    }

    @GetMapping("/definition")
    public Map<String, Object> getDefinition() {
        String authEndpoint = KGV2ServiceClient.getAuthEndpoint();
        Map<String, Object> result = new HashMap<>();
        result.put("types", definitionController.generateTypes());
        result.put("typeMappings", definitionController.generateTypeMappings());
        result.put("authEndpoint", authEndpoint);
        if(StringUtils.isNotBlank(commit) && !commit.equals("\"\"")){
            result.put("commit", commit);
        }
        return result;
    }

    @GetMapping("/groups")
    public ResponseEntity<?> getGroups(Principal principal) { 
        if (searchController.isInInProgressRole(principal)) {
            return ResponseEntity.ok(Arrays.asList(
                    Map.of("name", "curated",
                            "label", "in progress"),
                    Map.of("name", "public",
                            "label", "publicly released")
            ));
        } else {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @SuppressWarnings("java:S3740") // we keep the generics intentionally
    @GetMapping("/{org}/{domain}/{schema}/{version}/{id}/live")
    public ResponseEntity<Map> translate(@PathVariable("org") String org,
                                         @PathVariable("domain") String domain,
                                         @PathVariable("schema") String schema,
                                         @PathVariable("version") String version,
                                         @PathVariable("id") String id) throws TranslationException{
        try {
            String queryId = String.format("%s/%s/%s/%s", org, domain, schema, version);
            TranslatorModel<?, ?, ?, ?> translatorModel = TranslatorModel.MODELS.stream().filter(m -> m.getV2translator()!=null && m.getV2translator().getQueryIds().contains(queryId)).findFirst().orElse(null);
            if(translatorModel!=null){
                TargetInstance v = translationController.translateToTargetInstanceForLiveMode(kgV2, translatorModel.getV2translator(), queryId, DataStage.IN_PROGRESS, id, true, false);
                if(v!=null){
                   return ResponseEntity.ok(searchController.getLiveDocument(v));
                }
            }
            translatorModel = TranslatorModel.MODELS.stream().filter(m -> m.getV1translator()!=null && m.getV1translator().getQueryIds().contains(queryId)).findFirst().orElse(null);
            if(translatorModel!=null){
                TargetInstance v = translationController.translateToTargetInstanceForLiveMode(kgV2, translatorModel.getV1translator(), queryId, DataStage.IN_PROGRESS, id, true, false);
                if(v!=null){
                    return ResponseEntity.ok(searchController.getLiveDocument(v));
                }
            }
            return ResponseEntity.notFound().build();
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @SuppressWarnings("java:S3740") // we keep the generics intentionally
    @GetMapping("/{id}/live")
    public ResponseEntity<Map> translate(@PathVariable("id") String id, @RequestParam(required = false, defaultValue = "false") boolean skipReferenceCheck) throws TranslationException {
        try {
            final List<String> typesOfInstance = kgV3.getTypesOfInstance(id, DataStage.IN_PROGRESS, false);
            final TranslatorModel<?, ?, ?, ?> translatorModel = TranslatorModel.MODELS.stream().filter(m -> m.getV3translator() != null && m.getV3translator().semanticTypes().stream().anyMatch(typesOfInstance::contains)).findFirst().orElse(null);
            if(translatorModel!=null) {
                final String queryId = typesOfInstance.stream().map(type -> translatorModel.getV3translator().getQueryIdByType(type)).findFirst().orElse(null);
                if (queryId != null) {
                    final TargetInstance v = translationController.translateToTargetInstanceForLiveMode(kgV3, translatorModel.getV3translator(), queryId, DataStage.IN_PROGRESS, id, false, !skipReferenceCheck);
                    if (v != null) {
                        return ResponseEntity.ok(searchController.getLiveDocument(v));
                    }
                }
            }
            return ResponseEntity.notFound().build();
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @GetMapping("/groups/public/documents/{id}")
    public ResponseEntity<?> getDocumentForPublic(@PathVariable("id") String id) {
        try {
            Map<String, Object> res = searchController.getSearchDocument(DataStage.RELEASED, id);
            if (res != null) {
                return ResponseEntity.ok(res);
            }
            return ResponseEntity.notFound().build();
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }


    @GetMapping("/repositories/{id}/files/live")
    public ResponseEntity<?> getFilesFromRepoForLive(@PathVariable("id") String repositoryId, 
                                                     @RequestParam(required = false, name = "searchAfter") String searchAfter,
                                                     @RequestParam(required = false, defaultValue = "10000", name = "size") int size,
                                                     @RequestParam(required = false, defaultValue = "", name = "format") String format,
                                                     @RequestParam(required = false, defaultValue = "", name = "groupingType") String groupingType,
                                                       Principal principal) {
        final UUID repositoryUUID = MetaModelUtils.castToUUID(repositoryId);
        final UUID searchAfterUUID = MetaModelUtils.castToUUID(searchAfter);
        if(repositoryUUID == null || (searchAfter != null && searchAfterUUID == null) || size > 10000){
            return ResponseEntity.badRequest().build();
        }
        if (searchController.canReadLiveFiles(principal, repositoryUUID)) {
            try {
                return searchController.getFilesFromRepo(DataStage.IN_PROGRESS, repositoryUUID, searchAfterUUID, size, format, groupingType);
            } catch (WebClientResponseException e) {
                return ResponseEntity.status(e.getStatusCode()).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/repositories/{id}/files/formats/live")
    public ResponseEntity<?> getFileFormatsFromRepoForLive(@PathVariable("id") String repositoryId, 
                                                     Principal principal) {
        final UUID repositoryUUID = MetaModelUtils.castToUUID(repositoryId);
        if(repositoryUUID == null ){
            return ResponseEntity.badRequest().build();
        }
        if (searchController.canReadLiveFiles(principal, repositoryUUID)) {
            return searchController.getFileFormatsFromRepo(DataStage.IN_PROGRESS, repositoryUUID);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/repositories/{id}/files/groupingTypes/live")
    public ResponseEntity<?> getGroupingTypesFromRepoForLive(@PathVariable("id") String repositoryId, 
                                                     Principal principal) {
        final UUID repositoryUUID = MetaModelUtils.castToUUID(repositoryId);
        if(repositoryUUID == null){
            return ResponseEntity.badRequest().build();
        }
        if (searchController.canReadLiveFiles(principal, repositoryUUID)) {
            //kgV3.executeQueryForInstance("clazz", DataStage.IN_PROGRESS, "queryId", repositoryId, false)
            return searchController.getGroupingTypesFromRepo(DataStage.IN_PROGRESS, repositoryUUID);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/groups/public/repositories/{id}/files/formats")
    public ResponseEntity<?> getFileFormatsFromRepoForPublic(@PathVariable("id") String repositoryId) {
        final UUID repositoryUUID = MetaModelUtils.castToUUID(repositoryId);
        if(repositoryUUID == null){
            return ResponseEntity.badRequest().build();
        }
        return searchController.getFileFormatsFromRepo(DataStage.RELEASED, repositoryUUID);
    }

    @GetMapping("/groups/curated/repositories/{id}/files/formats")
    public ResponseEntity<?> getFileFormatsFromRepoForCurated(@PathVariable("id") String repositoryId, 
                                                        Principal principal) {
        final UUID repositoryUUID = MetaModelUtils.castToUUID(repositoryId);
        if(repositoryUUID == null){
            return ResponseEntity.badRequest().build();
        }
        if (searchController.isInInProgressRole(principal)) {
            return searchController.getFileFormatsFromRepo(DataStage.IN_PROGRESS, repositoryUUID);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/groups/public/repositories/{id}/files/groupingTypes")
    public ResponseEntity<?> getGroupingTypesFromRepoForPublic(@PathVariable("id") String repositoryId) {
        final UUID repositoryUUID = MetaModelUtils.castToUUID(repositoryId);
        if(repositoryUUID == null){
            return ResponseEntity.badRequest().build();
        }
        return searchController.getGroupingTypesFromRepo(DataStage.RELEASED, repositoryUUID);
    }

    @GetMapping("/groups/curated/repositories/{id}/files/groupingTypes")
    public ResponseEntity<?> getGroupingTypesFromRepoForCurated(@PathVariable("id") String repositoryId, 
                                                        Principal principal) {
        final UUID repositoryUUID = MetaModelUtils.castToUUID(repositoryId);
        if (repositoryUUID == null) {
            return ResponseEntity.badRequest().build();
        }
        if (searchController.isInInProgressRole(principal)) {
            return searchController.getGroupingTypesFromRepo(DataStage.IN_PROGRESS, repositoryUUID);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/groups/public/repositories/{id}/files")
    public ResponseEntity<?> getFilesFromRepoForPublic(@PathVariable("id") String id, 
                                                       @RequestParam(required = false, defaultValue = "", name = "searchAfter") String searchAfter,
                                                       @RequestParam(required = false, defaultValue = "10000", name = "size") int size,
                                                       @RequestParam(required = false, defaultValue = "", name = "format") String format,
                                                       @RequestParam(required = false, defaultValue = "", name = "groupingType") String groupingType) {
        final UUID searchAfterUUID = MetaModelUtils.castToUUID(searchAfter);
        final UUID uuid = MetaModelUtils.castToUUID(id);
        if ((searchAfter != null && searchAfterUUID == null) || uuid == null || size > 10000) {
            return ResponseEntity.badRequest().build();
        }
        return searchController.getFilesFromRepo(DataStage.RELEASED, uuid, searchAfterUUID, size, format, groupingType);
    }

    @GetMapping("/groups/curated/repositories/{id}/files")
    public ResponseEntity<?> getFilesFromRepoForCurated(@PathVariable("id") String id, 
                                                        @RequestParam(required = false, defaultValue = "", name = "searchAfter") String searchAfter,
                                                        @RequestParam(required = false, defaultValue = "10000", name = "size") int size,
                                                        @RequestParam(required = false, defaultValue = "", name = "format") String format,
                                                        @RequestParam(required = false, defaultValue = "", name = "groupingType") String groupingType,
                                                        Principal principal) {
        final UUID searchAfterUUID = MetaModelUtils.castToUUID(searchAfter);
        final UUID uuid = MetaModelUtils.castToUUID(id);
        if ((searchAfter != null && searchAfterUUID == null) || uuid == null || size > 10000) {
            return ResponseEntity.badRequest().build();
        }
        if (searchController.isInInProgressRole(principal)) {
            return searchController.getFilesFromRepo(DataStage.IN_PROGRESS, uuid, searchAfterUUID, size, format, groupingType);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/groups/public/documents/{type}/{id}")
    public ResponseEntity<?> getDocumentForPublic(@PathVariable("type") String type, @PathVariable("id") String id) {
        try {
            Map<String, Object> res = searchController.getSearchDocument(DataStage.RELEASED, String.format("%s/%s", type, id));
            if (res != null) {
                return ResponseEntity.ok(res);
            }
            return ResponseEntity.notFound().build();
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @GetMapping("/groups/curated/documents/{id}")
    public ResponseEntity<?> getDocumentForCurated(@PathVariable("id") String id, Principal principal) { 
        if (searchController.isInInProgressRole(principal)) {
            try {
                Map<String, Object> res = searchController.getSearchDocument(DataStage.IN_PROGRESS, id);
                if (res != null) {
                    return ResponseEntity.ok(res);
                }
                return ResponseEntity.notFound().build();
            } catch (WebClientResponseException e) {
                return ResponseEntity.status(e.getStatusCode()).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/groups/curated/documents/{type}/{id}")
    public ResponseEntity<?> getDocumentForCurated(@PathVariable("type") String type, @PathVariable("id") String id, Principal principal) { 
        if (searchController.isInInProgressRole(principal)) {
            try {
                Map<String, Object> res = searchController.getSearchDocument(DataStage.IN_PROGRESS, String.format("%s/%s", type, id));
                if (res != null) {
                    return ResponseEntity.ok(res);
                }
                return ResponseEntity.notFound().build();
            } catch (WebClientResponseException e) {
                return ResponseEntity.status(e.getStatusCode()).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/groups/public/search")
    public ResponseEntity<?> searchPublic (
            @RequestParam(required = false, defaultValue = "", name = "q") String q,
            @RequestParam(required = false, defaultValue = "", name = "type") String type,
            @RequestParam(required = false, defaultValue = "0", name = "from") int from,
            @RequestParam(required = false, defaultValue = "20", name = "size") int size,
            @RequestBody Map<String, FacetValue> facetValues
    ) {
        try {
            return ResponseEntity.ok(searchController.search(q, type, from, size, facetValues, DataStage.RELEASED));
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/groups/curated/search")
    public ResponseEntity<?> searchCurated(
            @RequestParam(required = false, defaultValue = "", name = "q") String q,
            @RequestParam(required = false, defaultValue = "", name = "type") String type,
            @RequestParam(required = false, defaultValue = "0", name = "from") int from,
            @RequestParam(required = false, defaultValue = "20", name = "size") int size,
            @RequestBody Map<String, FacetValue> facetValues,
            Principal principal) {
        if (searchController.isInInProgressRole(principal)) {
            try {
                return ResponseEntity.ok(searchController.search(q, type, from, size, facetValues, DataStage.IN_PROGRESS));
            } catch (WebClientResponseException e) {
                return ResponseEntity.status(e.getStatusCode()).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
