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
import eu.ebrains.kg.search.controller.Constants;
import eu.ebrains.kg.search.controller.authentication.UserInfoRoles;
import eu.ebrains.kg.search.controller.labels.LabelsController;
import eu.ebrains.kg.search.controller.search.SearchController;
import eu.ebrains.kg.search.controller.translators.TranslationController;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchDocument;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchResult;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.File;
import eu.ebrains.kg.search.services.ESServiceClient;
import eu.ebrains.kg.search.services.KGServiceClient;
import eu.ebrains.kg.search.utils.ESHelper;
import eu.ebrains.kg.search.utils.MetaModelUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class Search {
    private final KGServiceClient kgServiceClient;
    private final ESServiceClient esServiceClient;
    private final LabelsController labelsController;
    private final SearchController searchController;
    private final UserInfoRoles userInfoRoles;
    private final TranslationController translationController;
    private final MetaModelUtils utils;

    @Value("${eu.ebrains.kg.commit}")
    String commit;

    public Search(KGServiceClient kgServiceClient, ESServiceClient esServiceClient, LabelsController labelsController, SearchController searchController, UserInfoRoles userInfoRoles, TranslationController translationController, MetaModelUtils utils) throws JsonProcessingException {
        this.kgServiceClient = kgServiceClient;
        this.esServiceClient = esServiceClient;
        this.labelsController = labelsController;
        this.searchController = searchController;
        this.userInfoRoles = userInfoRoles;
        this.translationController = translationController;
        this.utils = utils;
    }

    private boolean isInInProgressRole(Principal principal) {
        return userInfoRoles.isInAnyOfRoles((KeycloakAuthenticationToken) principal, "team", "collab-kg-search-in-progress-administrator", "collab-kg-search-in-progress-editor", "collab-kg-search-in-progress-viewer");
    }

    @GetMapping("/auth/endpoint")
    public Map<String, String> getAuthEndpoint() {
        Map<String, String> result = new HashMap<>();
        String authEndpoint = kgServiceClient.getAuthEndpoint();
        result.put("authEndpoint", authEndpoint);
        return result;
    }

    @GetMapping("/labels")
    public Map<String, Object> getLabels() {
        String authEndpoint = kgServiceClient.getAuthEndpoint();
        Map<String, Object> result = new HashMap<>();
        result.put("_source", labelsController.generateLabels());
        result.put("authEndpoint", authEndpoint);
        if(StringUtils.isNotBlank(commit)){
            result.put("commit", commit);
        }
        return result;
    }

    @GetMapping("/groups")
    public ResponseEntity<?> getGroups(Principal principal) {
        if (isInInProgressRole(principal)) {
            return ResponseEntity.ok(Constants.GROUPS);
        } else {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/{org}/{domain}/{schema}/{version}/{id}/live")
    public ResponseEntity<Map> translate(@PathVariable("org") String org,
                                         @PathVariable("domain") String domain,
                                         @PathVariable("schema") String schema,
                                         @PathVariable("version") String version,
                                         @PathVariable("id") String id,
                                         @RequestHeader("X-Legacy-Authorization") String legacyAuthorization) {
        try {
            TargetInstance instance = translationController.createInstanceFromKGv2(DataStage.IN_PROGRESS, true, org, domain, schema, version, id, legacyAuthorization);
            return ResponseEntity.ok(Map.of("_source", instance));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @GetMapping("/{id}/live")
    public ResponseEntity<Map> translate(@PathVariable("id") String id) {
        try {
            TargetInstance instance = translationController.createLiveInstanceFromKGv3(DataStage.IN_PROGRESS, true, id);
            return ResponseEntity.ok(Map.of("_source", instance));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @GetMapping("/groups/public/documents/{id}")
    public ResponseEntity<?> getDocumentForPublic(@PathVariable("id") String id) {
        String index = ESHelper.getIndexesForDocument(DataStage.RELEASED);
        try {
            return ResponseEntity.ok(esServiceClient.getDocument(index, id));
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    private Map<String, Object> formatFilesResponse(ElasticSearchResult filesFromRepo) {
        Map<String, Object> result = new HashMap<>();
        if (filesFromRepo != null && filesFromRepo.getHits() != null && filesFromRepo.getHits().getHits() != null) {
            List<ElasticSearchDocument> hits = filesFromRepo.getHits().getHits();
            List<Object> data = hits.stream().map(e -> {
                if (e.getSource() != null) {
                    Map<String, Object> item = new HashMap<>();
                    Map<String, Object> source = e.getSource();
                    item.put("value", source.get("name"));
                    item.put("url", source.get("iri"));
                    if (source.get("size") != null) {
                        item.put("fileSize", source.get("size"));
                    }
                    if (source.get("format") != null) {
                        item.put("format", source.get("format"));
                    }
                    return item;
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            ElasticSearchResult.Total total = filesFromRepo.getHits().getTotal();
            result.put("total", total.getValue());
            result.put("data", data);
            if (hits.size() > 0) {
                result.put("searchAfter", hits.get(hits.size() - 1).getId());
            }
        } else {
            result.put("total", 0);
            result.put("data", Collections.emptyList());
        }
        return result;
    }


    @GetMapping("/repositories/{id}/files/live")
    public ResponseEntity<?> getFilesFromRepoForPublic(@PathVariable("id") String repositoryId,
                                                       @RequestParam(required = false, defaultValue = "0", name = "from") int from,
                                                       @RequestParam(required = false, defaultValue = "10000", name = "size") int size) {
        if (!MetaModelUtils.isValidUUID(repositoryId) || from < 0 ||  size > 10000) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(searchController.getFilesForLive(repositoryId, from, size));
    }

    @GetMapping("/groups/public/repositories/{id}/files")
    public ResponseEntity<?> getFilesFromRepoForPublic(@PathVariable("id") String id,
                                                       @RequestParam(required = false, name = "searchAfter") String searchAfter,
                                                       @RequestParam(required = false, defaultValue = "10000", name = "size") int size,
                                                       Principal principal) {
        if ((searchAfter != null && !MetaModelUtils.isValidUUID(searchAfter)) || !MetaModelUtils.isValidUUID(id) || size > 10000) {
            return ResponseEntity.badRequest().build();
        }
        try {
            String index = ESHelper.getIndexesForDocument(DataStage.RELEASED);
            ElasticSearchDocument document = esServiceClient.getDocument(index, id);
            Map<String, Object> source = document.getSource();
            if (source == null) {
                return ResponseEntity.notFound().build();
            } else {
                Map<String, Object> typeValue = (Map<String, Object>) source.get("type");
                String type = typeValue != null?((String) typeValue.get("value")):null;
                if (StringUtils.isEmpty(type) || !type.equals("FileRepository")) {
                    return ResponseEntity.notFound().build();
                } else {
                    String embargo = (String) source.get("embargo");
                    String useHDG = (String) source.get("useHDG");
                    if (StringUtils.isNotEmpty(useHDG) || (!isInInProgressRole(principal) && StringUtils.isNotEmpty(embargo))) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    } else {
                        String fileType = utils.getNameForClass(File.class);
                        String fileIndex = ESHelper.getAutoReleasedIndex(fileType);
                        ElasticSearchResult filesFromRepo = esServiceClient.getFilesFromRepo(fileIndex, id, searchAfter, size);
                        Map<String, Object> result = formatFilesResponse(filesFromRepo);
                        return ResponseEntity.ok(result);
                    }
                }
            }
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @GetMapping("/groups/curated/repositories/{id}/files")
    public ResponseEntity<?> getFilesFromRepoForCurated(@PathVariable("id") String id,
                                                        @RequestParam(required = false, name = "searchAfter") String searchAfter,
                                                        @RequestParam(required = false, defaultValue = "10000", name = "size") int size,
                                                        Principal principal) {
        if (isInInProgressRole(principal)) {
            if ((searchAfter != null && !MetaModelUtils.isValidUUID(searchAfter)) || !MetaModelUtils.isValidUUID(id) || size > 10000) {
                return ResponseEntity.badRequest().build();
            }
            try {
                String index = ESHelper.getIndexesForDocument(DataStage.IN_PROGRESS);
                ElasticSearchDocument document = esServiceClient.getDocument(index, id);
                Map<String, Object> source = document.getSource();
                if (source == null) {
                    return ResponseEntity.notFound().build();
                } else {
                    Map<String, Object> typeValue = (Map<String, Object>) source.get("type");
                    String type = typeValue != null?((String) typeValue.get("value")):null;
                    if (StringUtils.isEmpty(type) || !type.equals("FileRepository")) {
                        return ResponseEntity.notFound().build();
                    } else {
                        String useHDG = (String) source.get("useHDG");
                        if (StringUtils.isNotEmpty(useHDG)) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                        } else {
                            String fileType = utils.getNameForClass(File.class);
                            String fileIndex = ESHelper.getAutoReleasedIndex(fileType);
                            ElasticSearchResult filesFromRepo = esServiceClient.getFilesFromRepo(fileIndex, id, searchAfter, size);
                            Map<String, Object> result = formatFilesResponse(filesFromRepo);
                            return ResponseEntity.ok(result);
                        }
                    }
                }
            } catch (WebClientResponseException e) {
                return ResponseEntity.status(e.getStatusCode()).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/groups/public/documents/{type}/{id}")
    public ResponseEntity<?> getDocumentForPublic(@PathVariable("type") String type, @PathVariable("id") String id) {
        String index = ESHelper.getIndexesForDocument(DataStage.RELEASED);
        try {
            return ResponseEntity.ok(esServiceClient.getDocument(index, String.format("%s/%s", type, id)));
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @GetMapping("/groups/curated/documents/{id}")
    public ResponseEntity<?> getDocumentForCurated(@PathVariable("id") String id, Principal principal) {
        if (isInInProgressRole(principal)) {
            try {
                String index = ESHelper.getIndexesForDocument(DataStage.IN_PROGRESS);
                return ResponseEntity.ok(esServiceClient.getDocument(index, id));
            } catch (WebClientResponseException e) {
                return ResponseEntity.status(e.getStatusCode()).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/groups/curated/documents/{type}/{id}")
    public ResponseEntity<?> getDocumentForCurated(@PathVariable("type") String type, @PathVariable("id") String id, Principal principal) {
        if (isInInProgressRole(principal)) {
            try {
                String index = ESHelper.getIndexesForDocument(DataStage.IN_PROGRESS);
                return ResponseEntity.ok(esServiceClient.getDocument(index, String.format("%s/%s", type, id)));
            } catch (WebClientResponseException e) {
                return ResponseEntity.status(e.getStatusCode()).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/groups/public/search")
    public ResponseEntity<?> searchPublic(@RequestBody String payload) throws JsonProcessingException {
        try {
            return ResponseEntity.ok(searchController.getResult(payload, DataStage.RELEASED));
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/groups/curated/search")
    public ResponseEntity<?> searchCurated(@RequestBody String payload, Principal principal) throws JsonProcessingException {
        if (isInInProgressRole(principal)) {
            try {
                return ResponseEntity.ok(searchController.getResult(payload, DataStage.IN_PROGRESS));
            } catch (WebClientResponseException e) {
                return ResponseEntity.status(e.getStatusCode()).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
