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

package eu.ebrains.kg.search.api;

import eu.ebrains.kg.common.controller.kg.KG;
import eu.ebrains.kg.common.controller.translation.TranslationController;
import eu.ebrains.kg.common.controller.translation.TranslatorRegistry;
import eu.ebrains.kg.common.controller.translation.models.TranslatorModel;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.target.TargetInstance;
import eu.ebrains.kg.common.services.DOICitationFormatter;
import eu.ebrains.kg.common.services.KGServiceClient;
import eu.ebrains.kg.common.utils.MetaModelUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.search.controller.search.SearchController;
import eu.ebrains.kg.search.controller.settings.SettingsController;
import eu.ebrains.kg.search.model.FacetValue;
import eu.ebrains.kg.search.security.UserRoles;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;

@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@SuppressWarnings("java:S1452") // we keep the generics intentionally
public class Search {
    private final KGServiceClient kgv3ServiceClient;
    private final SettingsController definitionController;
    private final SearchController searchController;
    private final TranslationController translationController;
    private final KG kgV3;
    private final DOICitationFormatter doiCitationFormatter;
    private final TranslatorRegistry translatorRegistry;

    public Search(KGServiceClient kgv3ServiceClient, SettingsController definitionController, SearchController searchController, TranslationController translationController, KG kgV3, DOICitationFormatter doiCitationFormatter, TranslatorRegistry translatorRegistry) {
        this.kgv3ServiceClient = kgv3ServiceClient;
        this.definitionController = definitionController;
        this.searchController = searchController;
        this.translationController = translationController;
        this.kgV3 = kgV3;
        this.doiCitationFormatter = doiCitationFormatter;
        this.translatorRegistry = translatorRegistry;
    }

