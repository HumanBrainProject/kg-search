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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.target.elasticsearch.*;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.File;
import eu.ebrains.kg.common.services.ESServiceClient;
import eu.ebrains.kg.common.utils.ESHelper;
import eu.ebrains.kg.common.utils.MetaModelUtils;
import eu.ebrains.kg.search.controller.authentication.UserInfoRoles;
import eu.ebrains.kg.search.controller.facets.FacetsController;
import eu.ebrains.kg.search.model.Facet;
import eu.ebrains.kg.search.model.FacetValue;
import eu.ebrains.kg.search.utils.AggsUtils;
import eu.ebrains.kg.search.utils.FacetsUtils;
import eu.ebrains.kg.search.utils.FiltersUtils;
import eu.ebrains.kg.search.utils.QueryStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.security.Principal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@SuppressWarnings("java:S1452") // we keep the generics intentionally
public class SearchController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ESServiceClient esServiceClient;
    private final UserInfoRoles userInfoRoles;
    private final FacetsController facetsController;

    private final SearchFieldsController searchFieldsController;

    private final static String TOTAL = "total";

    public SearchController(
            ESServiceClient esServiceClient,
            UserInfoRoles userInfoRoles,
            FacetsController facetsController,
            SearchFieldsController searchFieldsController
    ) {
        this.esServiceClient = esServiceClient;
        this.userInfoRoles = userInfoRoles;
        this.facetsController = facetsController;
        this.searchFieldsController = searchFieldsController;
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


    public Map<String, Object> search(String q, String type, int from, int size, String sort, Map<String, FacetValue> facetValues, DataStage dataStage) {
        String index = ESHelper.getIndexesForSearch(dataStage);
        Map<String, Object> payload = new HashMap<>();
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
        Object esAggs = AggsUtils.getAggs(facets, activeFilters, facetValues);
        payload.put("aggs", esAggs);
        ObjectNode esQuery = null;
        List<String> sanitizedQuery = QueryStringUtils.sanitizeQueryString(q);
        final ObjectNode query = getEsQuery(QueryStringUtils.prepareQuery(sanitizedQuery), type);
        if (query != null) {
            payload.put("query", query);
        }
        ElasticSearchFacetsResult result = esServiceClient.searchDocuments(index, payload);
        int total = (result.getHits() != null && result.getHits().getTotal() != null) ? result.getHits().getTotal().getValue() : 0;
        Map<String, Object> response = new HashMap<>();
        response.put("total", total);
        response.put("hits", (result.getHits() != null && result.getHits().getHits() != null) ? result.getHits().getHits() : Collections.emptyList());
        response.put("aggregations", getFacetAggregation(facets, result.getAggregations(), facetValues, total != 0));
        response.put("types", getTypesAggregation(result.getAggregations()));
        response.put("suggestions", getSuggestions(sanitizedQuery, dataStage, type));

        return response;
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

    private ObjectNode getEsQuery(String q, String type) {
        if (StringUtils.isBlank(q)) {
            return null;
        }
        ObjectNode queryString = objectMapper.createObjectNode();
        queryString.put("lenient", true);
        queryString.put("analyze_wildcard", true);
        queryString.put("query", getEsSanitizedQuery(q));
        List<String> fields = searchFieldsController.getEsQueryFields(type);
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
        List<String> highlights = searchFieldsController.getHighlight(type);
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
}