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

package eu.ebrains.kg.search.controller.search;

import eu.ebrains.kg.common.controller.kg.KGv3;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.elasticsearch.Bucket;
import eu.ebrains.kg.common.model.elasticsearch.Document;
import eu.ebrains.kg.common.model.elasticsearch.Result;
import eu.ebrains.kg.common.model.elasticsearch.Suggestion;
import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.File;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.HasPreviews;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.VersionedInstance;
import eu.ebrains.kg.common.services.ESServiceClient;
import eu.ebrains.kg.common.utils.ESHelper;
import eu.ebrains.kg.common.utils.MetaModelUtils;
import eu.ebrains.kg.search.controller.authentication.UserInfoRoles;
import eu.ebrains.kg.search.controller.facets.FacetsController;
import eu.ebrains.kg.search.model.Facet;
import eu.ebrains.kg.search.model.FacetValue;
import eu.ebrains.kg.search.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@SuppressWarnings("java:S1452") // we keep the generics intentionally
public class SearchController extends FacetAggregationUtils {
;
    private final ESServiceClient esServiceClient;
    private final UserInfoRoles userInfoRoles;
    private final FacetsController facetsController;
    private final SearchFieldsController searchFieldsController;
    private final MetaModelUtils utils;

    private final KGv3 kg;

    private final static String TOTAL = "total";

    public SearchController(
            ESServiceClient esServiceClient,
            UserInfoRoles userInfoRoles,
            FacetsController facetsController,
            SearchFieldsController searchFieldsController,
            MetaModelUtils utils,
            KGv3 kg
    ) {
        this.esServiceClient = esServiceClient;
        this.userInfoRoles = userInfoRoles;
        this.facetsController = facetsController;
        this.searchFieldsController = searchFieldsController;
        this.utils = utils;
        this.kg = kg;
    }

    public boolean isKGDev(Principal principal) {
        return userInfoRoles.isInAnyOfRoles((KeycloakAuthenticationToken) principal, "group", "group-kg-devs");
    }

    public boolean isInInProgressRole(Principal principal) {
        return userInfoRoles.isInAnyOfRoles((KeycloakAuthenticationToken) principal, "team", "collab-kg-search-in-progress-administrator", "collab-kg-search-in-progress-editor", "collab-kg-search-in-progress-viewer");
    }

    public boolean canReadLiveFiles(Principal principal, UUID fileRepositoryId){
        return isInInProgressRole(principal) || isInvitedForFileRepository(fileRepositoryId);
    }

    public boolean isInvitedForFileRepository(UUID fileRepositoryId){
        final Set<UUID> invitationsFromKG = kg.getInvitationsFromKG();
        return invitationsFromKG.contains(fileRepositoryId);
    }


