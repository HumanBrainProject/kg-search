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
import eu.ebrains.kg.search.controller.kg.KGv2;
import eu.ebrains.kg.search.controller.kg.KGv3;
import eu.ebrains.kg.search.controller.labels.LabelsController;
import eu.ebrains.kg.search.controller.search.SearchController;
import eu.ebrains.kg.search.controller.translators.TranslationController;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.TranslatorModel;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.services.ESServiceClient;
import eu.ebrains.kg.search.services.KGV2ServiceClient;
import eu.ebrains.kg.search.utils.ESHelper;
import eu.ebrains.kg.search.utils.MetaModelUtils;
import eu.ebrains.kg.search.utils.TranslationException;
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
public class Search {
    private final KGV2ServiceClient KGV2ServiceClient;
    private final ESServiceClient esServiceClient;
    private final LabelsController labelsController;
    private final SearchController searchController;
    private final TranslationController translationController;
    private final KGv2 kgV2;
    private final KGv3 kgV3;

    @Value("${eu.ebrains.kg.commit}")
    String commit;

    public Search(KGV2ServiceClient KGV2ServiceClient, ESServiceClient esServiceClient, LabelsController labelsController, SearchController searchController, TranslationController translationController, KGv2 kgV2, KGv3 kgV3) throws JsonProcessingException {
        this.KGV2ServiceClient = KGV2ServiceClient;
        this.esServiceClient = esServiceClient;
        this.labelsController = labelsController;
        this.searchController = searchController;
        this.translationController = translationController;
        this.kgV3 = kgV3;
        this.kgV2 = kgV2;
    }

    @GetMapping("/auth/endpoint")
    public Map<String, String> getAuthEndpoint() {
        Map<String, String> result = new HashMap<>();
        String authEndpoint = KGV2ServiceClient.getAuthEndpoint();
        result.put("authEndpoint", authEndpoint);
        return result;
    }