    @GetMapping("/settings")
    public Map<String, Object> getSettings(
            @Value("${eu.ebrains.kg.commit}") String commit,
            @Value("${keycloak.realm}") String keycloakRealm,
            @Value("${keycloak.resource}") String keycloakClientId,
            @Value("${sentry.dsn.ui}") String sentryDsnUi,
            @Value("${sentry.environment}") String sentryEnvironment,
            @Value("${matomo.url}") String matomoUrl,
            @Value("${matomo.siteId}") String matomoSiteId
    ) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isNotBlank(commit) && !commit.equals("\"\"")) {
            result.put("commit", commit);

            // Only provide sentry when commit is available, ie on deployed env
            if (StringUtils.isNotBlank(sentryDsnUi)) {
                result.put("sentry", Map.of(
                        "dsn", sentryDsnUi,
                        "release", commit,
                        "environment", sentryEnvironment
                ));
            }
        }

        String authEndpoint = kgv3ServiceClient.getAuthEndpoint();
        if (StringUtils.isNotBlank(authEndpoint)) {
            result.put("keycloak", Map.of(
                    "realm", keycloakRealm,
                    "url", authEndpoint,
                    "clientId", keycloakClientId
            ));
        }
        if (StringUtils.isNotBlank(matomoUrl) && StringUtils.isNotBlank(matomoSiteId)) {
            result.put("matomo", Map.of(
                    "url", matomoUrl,
                    "siteId", matomoSiteId
            ));
        }
        result.put("types", definitionController.generateTypes());
        result.put("typeMappings", definitionController.generateTypeMappings());
        return result;
    }

    @GetMapping("/citation")
    public ResponseEntity<String> getCitation(@RequestParam("doi") String doiWithoutPrefix, @RequestParam("style") String style, @RequestParam("contentType") String contentType) {
        String doi = String.format("https://doi.org/%s", doiWithoutPrefix);
        String citation = doiCitationFormatter.getDOICitation(doi, style, contentType);
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(citation);
    }

    @GetMapping("/groups")
    public ResponseEntity<?> getGroups(JwtAuthenticationToken token) {
        if (UserRoles.hasInProgressRole(token)) {
            return ResponseEntity.ok(Arrays.asList(
                    Map.of("value", "curated",
                            "label", "in progress"),
                    Map.of("value", "public",
                            "label", "publicly released")
            ));
        } else {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }


    @SuppressWarnings("java:S3740") // we keep the generics intentionally
    @GetMapping("/{id}/live")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map> translate(@PathVariable("id") String id, @RequestParam(required = false, defaultValue = "false") boolean skipReferenceCheck) throws TranslationException {
        try {
            final List<String> typesOfInstance = kgV3.getTypesOfInstance(id, DataStage.IN_PROGRESS, false);
            final TranslatorModel<?, ?> translatorModel = this.translatorRegistry.getTranslators().stream().filter(m -> m.getTranslator() != null && m.getTranslator().semanticTypes().stream().anyMatch(typesOfInstance::contains)).findFirst().orElse(null);
            if (translatorModel != null) {
                final String queryId = typesOfInstance.stream().map(type -> translatorModel.getTranslator().getQueryIdByType(type)).findFirst().orElse(null);
                if (queryId != null) {
                    final TargetInstance v = translationController.translateToTargetInstanceForLiveMode(kgV3, translatorModel.getTranslator(), queryId, DataStage.IN_PROGRESS, id, false, !skipReferenceCheck);
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

    private boolean canReadLiveFiles(JwtAuthenticationToken token, UUID repositoryUUID){
        return UserRoles.hasInProgressRole(token) || searchController.isInvitedForFileRepository(repositoryUUID);
    }


    @GetMapping("/repositories/{id}/files/live")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFilesFromRepoForLive(@PathVariable("id") String repositoryId,
                                                     @RequestParam(required = false, defaultValue = "", name = "format") String format,
                                                     @RequestParam(required = false, defaultValue = "", name = "groupingType") String groupingType, JwtAuthenticationToken token) {
        final UUID repositoryUUID = MetaModelUtils.castToUUID(repositoryId);
        if (repositoryUUID == null) {
            return ResponseEntity.badRequest().build();
        }
        if (canReadLiveFiles(token, repositoryUUID)) {
            try {
                return searchController.getFilesFromRepo(DataStage.IN_PROGRESS, repositoryUUID, format, groupingType);
            } catch (WebClientResponseException e) {
                return ResponseEntity.status(e.getStatusCode()).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/repositories/{id}/files/formats/live")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFileFormatsFromRepoForLive(@PathVariable("id") String repositoryId, JwtAuthenticationToken token) {
        final UUID repositoryUUID = MetaModelUtils.castToUUID(repositoryId);
        if (repositoryUUID == null) {
            return ResponseEntity.badRequest().build();
        }
        if (canReadLiveFiles(token, repositoryUUID)) {
            return searchController.getFileFormatsFromRepo(DataStage.IN_PROGRESS, repositoryUUID);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/repositories/{id}/files/groupingTypes/live")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getGroupingTypesFromRepoForLive(@PathVariable("id") String repositoryId,
                                                             JwtAuthenticationToken token) {
        final UUID repositoryUUID = MetaModelUtils.castToUUID(repositoryId);
        if (repositoryUUID == null) {
            return ResponseEntity.badRequest().build();
        }
        if (canReadLiveFiles(token, repositoryUUID)) {
            //kgV3.executeQueryForInstance("clazz", DataStage.IN_PROGRESS, "queryId", repositoryId, false)
            return searchController.getGroupingTypesFromRepo(DataStage.IN_PROGRESS, repositoryUUID);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/groups/public/repositories/{id}/files/formats")
    public ResponseEntity<?> getFileFormatsFromRepoForPublic(@PathVariable("id") String repositoryId) {
        final UUID repositoryUUID = MetaModelUtils.castToUUID(repositoryId);
        if (repositoryUUID == null) {
            return ResponseEntity.badRequest().build();
        }
        return searchController.getFileFormatsFromRepo(DataStage.RELEASED, repositoryUUID);
    }

    @GetMapping("/groups/curated/repositories/{id}/files/formats")
    @UserRoles.MustBeInProgress
    public ResponseEntity<?> getFileFormatsFromRepoForCurated(@PathVariable("id") String repositoryId) {
        final UUID repositoryUUID = MetaModelUtils.castToUUID(repositoryId);
        if (repositoryUUID == null) {
            return ResponseEntity.badRequest().build();
        }
        return searchController.getFileFormatsFromRepo(DataStage.IN_PROGRESS, repositoryUUID);
    }

    @GetMapping("/groups/public/repositories/{id}/files/groupingTypes")
    public ResponseEntity<?> getGroupingTypesFromRepoForPublic(@PathVariable("id") String repositoryId) {
        final UUID repositoryUUID = MetaModelUtils.castToUUID(repositoryId);
        if (repositoryUUID == null) {
            return ResponseEntity.badRequest().build();
        }
        return searchController.getGroupingTypesFromRepo(DataStage.RELEASED, repositoryUUID);
    }

    @GetMapping("/groups/curated/repositories/{id}/files/groupingTypes")
    @UserRoles.MustBeInProgress
    public ResponseEntity<?> getGroupingTypesFromRepoForCurated(@PathVariable("id") String repositoryId) {
        final UUID repositoryUUID = MetaModelUtils.castToUUID(repositoryId);
        if (repositoryUUID == null) {
            return ResponseEntity.badRequest().build();
        }
        return searchController.getGroupingTypesFromRepo(DataStage.IN_PROGRESS, repositoryUUID);
    }

    @GetMapping("/groups/public/repositories/{id}/files")
    public ResponseEntity<?> getFilesFromRepoForPublic(@PathVariable("id") String id,
                                                       @RequestParam(required = false, defaultValue = "", name = "format") String format,
                                                       @RequestParam(required = false, defaultValue = "", name = "groupingType") String groupingType) {
        final UUID uuid = MetaModelUtils.castToUUID(id);
        if (uuid == null) {
            return ResponseEntity.badRequest().build();
        }
        return searchController.getFilesFromRepo(DataStage.RELEASED, uuid, format, groupingType);
    }

    @GetMapping("/groups/curated/repositories/{id}/files")
    @UserRoles.MustBeInProgress
    public ResponseEntity<?> getFilesFromRepoForCurated(@PathVariable("id") String id,
                                                        @RequestParam(required = false, defaultValue = "", name = "format") String format,
                                                        @RequestParam(required = false, defaultValue = "", name = "groupingType") String groupingType) {
        final UUID uuid = MetaModelUtils.castToUUID(id);
        if (uuid == null) {
            return ResponseEntity.badRequest().build();
        }
        return searchController.getFilesFromRepo(DataStage.IN_PROGRESS, uuid, format, groupingType);
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
    @UserRoles.MustBeInProgress
    public ResponseEntity<?> getDocumentForCurated(@PathVariable("id") String id) {
        try {
            Map<String, Object> res = searchController.getSearchDocument(DataStage.IN_PROGRESS, id);
            if (res != null) {
                return ResponseEntity.ok(res);
            }
            return ResponseEntity.notFound().build();
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @GetMapping("/groups/curated/documents/{type}/{id}")
    @UserRoles.MustBeInProgress
    public ResponseEntity<?> getDocumentForCurated(@PathVariable("type") String type, @PathVariable("id") String id) {
        try {
            Map<String, Object> res = searchController.getSearchDocument(DataStage.IN_PROGRESS, String.format("%s/%s", type, id));
            if (res != null) {
                return ResponseEntity.ok(res);
            }
            return ResponseEntity.notFound().build();
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/groups/public/search")
    public ResponseEntity<?> searchPublic(
            @RequestParam(required = false, defaultValue = "", name = "q") String q,
            @RequestParam(required = false, defaultValue = "", name = "type") String type,
            @RequestParam(required = false, defaultValue = "0", name = "from") int from,
            @RequestParam(required = false, defaultValue = "20", name = "size") int size,
            @RequestBody Map<String, FacetValue> facetValues
    ) {
        try {
            Map<String, Object> result = searchController.search(q, type, from, size, facetValues, DataStage.RELEASED);
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/groups/curated/search")
    @UserRoles.MustBeInProgress
    public ResponseEntity<?> searchCurated(
            @RequestParam(required = false, defaultValue = "", name = "q") String q,
            @RequestParam(required = false, defaultValue = "", name = "type") String type,
            @RequestParam(required = false, defaultValue = "0", name = "from") int from,
            @RequestParam(required = false, defaultValue = "20", name = "size") int size,
            @RequestBody Map<String, FacetValue> facetValues) {
        try {
            Map<String, Object> result = searchController.search(q, type, from, size, facetValues, DataStage.IN_PROGRESS);
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @GetMapping("/{id}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> userBookmarkedInstance(@PathVariable("id") String id) {
        UUID uuid = MetaModelUtils.castToUUID(id);
        if (uuid == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            Map<String, Object> result = Map.of(
                    "id", id,
                    "bookmarked", searchController.isBookmarked(uuid)
            );
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @DeleteMapping("/{id}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> userDeleteBookmarkOfInstance(@PathVariable("id") String id) {
        UUID uuid = MetaModelUtils.castToUUID(id);
        if (uuid == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            searchController.deleteBookmark(uuid);
            Map<String, Object> result = Map.of(
                    "id", id,
                    "bookmarked", false
            );
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PutMapping("/{id}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> userBookmarkInstance(@PathVariable("id") String id) {
        UUID uuid = MetaModelUtils.castToUUID(id);
        if (uuid == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            searchController.addBookmark(MetaModelUtils.castToUUID(id));
            Map<String, Object> result = Map.of(
                    "id", id,
                    "bookmarked", true
            );
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }
}