    private Map<String, Object> formatFileAggregation(Result esResult, String aggregation) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isNotBlank(aggregation) &&
                esResult != null &&
                esResult.getAggregations() != null &&
                esResult.getAggregations().containsKey(aggregation) &&
                esResult.getAggregations().get(aggregation) != null &&
                esResult.getAggregations().get(aggregation).getBuckets() != null) {

            List<String> formats = esResult.getAggregations().get(aggregation).getBuckets().stream()
                    .map(Bucket::getKey)
                    .filter(Objects::nonNull)
                    .sorted()
                    .collect(Collectors.toList());

            result.put(TOTAL, formats.size());
            result.put("data", formats);
        } else {
            result.put(TOTAL, 0);
            result.put("data", Collections.emptyList());
        }
        return result;
    }

    private ResponseEntity<?> getFileAggregationFromRepo(DataStage stage, UUID id, String field) {
        try {
            String fileIndex = ESHelper.getAutoReleasedIndex(stage, File.class, false);
            Map<String, String> aggs = Map.of("patterns", field);
            Result esResult = esServiceClient.getFilesAggregationsFromRepo(fileIndex, id, aggs);
            Map<String, Object> result = formatFileAggregation(esResult, "patterns");
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    public ResponseEntity<?> getGroupingTypesFromRepo(DataStage stage, UUID id) {
        return getFileAggregationFromRepo(stage, id, "groupingTypes.name.keyword");
    }

    public ResponseEntity<?> getFileFormatsFromRepo(DataStage stage, UUID id) {
        return getFileAggregationFromRepo(stage, id, "format.value.keyword");
    }

    public ResponseEntity<?> getFilesFromRepo(DataStage stage, UUID id, String format, String groupingType) {
        List<Object> data = new ArrayList<>();
        UUID searchAfterUUID = null;
        int total = 0;
        boolean isFirstRequest = true;
        String fileIndex = ESHelper.getAutoReleasedIndex(stage, File.class, false);
        while (isFirstRequest || searchAfterUUID != null) {
            isFirstRequest = false;
            try {
                Result filesFromRepo = esServiceClient.getFilesFromRepo(fileIndex, id, searchAfterUUID, 10000, format, groupingType);
                List<Document> hits = filesFromRepo.getHits().getHits();
                List<Object> paginatedData = hits.stream().map(Document::getSource).filter(Objects::nonNull).collect(Collectors.toList());
                data.addAll(paginatedData);
                if (!CollectionUtils.isEmpty(hits)) {
                    searchAfterUUID = MetaModelUtils.castToUUID(hits.get(hits.size() - 1).getId());
                } else {
                    searchAfterUUID = null;
                }
                total = filesFromRepo.getHits().getTotal().getValue();
            } catch (WebClientResponseException e) {
                return ResponseEntity.status(e.getStatusCode()).build();
            }
        }
        Map<String, Object> result = Map.of(TOTAL, total, "data", data);
        return ResponseEntity.ok(result);
    }


    public Map<String, Object> search(String q, String type, int from, int size, Map<String, FacetValue> facetValues, DataStage dataStage) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("from", from);
        payload.put("size", size);
        Map<String,Object> esHighlight = getEsHighlight(type);
        if (esHighlight != null) {
            payload.put("highlight", esHighlight);
        }
        Type targetClass = utils.getTypeTargetClass(type);
        MetaInfo metaInfo = null;
        if (targetClass != null) {
            metaInfo = ((Class<?>) targetClass).getAnnotation(MetaInfo.class);
        }
        payload.put("sort", getEsSort(metaInfo, StringUtils.isNotBlank(q)));
        List<Facet> facets = facetsController.getFacets(type);
        Map<String, Object> activeFilters = FiltersUtils.getActiveFilters(facets, type, facetValues);
        Object esPostFilter = FiltersUtils.getFilter(activeFilters, null);
        payload.put("post_filter", esPostFilter);
        Object esAggs = AggsUtils.getAggs(facets, activeFilters, facetValues);
        payload.put("aggs", esAggs);
        List<String> sanitizedQuery = QueryStringUtils.sanitizeQueryString(q);
        final Object query = getEsQuery(QueryStringUtils.prepareQuery(sanitizedQuery), type);
        if (query != null) {
            payload.put("query", query);
        }
        String index = ESHelper.getIndexesForSearch(dataStage);
        Result result = esServiceClient.searchDocuments(index, payload);
        int total = (result.getHits() != null && result.getHits().getTotal() != null) ? result.getHits().getTotal().getValue() : 0;
        Map<String, Object> response = new HashMap<>();
        response.put("total", total);
        response.put("hits", getHits(result, type, dataStage, metaInfo));
        response.put("aggregations", getFacetAggregation(facets, result.getAggregations(), facetValues, total != 0));
        response.put("types", getTypesAggregation(result.getAggregations()));
        response.put("suggestions", getSuggestions(sanitizedQuery, dataStage, type));

        return response;
    }

    private String getGroup(DataStage dataStage) {
        return dataStage == DataStage.IN_PROGRESS ? "curated" : "public";
    }


    private List<Map<String, Object>> getHits(Result result, String type, DataStage dataStage, MetaInfo metaInfo) {
        if (result.getHits() == null || result.getHits().getHits() == null) {
            return Collections.emptyList();
        }
        List<String> fieldNames = getHitFieldNames(type);
        return result.getHits().getHits().stream().map(h -> {
            Map<String, Object> source = h.getSource();
            Map<String, Object> hit = new HashMap<>();
            hit.put("id", source.get("id"));
            hit.put("type", type); // getValueField(source, "type")
            hit.put("group", getGroup(dataStage));
            hit.put("category", CastingUtils.getStringValueField(source, "category"));
            hit.put("title", CastingUtils.getStringValueField(source, "title"));
            hit.put("badges", source.get("badges"));
            hit.put("tags", source.get("tags"));
            if (h.getHighlight() != null) {
                hit.put("highlight", h.getHighlight());
            }
            String previewImage = getPreviewImage(source);
            if (StringUtils.isNotBlank(previewImage)) {
                hit.put("previewImage", previewImage);
            }
            hit.put("fields", getFields(source, fieldNames));
            return hit;
        }).collect(Collectors.toList());
    }

    private String getPreviewImage(Map<String, Object> source) {
        List<Map<String, Object>> previews = getPreviews(source);
        if (!CollectionUtils.isEmpty(previews)) {
            Optional<Map<String, Object>> preview = previews.stream()
                    .filter(o -> o.containsKey("staticImageUrl"))
                    .findFirst();
            if (preview.isPresent()) {
                return CastingUtils.getStringField(preview.get(), "staticImageUrl");
            }
        }
        return null;
    }


    public Map<String, Object> getSearchDocument(DataStage dataStage, String id) throws WebClientResponseException {
        String index = ESHelper.getIndexesForDocument(dataStage);
        Document doc = esServiceClient.getDocument(index, id);
        if (doc == null || doc.getSource() == null) {
            return null;
        }

        Map<String, Object> source = doc.getSource();
        String type = CastingUtils.getStringValueField(source, "type");
        Map<String, Object> res = new HashMap<>();
        res.put("id", source.get("id"));
        res.put("type", type);
        res.put("group", getGroup(dataStage));
        res.put("category", CastingUtils.getStringValueField(source, "category"));
        res.put("title", CastingUtils.getStringValueField(source, "title"));
        res.put("badges", source.get("badges"));

        String disclaimer = CastingUtils.getStringValueField(source, "disclaimer");
        if (StringUtils.isNotBlank(disclaimer)) {
            res.put("disclaimer", disclaimer);
        }

        Object meta = source.get("meta");
        if (meta!=null) {
            res.put("meta", meta);
            source.remove("meta");
        }


        String version = CastingUtils.getStringField(source, "version");
        if (StringUtils.isNotBlank(version)) {
            res.put("version", version);
        }

        List<Object> versions = CastingUtils.getListField(source, "versions");
        if (!CollectionUtils.isEmpty(versions)) {
            res.put("versions", versions);
        }

        if (source.get("allVersionRef") != null) {
            res.put("allVersionRef", source.get("allVersionRef"));
        }

        List<Map<String, Object>> previews = getPreviews(source);
        if (!CollectionUtils.isEmpty(previews)) {
            res.put("previews", previews);
        }

        List<String> fieldNames = getDocumentFieldNames(type);
        res.put("fields", getFields(source, fieldNames));
        return res;
    }

    private List<Map<String, Object>> getPreviews(Map<String, Object> source) {
        List<Map<String, Object>> previews = new ArrayList<>();
        if (source.containsKey("previewObjects")) {
            try {
                List<Map<String, Object>> previewObjects = (List<Map<String, Object>>) source.get("previewObjects");
                if (!CollectionUtils.isEmpty(previewObjects)) {
                    previewObjects.forEach(o -> {
                        Map<String, Object> preview = getPreviewFromPreviewObject(o);
                        if (!CollectionUtils.isEmpty(preview)) {
                            previews.add(preview);
                        }
                    });
                }

            } catch (ClassCastException ignored) {
            }
        }
        if (source.containsKey("filesOld")) {
            try {
                List<Map<String, Object>> oldFiles = (List<Map<String, Object>>) source.get("filesOld");
                if (!CollectionUtils.isEmpty(oldFiles)) {
                    oldFiles.forEach(o -> {
                        Map<String, Object> preview = getPreviewFromOldFile(o);
                        if (!CollectionUtils.isEmpty(preview)) {
                            previews.add(preview);
                        }
                    });
                }

            } catch (ClassCastException ignored) {
            }
        }
        return previews;
    }

    private Map<String, Object> getPreviewFromPreviewObject(Map<String, Object> previewObject) {
        String description = CastingUtils.getStringField(previewObject, "description");
        String imageUrl = CastingUtils.getStringField(previewObject, "imageUrl");
        String videoUrl = CastingUtils.getStringField(previewObject, "videoUrl");
        Object link = previewObject.get("link");
        return getPreview(description, link, imageUrl, videoUrl, true);
    }

    @Deprecated(forRemoval = true)
    private  Map<String, Object> getPreviewFromOldFile(Map<String, Object> oldFile) {
        Map<String, Object> preview = new HashMap<>();
        String value = CastingUtils.getStringField(oldFile, "value");
        String staticImageUrl = CastingUtils.getObjectFieldStringProperty(oldFile, "staticImageUrl", "url");
        String previewUrl = CastingUtils.getObjectFieldStringProperty(oldFile, "previewUrl", "url");
        boolean isAnimated = CastingUtils.getObjectFieldBooleanProperty(oldFile, "previewUrl", "isAnimated");
        return getPreview(value, null, staticImageUrl, previewUrl, StringUtils.isNotBlank(previewUrl) && isAnimated);
    }

    public Map<String, Object> getLiveDocument(TargetInstance v) {
        if (v == null) {
            return null;
        }
        Map<String, Object> res = new HashMap<>();
        res.put("id", v.getId());
        if (v.getType() != null && StringUtils.isNotBlank(v.getType().getValue())) {
            res.put("type", v.getType().getValue());
        }
        if (v.getCategory() != null && StringUtils.isNotBlank(v.getCategory().getValue())) {
            res.put("category", v.getCategory().getValue());
        }
        if (v.getTitle() != null && StringUtils.isNotBlank(v.getTitle().getValue())) {
            res.put("title", v.getTitle().getValue());
        }

        if (v.getDisclaimer() != null && StringUtils.isNotBlank(v.getDisclaimer().getValue())) {
            res.put("disclaimer", v.getDisclaimer().getValue());
        }

        if (v.getMeta() != null) {
            res.put("meta", v.getMeta());
            v.setMeta(null);
        }

        if (v instanceof VersionedInstance) {
            VersionedInstance versioned = (VersionedInstance) v;

            if (StringUtils.isNotBlank(versioned.getVersion())) {
                res.put("version", versioned.getVersion());
            }
            if (!CollectionUtils.isEmpty(versioned.getVersions())) {
                res.put("versions", versioned.getVersions());
            }
            if (versioned.getAllVersionRef() != null) {
                res.put("allVersionRef", versioned.getAllVersionRef());
            }
        }

        if (v instanceof HasPreviews) {
            HasPreviews hasPreviews = (HasPreviews) v;
            List<Map<String, Object>> previews = new ArrayList<>();
            if (!CollectionUtils.isEmpty(hasPreviews.getPreviewObjects())) {
                hasPreviews.getPreviewObjects().forEach(o -> {
                    previews.add(getPreview(o.getDescription(), o.getLink(), o.getImageUrl(), o.getVideoUrl(), true));
                });
            };
            if (!CollectionUtils.isEmpty(previews)) {
                res.put("previews", previews);
            }
        }

        res.put("fields", v);
        return res;
    }

    private Map<String, Object> getPreview(String label, Object link, String imageUrl, String videoUrl, boolean isVideoAnimated) {
        Map<String, Object> preview = new HashMap<>();
        preview.put("label", label);
        if (link != null) {
            preview.put("link", link);
        }
        if (StringUtils.isNotBlank(imageUrl)) {
            preview.put("staticImageUrl", imageUrl);
        }
        if (StringUtils.isNotBlank(videoUrl) || StringUtils.isNotBlank(imageUrl)) {
            boolean isAnimated = isVideoAnimated && StringUtils.isNotBlank(videoUrl);
            preview.put("previewUrl", Map.of(
                    "url", isAnimated?videoUrl:imageUrl,
                    "isAnimated", isAnimated
            ));
        }
        return preview;
    }

    private Map<String, Object> getFields(Map<String, Object> source, List<String> fieldNames) {
        Map<String, Object> fields = new HashMap<>();
        fieldNames.forEach(name -> {
            if (source.containsKey(name) && source.get(name) != null) {
                fields.put(name, source.get(name));
            }
        });
        return fields;
    }

    private List<String> getFieldNames(String type, Predicate<FieldInfo> predicate, List<String> except) {
        List<String> fieldNames = new ArrayList<>();

        Consumer<Field> collect = field -> {
            FieldInfo info = field.getAnnotation(FieldInfo.class);
            if (info != null && predicate.test(info)) {
                String fieldName = utils.getPropertyName(field);
                if (!except.contains(fieldName)) {
                    fieldNames.add(utils.getPropertyName(field));
                }
            }
        };

        utils.visitTypeFields(type, collect);
        return fieldNames;
    }

    private List<String> getDocumentFieldNames(String type) {
        return getFieldNames(type, FieldInfo::visible, List.of("title"));
    }

    private List<String> getHitFieldNames(String type) {
        return getFieldNames(type, FieldInfo::overview, List.of("title"));
    }

    private Map<String, String> getSuggestions(List<String> sanitizedQuery, DataStage dataStage, String type) {
        Map<String, String> result = new LinkedHashMap<>();
        if (!sanitizedQuery.isEmpty()) {
            final String query = String.join(" ", sanitizedQuery);
            String index = ESHelper.getSearchableIndex(dataStage, MetaModelUtils.getClassForType(type), false);
            Map<String, Object> payload = new HashMap<>();
            Map<String, Object> suggest = new HashMap<>();
            payload.put("suggest", suggest);
            List<String> fields = searchFieldsController.getSuggestionFields(type);
            for (String field : fields) {
                suggest.put(field, Map.of("text", query, "term", Map.of("field", field)));
            }
            final Result elasticSearchFacetsResult = esServiceClient.searchDocuments(index, payload);
            final Map<String, List<Suggestion>> suggestResult = elasticSearchFacetsResult.getSuggest();
            Map<String, Set<Suggestion.Option>> suggestionsPerTerm = new HashMap<>();
            if (suggestResult != null) {
                suggestResult.values().stream().flatMap(Collection::stream).forEach(s -> {
                    final Set<Suggestion.Option> options = suggestionsPerTerm.computeIfAbsent(s.getText(), k -> new HashSet<>());
                        options.addAll(s.getOptions());
                });
            }
            Set<String> handledTerms = new HashSet<>();
            final List<String> unescapedQuery = sanitizedQuery.stream().map(w -> w.replaceAll("\\\\", "").toLowerCase()).collect(Collectors.toList());
            final String unescapedQ = String.join(" ", unescapedQuery);
            suggestionsPerTerm.keySet().forEach(k -> {
                final Set<Suggestion.Option> options = suggestionsPerTerm.get(k);
                final List<Suggestion.Option> sortedOptions = options.stream()
                        .filter(o -> o != null && o.getText() != null)
                        .peek(o -> o.setText(o.getText().replaceAll("\\W+\\s?$", "")))
                        .filter(o -> !unescapedQuery.contains(o.getText()))
                        .sorted(Comparator.comparing(Suggestion.Option::getText)).collect(Collectors.toList());
                final List<Suggestion.Option> limitedOptions = sortedOptions.size() > 5 ? sortedOptions.subList(0, 5) : sortedOptions;
                limitedOptions.forEach(o -> {
                    if (!handledTerms.contains(o.getText())) {
                        handledTerms.add(o.getText());
                        result.put(o.getText(), unescapedQ.replaceAll(k, o.getText()));
                    }
                });
            });
        }
        return result;
    }


    private Map<String,Object> getEsQuery(String q, String type) {
        if (StringUtils.isBlank(q)) {
            return null;
        }
        Map<String,Object> queryString = new HashMap<>();
        queryString.put("lenient", true);
        queryString.put("analyze_wildcard", true);
        queryString.put("query", q);
        List<String> fields = searchFieldsController.getEsQueryFields(type);
        if (!CollectionUtils.isEmpty(fields)) {
            queryString.put("fields", fields);
        }
        return Map.of("query_string", queryString);
    }

    private Map<String,Object> getEsHighlight(String type) {
        List<String> highlights = searchFieldsController.getHighlight(type);
        if (CollectionUtils.isEmpty(highlights)) {
            return null;
        }
        Map<String, Object> fields = new HashMap<>();
        for (String h: highlights) {
            fields.put(h, Collections.emptyMap());
        }
        return Map.of(
                "encoder", "html",
                "fields", fields
        );
    }

    private List<Object> getEsSort(MetaInfo metaInfo, boolean forceSortByRelevance) {
        boolean sortByRelevance = forceSortByRelevance || metaInfo == null || metaInfo.sortByRelevance();

        List<Object> fields = new ArrayList<>();

        if (sortByRelevance) {
            fields.add(Map.of("_score", Map.of("order", "desc")));
            fields.add(Map.of("trending", Map.of("order", "desc", "missing", "_last", "unmapped_type", "boolean")));
            fields.add(Map.of("releasedDateForSorting.value", Map.of("order", "desc", "missing", "_last", "unmapped_type", "keyword")));
        } else {
            fields.add(Map.of(
                    "title.value.keyword", Map.of(
                            "order", "asc"
                    )
            ));
        }
        return fields;
    }
}