    @GetMapping("/labels")
    public Map<String, Object> getLabels() {
        String authEndpoint = KGV2ServiceClient.getAuthEndpoint();
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
                   return ResponseEntity.ok(Map.of("_source", v));
                }
            }
            translatorModel = TranslatorModel.MODELS.stream().filter(m -> m.getV1translator()!=null && m.getV1translator().getQueryIds().contains(queryId)).findFirst().orElse(null);
            if(translatorModel!=null){
                TargetInstance v = translationController.translateToTargetInstanceForLiveMode(kgV2, translatorModel.getV1translator(), queryId, DataStage.IN_PROGRESS, id, true, false);
                if(v!=null){
                    return ResponseEntity.ok(Map.of("_source", v));
                }
            }
            return ResponseEntity.notFound().build();
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @GetMapping("/{id}/live")
    public ResponseEntity<Map> translate(@PathVariable("id") String id) throws TranslationException {
        try {
            final List<String> typesOfInstance = kgV3.getTypesOfInstance(id, DataStage.IN_PROGRESS, false);
            final TranslatorModel<?, ?, ?, ?> translatorModel = TranslatorModel.MODELS.stream().filter(m -> m.getV3translator() != null && m.getV3translator().semanticTypes().stream().anyMatch(typesOfInstance::contains)).findFirst().orElse(null);
            if(translatorModel!=null) {
                final String queryId = typesOfInstance.stream().map(type -> translatorModel.getV3translator().getQueryIdByType(type)).findFirst().orElse(null);
                final TargetInstance v = translationController.translateToTargetInstanceForLiveMode(kgV3, translatorModel.getV3translator(), queryId, DataStage.IN_PROGRESS, id, false, true);
                if(v!=null) {
                    return queryId == null ? null : ResponseEntity.ok(Map.of("_source", v));
                }
            }
            return ResponseEntity.notFound().build();
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


    @GetMapping("/repositories/{id}/files/live")
    public ResponseEntity<?> getFilesFromRepoForLive(@PathVariable("id") String repositoryId,
                                                     @RequestParam(required = false, name = "searchAfter") String searchAfter,
                                                     @RequestParam(required = false, defaultValue = "10000", name = "size") int size,
                                                     @RequestParam(required = false, name = "format") String format,
                                                     @RequestParam(required = false, name = "fileBundle") String fileBundle,
                                                       Principal principal) {
        if (searchController.isInInProgressRole(principal)) {
            if ((searchAfter != null && !MetaModelUtils.isValidUUID(searchAfter)) || !MetaModelUtils.isValidUUID(repositoryId) || size > 10000) {
                return ResponseEntity.badRequest().build();
            }
            try {
                //FIXME fix the files for live mechanism
                //kgV3.fetchInstance(repositoryId, DataStage.IN_PROGRESS);
                return searchController.getFilesFromRepo(DataStage.IN_PROGRESS, repositoryId, searchAfter, size, format, fileBundle);
                
            } catch (WebClientResponseException e) {
                return ResponseEntity.status(e.getStatusCode()).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/groups/public/repositories/{id}/fileFormats")
    public ResponseEntity<?> getFileFormatsFromRepoForPublic(@PathVariable("id") String id) {
        return searchController.getFileFormatsFromRepo(DataStage.RELEASED, id);
    }

    @GetMapping("/groups/curated/repositories/{id}/fileFormats")
    public ResponseEntity<?> getFileFormatsFromRepoForCurated(@PathVariable("id") String id,
                                                        Principal principal) {
        if (searchController.isInInProgressRole(principal)) {
            return searchController.getFileFormatsFromRepo(DataStage.IN_PROGRESS, id);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/groups/public/repositories/{id}/fileBundles")
    public ResponseEntity<?> getFileBundlesFromRepoForPublic(@PathVariable("id") String id) {
        return searchController.getFileBundlesFromRepo(DataStage.RELEASED, id);
    }

    @GetMapping("/groups/curated/repositories/{id}/fileBundles")
    public ResponseEntity<?> getFileBundlesFromRepoForCurated(@PathVariable("id") String id,
                                                        Principal principal) {
        if (searchController.isInInProgressRole(principal)) {
            return searchController.getFileBundlesFromRepo(DataStage.IN_PROGRESS, id);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/groups/public/repositories/{id}/files")
    public ResponseEntity<?> getFilesFromRepoForPublic(@PathVariable("id") String id,
                                                       @RequestParam(required = false, defaultValue = "", name = "searchAfter") String searchAfter,
                                                       @RequestParam(required = false, defaultValue = "10000", name = "size") int size,
                                                       @RequestParam(required = false, defaultValue = "", name = "format") String format,
                                                       @RequestParam(required = false, defaultValue = "", name = "fileBundle") String fileBundle) {
        if ((StringUtils.isNotBlank(searchAfter) && !MetaModelUtils.isValidUUID(searchAfter)) || !MetaModelUtils.isValidUUID(id) || size > 10000) {
            return ResponseEntity.badRequest().build();
        }
        return searchController.getFilesFromRepo(DataStage.RELEASED, id, searchAfter, size, format, fileBundle);
    }

    @GetMapping("/groups/curated/repositories/{id}/files")
    public ResponseEntity<?> getFilesFromRepoForCurated(@PathVariable("id") String id,
                                                        @RequestParam(required = false, defaultValue = "", name = "searchAfter") String searchAfter,
                                                        @RequestParam(required = false, defaultValue = "10000", name = "size") int size,
                                                        @RequestParam(required = false, defaultValue = "", name = "format") String format,
                                                        @RequestParam(required = false, defaultValue = "", name = "bundle") String bundle,
                                                        Principal principal) {
        if (searchController.isInInProgressRole(principal)) {
            if ((StringUtils.isNotBlank(searchAfter) && !MetaModelUtils.isValidUUID(searchAfter)) || !MetaModelUtils.isValidUUID(id) || size > 10000) {
                return ResponseEntity.badRequest().build();
            }
            return searchController.getFilesFromRepo(DataStage.IN_PROGRESS, id, searchAfter, size, format, bundle);
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
        if (searchController.isInInProgressRole(principal)) {
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
        if (searchController.isInInProgressRole(principal)) {
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
        if (searchController.isInInProgressRole(principal)) {
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
