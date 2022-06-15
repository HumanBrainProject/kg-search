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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.ebrains.kg.common.model.TranslatorModel;
import eu.ebrains.kg.common.model.target.elasticsearch.*;
import eu.ebrains.kg.search.controller.authentication.UserInfoRoles;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.File;
import eu.ebrains.kg.common.services.ESServiceClient;
import eu.ebrains.kg.common.utils.ESHelper;
import eu.ebrains.kg.common.utils.MetaModelUtils;
import eu.ebrains.kg.search.model.Facet;
import eu.ebrains.kg.search.model.FacetValue;
import eu.ebrains.kg.search.utils.AggsUtils;
import eu.ebrains.kg.search.controller.facets.FacetsController;
import eu.ebrains.kg.search.utils.FiltersUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.lang.reflect.Type;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
@SuppressWarnings("java:S1452") // we keep the generics intentionally
public class SearchController {

    private final Set<String> FIELDS_TO_HIGHLIGHT = Stream.of(
            "title.value",
            "description.value",
            "contributors.value",
            "custodians.value",
            "owners.value",
            "component.value",
            "created_at.value",
            "releasedate.value",
            "activities.value"
    ).collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String parentCountLabel = "temporary_parent_doc_count";
    private final String docCountLabel = "doc_count";
    private final JsonNode parentDocCountObj = objectMapper.readTree(
            String.format("{\"%s\": {\"reverse_nested\": {}}}", parentCountLabel)
    );
    private Map<String, Object> temp = Map.of(
            "temporary_parent_doc_count", Map.of(
                    "reverse_nested", Collections.emptyMap()
            )
    );
    private final ESServiceClient esServiceClient;
    private final MetaModelUtils utils;
    private final UserInfoRoles userInfoRoles;
    private final FacetsController facetsController;
    private final static String TOTAL = "total";

