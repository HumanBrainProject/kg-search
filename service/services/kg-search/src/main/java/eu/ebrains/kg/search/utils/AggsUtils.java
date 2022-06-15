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

package eu.ebrains.kg.search.utils;

import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.Facet;
import eu.ebrains.kg.search.model.FacetValue;

import java.util.*;

import static eu.ebrains.kg.search.utils.FacetsUtils.FACET_TYPE;

public class AggsUtils {

    public static Map<String, Object> getAggs(List<Facet> facets, Map<String, Object> filters, Map<String, FacetValue> facetValues) {
        Map<String, Object> aggs = new HashMap<>();
        facets.forEach(facet -> setFacet(aggs, facet, filters, facetValues.get(facet.getId())));
        setTypeFacet(aggs, filters);
        return aggs;
    }

    private static void setFacet(Map<String, Object> aggs, Facet facet, Map<String, Object> filters, FacetValue value) {
        Object filter = FiltersUtils.getFilter(filters, facet.getId());
        Map<String, Object> facetAggs = getFacetAggs(facet, value);
        if (facetAggs != null) {
            aggs.put(facet.getId(), Map.of(
                    "aggs", facetAggs,
                    "filter", filter
                )
            );
        }
    }

    private static Map<String, Object> getFacetAggs(Facet facet, FacetValue value) {
        if (facet.getFilterType() == FieldInfo.Facet.LIST) {
            Integer size = (value != null)? value.getSize() : null;
            return getListFacetAggs(facet, size);
        }
        if (facet.getFilterType() == FieldInfo.Facet.EXISTS) {
            return Collections.emptyMap();
        }
        return null;
    }

    private static Map<String, Object> getListFacetAggs(Facet facet, Integer size) {
        String orderDirection =  facet.getFilterOrder() == FieldInfo.FacetOrder.BYVALUE? "asc" : "desc";
        if (facet.isChild()) {
            if (facet.getIsHierarchical()) {
                Map<String, Object> aggs = getLeafAggs(facet.getParentPath(), orderDirection, size, false);
                Map<String, Object> childAggs = getLeafAggs(facet.getName(), orderDirection, size, false);
                aggs.put("aggs", childAggs);
                return aggs;
            }
            Map<String, Object> aggs = getLeafAggs(FacetsUtils.getPath(facet.getPath(), facet.getName()), orderDirection, size, false);
            Map<String, Object> childAggs = Map.of(
                    "inner", Map.of(
                            "aggs", getLeafAggs(facet.getName(), orderDirection, size, true),
                            "nested", Map.of(
                                    "path", String.format("%s.children", facet.getPath())
                            )
                    )
            );
            aggs.put("aggs", childAggs);
            return aggs;
        }
        return getLeafAggs(FacetsUtils.getPath(facet.getPath(), facet.getName()), orderDirection, size, false);
    }

    private static Map<String, Object> getLeafAggs(String key, String orderDirection, Integer size, boolean reverseNested) {
        String name = String.format("%s.value.keyword", key);
        Map<String, Object> terms = new HashMap<>();
        if (size != null) {
            terms.put("size", size);
        }
        terms.put("field", name);
        if (orderDirection != null) {
            terms.put("order", Map.of(
                    "_count", orderDirection
            ));
        }
        Map<String, Object> leafAggs = new HashMap<>();
        leafAggs.put("keywords", Map.of(
                "terms", terms
        ));
        leafAggs.put("total", Map.of(
                "cardinality", Map.of(
                        "field", name
                )
            )
        );
        if (reverseNested) {
            leafAggs.put("aggs", Map.of(
                "reverse", Map.of(
                        "reverse_nested", Collections.emptyMap()
                    )
                )
            );
        }
        return leafAggs;
    }

    private static void setTypeFacet(Map<String, Object> aggs, Map<String, Object> filters) {
        Object filter = FiltersUtils.getFilter(filters, FACET_TYPE);
        aggs.put(FACET_TYPE, Map.of(
                "aggs", Map.of(
                        "type.value", Map.of(
                                "terms", Map.of(
                                        "field", "type.value",
                                        "size", 50
                                )
                        ),
                        "type.value_count", Map.of(
                                "cardinality", Map.of(
                                        "field", "type.value"
                                )
                        )
                ),
                "filter", filter
            )
        );
    }
}
