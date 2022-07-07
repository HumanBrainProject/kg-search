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

package eu.ebrains.kg.search.controller.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.target.elasticsearch.*;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.File;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.HasPreviews;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.VersionedInstance;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.ISODateValue;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetFile;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@SuppressWarnings("java:S1452") // we keep the generics intentionally
public class SearchController  {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ESServiceClient esServiceClient;
    private final UserInfoRoles userInfoRoles;
    private final FacetsController facetsController;
    private final MetaModelUtils utils;

    private final SearchFieldsController searchFieldsController;

    private final static String TOTAL = "total";

    public SearchController(
            ESServiceClient esServiceClient,
            UserInfoRoles userInfoRoles,
            FacetsController facetsController,
            SearchFieldsController searchFieldsController,
            MetaModelUtils utils
    ) {
        this.esServiceClient = esServiceClient;
        this.userInfoRoles = userInfoRoles;
        this.facetsController = facetsController;
        this.searchFieldsController = searchFieldsController;
        this.utils = utils;
    }

    public boolean isInInProgressRole(Principal principal) {
        return userInfoRoles.isInAnyOfRoles((KeycloakAuthenticationToken) principal, "team", "collab-kg-search-in-progress-administrator", "collab-kg-search-in-progress-editor", "collab-kg-search-in-progress-viewer");
    }

