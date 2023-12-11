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

package eu.ebrains.kg.search.utils;

import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.FacetValue;
import eu.ebrains.kg.search.model.Facet;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.utils.FacetsUtils.FACET_TYPE;

public class FiltersUtils {

    private static List<Object> filterActiveFilters(Map<String, Object> activeFilters, String facetIdToSkip) {
        if (facetIdToSkip == null) {
            return new ArrayList<>(activeFilters.values());
        }
        return activeFilters.entrySet().stream().filter(e -> !e.getKey().equals(facetIdToSkip)).map(Map.Entry::getValue).collect(Collectors.toList());
    }

    public static Object getFilter(Map<String, Object> activeFilters, String facetIdToSkip) {
        List<Object> filtered = filterActiveFilters(activeFilters, facetIdToSkip);
        List<Object> filters = new ArrayList<>();
        filtered.forEach(f -> {
            if (f instanceof List) {
                filters.addAll((List) f);
            } else {
                filters.add(f);
            }
        });
        if (filters.size() > 1) {
            return Map.of(
                    "bool", Map.of(
                            "must", filters
                    )
            );
        }
        if (filters.size() == 1) {
            return filters.get(0);
        }
        return Map.of(
                "match_all", Collections.emptyMap()
        );
    }

    public static Map<String, Object> getActiveFilters(List<Facet> facets, String type, List<UUID> idsToFiler, Map<String, FacetValue> values) {
        Map<String, Object> filters = new HashMap<>();
        addTypeFilter(filters, type);
        addIdsFilter(filters, idsToFiler);
        facets.forEach(facet -> {
            if (values.containsKey(facet.getName())) {
                if (facet.getType() == FieldInfo.Facet.LIST) {
                    FacetValue value = values.get(facet.getName());
                    List<String> list = value.getValues();
                    if (!CollectionUtils.isEmpty(list)) {
                        if (facet.getExclusiveSelection()) {
                            addANDListFilter(filters, facet, list);
                        } else {
                            addORListFilter(filters, facet, list);
                        }
                    }
                } else if (facet.getType() == FieldInfo.Facet.EXISTS) {
                    addExistsFilter(filters, facet);
                }
            }
        });
        return filters;
    }

    private static void addORListFilter(Map<String, Object> filters, Facet facet, List<String> values) {
        if (values.size() == 1) {
            filters.put(facet.getName(), getFacetFilter(facet, values.get(0)));
        } else {
            List<Map<String, Object>> list = values.stream().map(v -> getFacetFilter(facet, v)).collect(Collectors.toList());
            filters.put(facet.getName(), Map.of(
                    "bool", Map.of(
                            "should", list
                    )
            ));
        }
    }

    private static void addANDListFilter(Map<String, Object> filters, Facet facet, List<String> values) {
        List<Map<String, Object>> list = values.stream().map(v -> getFacetFilter(facet, v)).collect(Collectors.toList());
        filters.put(facet.getName(), list);
    }

    private static void addExistsFilter(Map<String, Object> filters, Facet facet) {
        String path = FacetsUtils.getPath(facet.getPath(), facet.getProperty());
        String field = facet.getField(path);
        Map<String, Object> exists = Map.of(
                "exists", Map.of(
                        "field", field
                )
        );
        filters.put(facet.getName(), exists);
    }

    private static void addTypeFilter(Map<String, Object> filters, String type) {
        Map<String, Object> filter = Map.of(
                "term", Map.of(
                        "type.value", type
                )
        );
        filters.put(FACET_TYPE, filter);
    }

    private static void addIdsFilter(Map<String, Object> filters, List<UUID> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            List<Object> terms = ids.stream().map(id -> Map.of(
                    "term", Map.of(
                            "id", id
                    )
            )).collect(Collectors.toList());
            Map<String, Object> filter = Map.of(
                    "bool", Map.of(
                            "should", terms
                    )
            );
            filters.put("id_filter", filter);
        }
    }

    private static Map<String, Object> getFacetFilter(Facet facet, String value) {
        String path = FacetsUtils.getPath(facet.getPath(), facet.getProperty());
        String getField = facet.getField(path);
        Map<String, String> term = Map.of(
                getField, value
        );

        if (facet.isChild()) {
           if (facet.getIsHierarchical()) {
               return Map.of("term", term);
           }
           return Map.of(
                "nested", Map.of(
                    "path", String.format("%s.children", facet.getProperty()),
                    "query", Map.of(
                        "term", term
                    )
                )
            );
        }
        return Map.of("term", term);
    };
}
