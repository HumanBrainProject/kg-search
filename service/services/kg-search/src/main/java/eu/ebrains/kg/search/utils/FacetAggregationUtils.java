package eu.ebrains.kg.search.utils;

import eu.ebrains.kg.common.model.elasticsearch.*;
import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.Facet;
import eu.ebrains.kg.search.model.FacetValue;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class FacetAggregationUtils {
    private static List<Map<String, Object>> getKeywords(List<KeywordsBucket> buckets, FacetValue facetValue, boolean hasResults) {
        List<String> values = (facetValue == null || CollectionUtils.isEmpty(facetValue.getValues())) ? null : facetValue.getValues();
        if (CollectionUtils.isEmpty(buckets) && (hasResults || CollectionUtils.isEmpty(values))) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> keywords = new ArrayList<>();
        Set<String> keywordsWithValues = new HashSet<>();
        for (Bucket bucket : buckets) {
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

    private static List<Map<String, Object>> getHierarchicalKeywords(List<KeywordsBucket> buckets, FacetValue facetValue, boolean hasResults) {
        if (CollectionUtils.isEmpty(buckets) && (hasResults || facetValue == null || CollectionUtils.isEmpty(facetValue.getValues()))) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> keywords = new ArrayList<>();
        for (KeywordsBucket bucket : buckets) {
            KeywordsAgg child = bucket.getKeywords();
            List<KeywordsBucket> childBuckets = (child != null) ? child.getBuckets() : null;
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

    private static List<Map<String, Object>> getNestedKeywords(List<KeywordsBucket> buckets, FacetValue facetValue, boolean hasResults) {
        List<String> values = (facetValue == null || CollectionUtils.isEmpty(facetValue.getValues())) ? null : facetValue.getValues();
        if (CollectionUtils.isEmpty(buckets) && (hasResults || CollectionUtils.isEmpty(values))) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> keywords = new ArrayList<>();
        Set<String> keywordsWithValues = new HashSet<>();
        for (KeywordsBucket bucket : buckets) {
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

    private static int getOthers(Agg keywords) {
        if (keywords == null || keywords.getSumOtherDocCount() == null) {
            return 0;
        }
        return keywords.getSumOtherDocCount();
    }

    private static Map<String, Object> getHierarchicalFacetList(Aggregation agg, FacetValue facetValue, boolean hasResults) {
        KeywordsAgg keywords = agg.getKeywords();
        List<KeywordsBucket> buckets = (keywords != null) ? keywords.getBuckets() : null;
        return Map.of(
                "keywords", getHierarchicalKeywords(buckets, facetValue, hasResults),
                "others", getOthers(keywords),
                "count", getFacetCount(agg)
        );
    }

    private static Map<String, Object> getNestedFacetList(Aggregation agg, FacetValue facetValue, boolean hasResults) {
        KeywordsAgg keywords = agg.getInner() != null ? agg.getInner().getKeywords() : null;
        List<KeywordsBucket> buckets = (keywords != null) ? keywords.getBuckets() : null;
        return Map.of(
                "keywords", getNestedKeywords(buckets, facetValue, hasResults),
                "others", getOthers(keywords),
                "count", getNestedFacetCount(agg)
        );
    }

    private static Map<String, Object> getFacetList(Aggregation agg, FacetValue facetValue, boolean hasResults) {
        KeywordsAgg keywords = agg.getKeywords();
        List<KeywordsBucket> buckets = (keywords != null) ? keywords.getBuckets() : null;
        return Map.of(
                "keywords", getKeywords(buckets, facetValue, hasResults),
                "others", getOthers(keywords),
                "count", getFacetCount(agg)
        );
    }

    private static Map<String, Object> getFacetExists(Aggregation agg) {
        return Map.of(
                "count", getFacetCount(agg)
        );
    }

    private static int getFacetCount(Aggregation agg) {
        if (agg == null || agg.getTotal() == null || agg.getTotal().getValue() == null) {
            return 0;
        }
        return agg.getTotal().getValue();
    }

    private static int getNestedFacetCount(Aggregation agg) {
        if (agg == null || agg.getKeywords() == null || agg.getKeywords().getBuckets() == null || agg.getKeywords().getBuckets().isEmpty()) {
            return 0;
        }
        return agg.getKeywords().getBuckets().stream().mapToInt(bucket -> (bucket.getReverse() != null) ? bucket.getReverse().getDocCount() : 0).sum();
    }

    public static Map<String, Object> getFacetAggregation(List<Facet> facets, Map<String, Aggregation> aggregations, Map<String, FacetValue> facetValues, boolean hasResults) {
        if (CollectionUtils.isEmpty(aggregations)) {
            return Collections.emptyMap();
        }
        Map<String, Object> res = new HashMap<>();
        facets.forEach(facet -> {
            if (aggregations.containsKey(facet.getName())) {

                Aggregation agg = aggregations.get(facet.getName());
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

    public static Map<String, Object> getTypesAggregation(Map<String, Aggregation> aggregations) {
        if (CollectionUtils.isEmpty(aggregations) ||
                !aggregations.containsKey(FacetsUtils.FACET_TYPE) ||
                aggregations.get(FacetsUtils.FACET_TYPE).getKeywords() == null ||
                CollectionUtils.isEmpty(aggregations.get(FacetsUtils.FACET_TYPE).getKeywords().getBuckets())
        ) {
            return Collections.emptyMap();
        }
        Map<String, Object> res = new HashMap<>();
        List<KeywordsBucket> buckets = aggregations.get(FacetsUtils.FACET_TYPE).getKeywords().getBuckets();
        for (Bucket bucket : buckets) {
            if (bucket.getDocCount() > 0) {
                res.put(bucket.getKey(), Map.of(
                        "count", bucket.getDocCount()
                ));
            }
        }
        return res;
    }
}