    private Map<String, Object> formatFileAggregation(ElasticSearchFilesResult esResult, String aggregation) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isNotBlank(aggregation) &&
                esResult != null &&
                esResult.getAggregations() != null &&
                esResult.getAggregations().containsKey(aggregation) &&
                esResult.getAggregations().get(aggregation) != null &&
                esResult.getAggregations().get(aggregation).getBuckets() != null) {

            List<String> formats = esResult.getAggregations().get(aggregation).getBuckets().stream()
                    .map(bucket -> bucket.getKey())
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

    private Map<String, Object> formatFilesResponse(ElasticSearchResult filesFromRepo) {
        Map<String, Object> result = new HashMap<>();
        if (filesFromRepo != null && filesFromRepo.getHits() != null && filesFromRepo.getHits().getHits() != null) {
            List<ElasticSearchDocument> hits = filesFromRepo.getHits().getHits();
            List<Object> data = hits.stream().map(ElasticSearchDocument::getSource).filter(Objects::nonNull).collect(Collectors.toList());
            ElasticSearchResult.Total total = filesFromRepo.getHits().getTotal();
            result.put(TOTAL, total.getValue());
            result.put("data", data);
            if (!CollectionUtils.isEmpty(hits)) {
                result.put("searchAfter", hits.get(hits.size() - 1).getId());
            }
        } else {
            result.put(TOTAL, 0);
            result.put("data", Collections.emptyList());
        }
        return result;
    }

    private ResponseEntity<?> getFileAggregationFromRepo(DataStage stage, String id, String field) {
        try {
            String fileIndex = ESHelper.getAutoReleasedIndex(stage, File.class, false);
            Map<String, String> aggs = Map.of("patterns", field);
            ElasticSearchFilesResult esResult = esServiceClient.getFilesAggregationsFromRepo(fileIndex, id, aggs);
            Map<String, Object> result = formatFileAggregation(esResult, "patterns");
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    public ResponseEntity<?> getGroupingTypesFromRepo(DataStage stage, String id) {
        return getFileAggregationFromRepo(stage, id, "groupingTypes.name.keyword");
    }

    public ResponseEntity<?> getFileFormatsFromRepo(DataStage stage, String id) {
        return getFileAggregationFromRepo(stage, id, "format.value.keyword");
    }

    public ResponseEntity<?> getFilesFromRepo(DataStage stage, String id, String searchAfter, int size, String format, String groupingType) {
        try {
            String fileIndex = ESHelper.getAutoReleasedIndex(stage, File.class, false);
            ElasticSearchResult filesFromRepo = esServiceClient.getFilesFromRepo(fileIndex, id, searchAfter, size, format, groupingType);
            Map<String, Object> result = formatFilesResponse(filesFromRepo);
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }


    public Map<String, Object> search(String q, String type, int from, int size, Map<String, FacetValue> facetValues, DataStage dataStage) {
        String index = ESHelper.getIndexesForSearch(dataStage);
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
        Object query = getEsQuery(QueryStringUtils.prepareQuery(sanitizedQuery), type);
        if (query != null) {
            payload.put("query", query);
        }
        ElasticSearchFacetsResult result = esServiceClient.searchDocuments(index, payload);
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


    private List<Map<String, Object>> getHits(ElasticSearchFacetsResult result, String type, DataStage dataStage, MetaInfo metaInfo) {
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
            hit.put("category", CastingUtils.getValueField(source, "category"));
            hit.put("title", CastingUtils.getValueField(source, "title"));
            Map<String, Boolean> badges = getBadges(source, metaInfo);
            if (!CollectionUtils.isEmpty(badges)) {
                hit.put("badges", badges);
            }
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

    private Map<String, Boolean> getBadges(Map<String, Object> source, MetaInfo metaInfo) {
        if(metaInfo == null || !metaInfo.badges()) {
            return Collections.emptyMap();
        }
        Map<String, Boolean> badges = new HashMap<>();
        String firstRelease = CastingUtils.getValueField(source, "first_release");
        if (isNew(firstRelease)) {
            badges.put("isNew", true);
        }
        return badges;
    }


    private boolean isNew(String firstRelease) {
        if (StringUtils.isBlank(firstRelease)) {
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(ISODateValue.ISO_DATE_PATTERN);
            Date firstReleaseDate = sdf.parse(firstRelease);
            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.DAY_OF_MONTH, -32);
            Date oneMonthAgo = cal.getTime();
            return firstReleaseDate.after(oneMonthAgo);
        } catch (ParseException e) {
            return false;
        }
    }

    public Map<String, Object> getSearchDocument(DataStage dataStage, String id) throws WebClientResponseException {
        String index = ESHelper.getIndexesForDocument(dataStage);
        ElasticSearchDocument doc = esServiceClient.getDocument(index, id);
        if (doc == null || doc.getSource() == null) {
            return null;
        }

        Map<String, Object> source = doc.getSource();
        String type = CastingUtils.getValueField(source, "type");
        Map<String, Object> res = new HashMap<>();
        res.put("id", source.get("id"));
        res.put("type", type);
        res.put("group", getGroup(dataStage));
        res.put("category", CastingUtils.getValueField(source, "category"));
        res.put("title", CastingUtils.getValueField(source, "title"));

        String disclaimer = CastingUtils.getValueField(source, "disclaimer");
        if (StringUtils.isNotBlank(disclaimer)) {
            res.put("disclaimer", disclaimer);
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
            if (!CollectionUtils.isEmpty(hasPreviews.getFilesOld())) {
                hasPreviews.getFilesOld().forEach(o -> {
                    previews.add(getPreview(o.getValue(), o.getStaticImageUrl(), o.getPreviewUrl()));
                });
            }
            if (!CollectionUtils.isEmpty(previews)) {
                res.put("previews", previews);
            }
        }

        res.put("fields", v);
        return res;
    }

    private Map<String, Object> getPreview(String label, TargetFile.FileImage staticImageUrl, TargetFile.FileImage previewUrl) {
        String imageUrl = staticImageUrl != null?staticImageUrl.getUrl():null;
        String videoUrl = previewUrl != null?previewUrl.getUrl():null;
        return getPreview(
                label,
                null,
                imageUrl,
                videoUrl,
                previewUrl != null && previewUrl.getIsAnimated()
        );
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
            final ElasticSearchFacetsResult elasticSearchFacetsResult = esServiceClient.searchDocuments(index, payload);
            final Map<String, List<ElasticSearchFacetsResult.Suggestion>> suggestResult = elasticSearchFacetsResult.getSuggest();
            Map<String, Set<ElasticSearchFacetsResult.Option>> suggestionsPerTerm = new HashMap<>();
            if (suggestResult != null) {
                suggestResult.values().stream().flatMap(Collection::stream).forEach(s -> {
                    final Set<ElasticSearchFacetsResult.Option> options = suggestionsPerTerm.computeIfAbsent(s.getText(), k -> new HashSet<>());
                    options.addAll(s.getOptions());
                });
            }
            Set<String> handledTerms = new HashSet<>();
            suggestionsPerTerm.keySet().forEach(k -> {
                final Set<ElasticSearchFacetsResult.Option> options = suggestionsPerTerm.get(k);
                final List<ElasticSearchFacetsResult.Option> sortedOptions = options.stream().sorted(Comparator.comparing(ElasticSearchFacetsResult.Option::getText)).collect(Collectors.toList());
                final List<ElasticSearchFacetsResult.Option> limitedOptions = sortedOptions.size() > 5 ? sortedOptions.subList(0, 5) : sortedOptions;
                limitedOptions.forEach(o -> {
                    if (!handledTerms.contains(o.getText())) {
                        handledTerms.add(o.getText());
                        result.put(o.getText(), query.replaceAll(k, o.getText()));
                    }
                });
            });
        }
        return result;
    }


    private List<Map<String, Object>> getKeywords(List<ElasticSearchAgg.Bucket> buckets, FacetValue facetValue, boolean hasResults) {
        List<String> values = (facetValue == null || CollectionUtils.isEmpty(facetValue.getValues())) ? null : facetValue.getValues();
        if (CollectionUtils.isEmpty(buckets) && (hasResults || CollectionUtils.isEmpty(values))) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> keywords = new ArrayList<>();
        Set<String> keywordsWithValues = new HashSet<>();
        for (ElasticSearchAgg.Bucket bucket : buckets) {
            keywords.add(Map.of(
                    "value", bucket.getKey(),
                    "count", bucket.getDocCount()
            ));
            keywordsWithValues.add(bucket.getKey());
        }
        if (!hasResults && !CollectionUtils.isEmpty(values)) {
            for (String keyword : values) {
                if (!keywordsWithValues.contains(keyword)) {
                    keywords.add(Map.of(
                            "value", keyword,
                            "count", 0
                    ));
                }
            }
        }
        return keywords;
    }

    private List<Map<String, Object>> getHierarchicalKeywords(List<ElasticSearchAgg.Bucket> buckets, FacetValue facetValue, boolean hasResults) {
        if (CollectionUtils.isEmpty(buckets) && (hasResults || facetValue == null || CollectionUtils.isEmpty(facetValue.getValues()))) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> keywords = new ArrayList<>();
        for (ElasticSearchAgg.Bucket bucket : buckets) {
            ElasticSearchAgg child = bucket.getKeywords();
            List<ElasticSearchAgg.Bucket> childBuckets = (child != null) ? child.getBuckets() : null;
            if (childBuckets != null) {
                keywords.add(Map.of(
                        "value", bucket.getKey(),
                        "count", bucket.getDocCount(),
                        "children", Map.of(
                                "keywords", getKeywords(childBuckets, facetValue, hasResults),
                                "others", getOthers(child)
                        )
                ));
            } else {
                keywords.add(Map.of(
                        "value", bucket.getKey(),
                        "count", bucket.getDocCount()
                ));
            }
        }
        return keywords;
    }

    private List<Map<String, Object>> getNestedKeywords(List<ElasticSearchAgg.Bucket> buckets, FacetValue facetValue, boolean hasResults) {
        List<String> values = (facetValue == null || CollectionUtils.isEmpty(facetValue.getValues())) ? null : facetValue.getValues();
        if (CollectionUtils.isEmpty(buckets) && (hasResults || CollectionUtils.isEmpty(values))) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> keywords = new ArrayList<>();
        Set<String> keywordsWithValues = new HashSet<>();
        for (ElasticSearchAgg.Bucket bucket : buckets) {
            keywords.add(Map.of(
                    "value", bucket.getKey(),
                    "count", (bucket.getReverse() != null) ? bucket.getReverse().getDocCount() : 0
            ));
            keywordsWithValues.add(bucket.getKey());
        }
        if (!hasResults && !CollectionUtils.isEmpty(values)) {
            for (String keyword : values) {
                if (!keywordsWithValues.contains(keyword)) {
                    keywords.add(Map.of(
                            "value", keyword,
                            "count", 0
                    ));
                }
            }
        }
        return keywords;
    }

    private int getOthers(ElasticSearchAgg keywords) {
        if (keywords == null) {
            return 0;
        }
        return keywords.getSumOtherDocCount();
    }

    private Map<String, Object> getHierarchicalFacetList(ElasticSearchFacetsResult.Aggregation agg, FacetValue facetValue, boolean hasResults) {
        ElasticSearchAgg keywords = agg.getKeywords();
        List<ElasticSearchAgg.Bucket> buckets = (keywords != null) ? keywords.getBuckets() : null;
        return Map.of(
                "keywords", getHierarchicalKeywords(buckets, facetValue, hasResults),
                "others", getOthers(keywords),
                "count", getFacetCount(agg)
        );
    }

    private Map<String, Object> getNestedFacetList(ElasticSearchFacetsResult.Aggregation agg, FacetValue facetValue, boolean hasResults) {
        ElasticSearchAgg keywords = agg.getInner() != null ? agg.getInner().getKeywords() : null;
        List<ElasticSearchAgg.Bucket> buckets = (keywords != null) ? keywords.getBuckets() : null;
        return Map.of(
                "keywords", getNestedKeywords(buckets, facetValue, hasResults),
                "others", getOthers(keywords),
                "count", getNestedFacetCount(agg)
        );
    }

    private Map<String, Object> getFacetList(ElasticSearchFacetsResult.Aggregation agg, FacetValue facetValue, boolean hasResults) {
        ElasticSearchAgg keywords = agg.getKeywords();
        List<ElasticSearchAgg.Bucket> buckets = (keywords != null) ? keywords.getBuckets() : null;
        return Map.of(
                "keywords", getKeywords(buckets, facetValue, hasResults),
                "others", getOthers(keywords),
                "count", getFacetCount(agg)
        );
    }

    private Map<String, Object> getFacetExists(ElasticSearchFacetsResult.Aggregation agg) {
        return Map.of(
                "count", getFacetCount(agg)
        );
    }

    private int getFacetCount(ElasticSearchFacetsResult.Aggregation agg) {
        if (agg == null || agg.getTotal() == null || agg.getTotal().getValue() == null) {
            return 0;
        }
        return agg.getTotal().getValue();
    }

    private int getNestedFacetCount(ElasticSearchFacetsResult.Aggregation agg) {
        if (agg == null || agg.getKeywords() == null || agg.getKeywords().getBuckets() == null || agg.getKeywords().getBuckets().isEmpty()) {
            return 0;
        }
        return agg.getKeywords().getBuckets().stream().mapToInt(bucket -> (bucket.getReverse() != null) ? bucket.getReverse().getDocCount() : 0).sum();
    }

    private Map<String, Object> getFacetAggregation(List<Facet> facets, Map<String, ElasticSearchFacetsResult.Aggregation> aggregations, Map<String, FacetValue> facetValues, boolean hasResults) {
        if (CollectionUtils.isEmpty(aggregations)) {
            return Collections.emptyMap();
        }
        Map<String, Object> res = new HashMap<>();
        facets.forEach(facet -> {
            if (aggregations.containsKey(facet.getName())) {

                ElasticSearchFacetsResult.Aggregation agg = aggregations.get(facet.getName());
                if (facet.getType() == FieldInfo.Facet.LIST) {
                    FacetValue facetValue = facetValues.get(facet.getName());
                    if (facet.isChild()) {
                        if (facet.getIsHierarchical()) {
                            res.put(facet.getName(), getHierarchicalFacetList(agg, facetValue, hasResults));
                        } else { // nested
                            res.put(facet.getName(), getNestedFacetList(agg, facetValue, hasResults));
                        }
                    } else {
                        res.put(facet.getName(), getFacetList(agg, facetValue, hasResults));
                    }
                } else if (facet.getType() == FieldInfo.Facet.EXISTS) {
                    res.put(facet.getName(), getFacetExists(agg));
                }
            }
        });
        if (!hasResults) {
            for (Map.Entry<String, FacetValue> entry : facetValues.entrySet()) {
                String name = entry.getKey();
                FacetValue value = entry.getValue();
                List<String> values = value.getValues();
                if (!res.containsKey(name) && !CollectionUtils.isEmpty(values)) {
                    List<Facet> matchingFacets = facets.stream().filter(facet -> facet.getName().equals(name)).collect(Collectors.toList());
                    if (!matchingFacets.isEmpty()) {
                        Facet facet = matchingFacets.get(0);
                        if (facet.getType() == FieldInfo.Facet.LIST) {
//                        if (facet.isChild() && facet.getIsHierarchical()) {
//
//                        } else {
                            List<Map<String, Object>> keywords = new ArrayList<>();
                            for (String keyword : values) {
                                keywords.add(Map.of(
                                        "count", 0,
                                        "value", keyword
                                ));
                            }
                            res.put(name, Map.of(
                                    "count", 0,
                                    "keywords", keywords,
                                    "others", 0
                            ));
//                        }
                        }
                    }
                }
            }
        }
        return res;
    }

    private Map<String, Object> getTypesAggregation(Map<String, ElasticSearchFacetsResult.Aggregation> aggregations) {
        if (CollectionUtils.isEmpty(aggregations) ||
                !aggregations.containsKey(FacetsUtils.FACET_TYPE) ||
                aggregations.get(FacetsUtils.FACET_TYPE).getKeywords() == null ||
                CollectionUtils.isEmpty(aggregations.get(FacetsUtils.FACET_TYPE).getKeywords().getBuckets())
        ) {
            return Collections.emptyMap();
        }
        Map<String, Object> res = new HashMap<>();
        List<ElasticSearchAgg.Bucket> buckets = aggregations.get(FacetsUtils.FACET_TYPE).getKeywords().getBuckets();
        for (ElasticSearchAgg.Bucket bucket : buckets) {
            if (bucket.getDocCount() > 0) {
                res.put(bucket.getKey(), Map.of(
                        "count", bucket.getDocCount()
                ));
            }
        }
        return res;
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
        return Map.of(
                "encoder", "html",
                "fields", highlights
        );
    }

    private List<Object> getEsSort(MetaInfo metaInfo, boolean forceSortByRelevance) {
        boolean sortByRelevance = true;
        if (!forceSortByRelevance && metaInfo != null && !metaInfo.sortByRelevance()) {
            sortByRelevance = false;
        }

        List<Object> fields = new ArrayList<>();

        if (sortByRelevance) {

            Map<String, String> score = new HashMap<>();
            score.put("order", "desc");
            Map<String, Object> first = new HashMap<>();
            first.put("_score", score);
            fields.add(first);

            Map<String, String> firstRelease = new HashMap<>();
            firstRelease.put("order", "desc");
            firstRelease.put("missing", "_last");
            Map<String, Object> second = new HashMap<>();
            second.put("first_release.value", firstRelease);
            fields.add(second);

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