    public SearchController(
        ESServiceClient esServiceClient,
        MetaModelUtils utils,
        UserInfoRoles userInfoRoles,
        FacetsController facetsController
) throws JsonProcessingException {
        this.esServiceClient = esServiceClient;
        this.utils = utils;
        this.userInfoRoles = userInfoRoles;
        this.facetsController = facetsController;
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
            List<Object> data = hits.stream().map(e -> e.getSource()).filter(Objects::nonNull).collect(Collectors.toList());
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


    public Map<String, Object> search(String q, String type, int from, int size, String sort, Map<String, FacetValue> facetValues, DataStage dataStage) throws JsonProcessingException {
        String index = ESHelper.getIndexesForSearch(dataStage);
        Map<String, Object> payload = new HashMap<>();
        ObjectNode esQuery = getEsQuery(q, type);
        if (esQuery != null) {
            payload.put("query", esQuery);
        }
        payload.put("from", from);
        payload.put("size", size);
        ObjectNode esHighlight = getEsHighlight(type);
        if (esHighlight != null) {
            payload.put("highlight", esHighlight);
        }
        List<Object> esSort = getEsSort(sort);
        if (esSort != null) {
            payload.put("sort", esSort);
        }
        List<Facet> facets = facetsController.getFacets(type);
        Map<String, Object> activeFilters = FiltersUtils.getActiveFilters(facets, type, facetValues);
        Object esPostFilter = FiltersUtils.getFilter(activeFilters, null);
        payload.put("post_filter", esPostFilter);
        Object esAggs =  AggsUtils.getAggs(facets, activeFilters, facetValues);
        payload.put("aggs", esAggs);
        ElasticSearchFacetsResult result = esServiceClient.searchDocuments(index, payload);
        Map<String, Object> response = new HashMap<>();
        response.put("total", (result.getHits() != null && result.getHits().getTotal() != null)?result.getHits().getTotal().getValue():0);
        response.put("hits", (result.getHits() != null && result.getHits().getHits() != null)?result.getHits().getHits(): Collections.emptyList());
        response.put("aggregations", getFacetAggregation(facets, result.getAggregations()));
        return response;
    }

    private List<Map<String, Object>> getKeywords(List<ElasticSearchAgg.Bucket> buckets) {
        if (CollectionUtils.isEmpty(buckets)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> keywords = new ArrayList<>();
        for (ElasticSearchAgg.Bucket bucket : buckets) {
            keywords.add(Map.of(
                "value", bucket.getKey(),
                "count", bucket.getDocCount()
            ));
        }
        return keywords;
    }

    private List<Map<String, Object>> getHierarchicalKeywords(List<ElasticSearchAgg.Bucket> buckets) {
        if (CollectionUtils.isEmpty(buckets)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> keywords = new ArrayList<>();
        for (ElasticSearchAgg.Bucket bucket : buckets) {
            ElasticSearchAgg child = bucket.getKeywords();
            List<ElasticSearchAgg.Bucket> childBuckets = (child != null)?child.getBuckets():null;
            if (childBuckets != null) {
                keywords.add(Map.of(
                        "value", bucket.getKey(),
                        "count", bucket.getDocCount(),
                        "children", Map.of(
                                "keywords", getKeywords(childBuckets),
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

    private List<Map<String, Object>> getNestedKeywords(List<ElasticSearchAgg.Bucket> buckets) {
        if (CollectionUtils.isEmpty(buckets)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> keywords = new ArrayList<>();
        for (ElasticSearchAgg.Bucket bucket : buckets) {
            keywords.add(Map.of(
                    "value", bucket.getKey(),
                    "count", (bucket.getReverse() != null)?bucket.getReverse().getDocCount():0
            ));
        }
        return keywords;
    }

    private int getOthers(ElasticSearchAgg keywords) {
        if (keywords == null) {
            return 0;
        }
        return keywords.getSumOtherDocCount();
    }

    private Map<String, Object> getHierarchicalFacetList(Facet facet, ElasticSearchFacetsResult.Aggregation agg) {
        ElasticSearchAgg keywords = agg.getKeywords();
        List<ElasticSearchAgg.Bucket> buckets = (keywords != null)?keywords.getBuckets():null;
        return Map.of(
                "keywords", getHierarchicalKeywords(buckets),
                "others", getOthers(keywords),
                "count", getFacetCount(agg)
        );
    }

    private Map<String, Object> getNestedFacetList(Facet facet, ElasticSearchFacetsResult.Aggregation agg) {
        ElasticSearchAgg keywords = agg.getInner() != null?agg.getInner().getKeywords():null;
        List<ElasticSearchAgg.Bucket> buckets = (keywords != null)?keywords.getBuckets():null;
        return Map.of(
                "keywords", getNestedKeywords(buckets),
                "others", getOthers(keywords),
                "count", getNestedFacetCount(agg)
        );
    }

    private Map<String, Object> getFacetList(Facet facet, ElasticSearchFacetsResult.Aggregation agg) {
        ElasticSearchAgg keywords = agg.getKeywords();
        List<ElasticSearchAgg.Bucket> buckets = (keywords != null)?keywords.getBuckets():null;
        return Map.of(
                "keywords", getKeywords(buckets),
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
        return agg.getKeywords().getBuckets().stream().mapToInt(bucket -> (bucket.getReverse() != null)?bucket.getReverse().getDocCount():0).sum();
    }

    private Map<String, Object> getFacetAggregation(List<Facet> facets, Map<String, ElasticSearchFacetsResult.Aggregation> aggregations) {
        if (CollectionUtils.isEmpty(aggregations)) {
            return Collections.emptyMap();
        }
        Map<String, Object> res = new HashMap<>();
        facets.forEach(facet -> {
            if (aggregations.containsKey(facet.getId())) {

                ElasticSearchFacetsResult.Aggregation agg = aggregations.get(facet.getId());

                if (facet.getFilterType() == FieldInfo.Facet.LIST) {
                    if (facet.isChild()) {
                        if (facet.getIsHierarchical()) {
                            res.put(facet.getId(), getHierarchicalFacetList(facet, agg));
                        } else { // nested
                            res.put(facet.getId(), getNestedFacetList(facet, agg));
                        }
                    } else {
                        res.put(facet.getId(), getFacetList(facet, agg));
                    }
                } else if (facet.getFilterType() == FieldInfo.Facet.EXISTS) {
                    res.put(facet.getId(), getFacetExists(agg));
                }
            }
        });
        return res;
    }

    private ObjectNode getEsQuery(String q, String type) {
        if (StringUtils.isBlank(q)) {
            return null;
        }
        ObjectNode queryString = objectMapper.createObjectNode();
        queryString.put("lenient", true);
        queryString.put("analyze_wildcard", true);
        queryString.put("query", getEsSanitizedQuery(q));
        List<String> fields = getEsQueryFields(type);
        if (!CollectionUtils.isEmpty(fields)) {
            ArrayNode fieldsTree = objectMapper.valueToTree(fields);
            queryString.putArray("fields").addAll(fieldsTree);
        }
        ObjectNode query = objectMapper.createObjectNode();
        query.set("query_string", queryString);
        return query;
    }

    private String getEsSanitizedQuery(String query) {
        return query;
    }

    private ObjectNode getEsHighlight(String type) {
        List<String> highlights = getHighlight(type);
        if (CollectionUtils.isEmpty(highlights)) {
            return null;
        }
        ObjectNode highlight = objectMapper.createObjectNode();
        highlight.put("encoder", "html");
        ObjectNode fields = objectMapper.createObjectNode();
        highlights.forEach(h -> {
            fields.set(h, objectMapper.createObjectNode());
        });
        highlight.set("fields", fields);
        return highlight;
    }

    private List<Object> getEsSort(String sort) {
        if (StringUtils.isBlank(sort)) {
            return null;
        }

        List<Object> fields = new ArrayList<>();

        if (sort.equals("newestFirst")) {

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

            Map<String, String> name = new HashMap<>();
            name.put("order", "asc");
            Map<String, Object> first = new HashMap<>();
            String key = String.format("%s.value.keyword", sort);
            first.put(key, name);
            fields.add(first);

        }
        return fields;
    }

    @Cacheable(value = "highlight", key = "#type")
    private List<String> getHighlight(String type) {
        List<String> highlights = new ArrayList<>();
        if (StringUtils.isNotBlank(type)) {
            TranslatorModel.MODELS.stream().filter(m -> MetaModelUtils.getNameForClass(m.getTargetClass()).equals(type)).forEach(m -> {
                Class<?> targetModel = m.getTargetClass();
                addFieldsHighlight(highlights, targetModel);
            });
        }
        return highlights;
    }

    private void addFieldsHighlight(List<String> highlights, Class<?> clazz) {
        List<MetaModelUtils.FieldWithGenericTypeInfo> allFields = utils.getAllFields(clazz);
        allFields.forEach(f -> {
            try {
                addFieldHighlight(highlights, f, "");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void addFieldHighlight(List<String> highlights, MetaModelUtils.FieldWithGenericTypeInfo f, String parentPath) throws ClassNotFoundException {
        FieldInfo info = f.getField().getAnnotation(FieldInfo.class);
        if (info != null && !info.ignoreForSearch()) {
            String propertyName = utils.getPropertyName(f.getField());
            String path = String.format("%s%s", parentPath, propertyName);
            if (!propertyName.equals("children")) { // if (f.getField().getType() != Children.class) { if (f.getField().getDeclaringClass() != Children.class) {
                if (StringUtils.isBlank(parentPath) || f.getField().getType() == Value.class) {
                    String valuePath = String.format("%s.value", path);
                    if (FIELDS_TO_HIGHLIGHT.contains(valuePath)) {
                        highlights.add(valuePath);
                    }
                } else {
                    if (FIELDS_TO_HIGHLIGHT.contains(path)) {
                        highlights.add(path);
                    }
                }
            }
//          Type topTypeToHandle = f.getGenericType() != null ? f.getGenericType() : MetaModelUtils.getTopTypeToHandle(f.getField().getGenericType());
//          addChildrenFieldHighlight(highlights, topTypeToHandle, String.format("%s.children", path));
        }
    }

    // No highlight in the children for now
//    private void addChildrenFieldHighlight(List<String> highlights, Type type, String parentPath) {
//        List<MetaModelUtils.FieldWithGenericTypeInfo> allFields = utils.getAllFields(type);
//        allFields.forEach(field -> {
//            try {
//                addFieldHighlight(highlights, field, parentPath);
//            } catch (ClassNotFoundException e) {
//                throw new RuntimeException(e);
//            }
//        });
//    }

    @Cacheable(value = "queryFields", key = "#type")
    private List<String> getEsQueryFields(String type) {
        Map<String, Double> boosts = new HashMap<>();
        Class<?> targetModelForType = null;
        for (int i = 0; i < TranslatorModel.MODELS.size(); i++) {
            Class<?> targetModel = TranslatorModel.MODELS.get(i).getTargetClass();
            String targetModelName = MetaModelUtils.getNameForClass(targetModel);
            if (StringUtils.isNotBlank(type) && targetModelName.equals(type)) {
                targetModelForType = targetModel;
            } else {
                addFieldsBoost(boosts, targetModel);
            }
        }
        //selected type fields override others
        if (targetModelForType != null) {
            addFieldsBoost(boosts, targetModelForType);
        }
        List<String> fields = boosts.entrySet().stream().map(e -> {
            String field = e.getKey();
            double boost = e.getValue() == null?1.0:(double) e.getValue();
            return String.format("%s^%d", field, (int) boost);
        }).sorted().collect(Collectors.toList());
        return fields;
    }

    private void addFieldsBoost(Map<String, Double> boosts, Class<?> clazz) {
        List<MetaModelUtils.FieldWithGenericTypeInfo> allFields = utils.getAllFields(clazz);
        allFields.forEach(f -> {
            try {
                addFieldBoost(boosts, f, "");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void addFieldBoost(Map<String, Double> boosts, MetaModelUtils.FieldWithGenericTypeInfo f, String parentPath) throws ClassNotFoundException {
        FieldInfo info = f.getField().getAnnotation(FieldInfo.class);
        if (info != null && !info.ignoreForSearch()) {
            String propertyName = utils.getPropertyName(f.getField());
            String path = String.format("%s%s", parentPath, propertyName);
            if (!propertyName.equals("children")) { // if (f.getField().getType() != Children.class) { if (f.getField().getDeclaringClass() != Children.class) {
                if (StringUtils.isBlank(parentPath) || f.getField().getType() == Value.class) {
                    String valuePath = String.format("%s.value", path);
                    boosts.put(valuePath, info.boost());
                } else {
                    boosts.put(path, info.boost());
                }
            }

            Type topTypeToHandle = f.getGenericType() != null ? f.getGenericType() : MetaModelUtils.getTopTypeToHandle(f.getField().getGenericType());
            addChildrenFieldBoost(boosts, topTypeToHandle, String.format("%s.", path));
        }
    }

    private void addChildrenFieldBoost(Map<String, Double> boosts, Type type, String parentPath) {
        List<MetaModelUtils.FieldWithGenericTypeInfo> allFields = utils.getAllFields(type);
        allFields.forEach(field -> {
            try {
                addFieldBoost(boosts, field, parentPath);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
