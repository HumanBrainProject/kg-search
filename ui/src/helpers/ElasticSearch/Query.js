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

import { sanitizeQueryString } from "./QueryString";
import { getQueryFields }  from "./QueryFields";

const getCustomHighlightFields = (queryFieldsByType, selectedType) => {
  if (selectedType && queryFieldsByType[selectedType]) {
    const highlightFields = Object.entries(queryFieldsByType[selectedType]).reduce((acc, [name, field]) => {
      if (field.highlight) {
        acc[name] = {}; // field
      }
      return acc;
    }, {});
    return Object.keys(highlightFields).length?highlightFields:null;
  }
  return null;
};

const buildFilter = (facet, key, value) => {
  const term = {
    [key]: value
  };
  if (facet.isChild) {
    if (facet.isHierarchical) {
      return {
        term: term
      };
    }

    return {
      nested: {
        path: `${facet.name}.children`,
        query: {
          term: term
        }
      }
    };
  }

  return {
    term: term
  };
};

const getAllFilters = facets => {
  const filters = {};

  facets.forEach(facet => {
    let filter = null;
    const facetKey = facet.isChild ?(facet.isHierarchical?`${facet.childName}.value.keyword`:`${facet.name}.children.${facet.childName}.value.keyword`):`${facet.name}.value.keyword`;
    switch (facet.filterType) {
    case "type":
    {
      filter = {
        term: {}
      };
      filter.term[facet.name] = facet.value;
      break;
    }
    case "list":
    {
      if (Array.isArray(facet.value) && facet.value.length) {
        if (facet.exclusiveSelection === false) { // OR
          if (facet.value.length > 1) {
            filter = {
              bool: {
                should: []
              }
            };
            facet.value.forEach(v => {
              filter.bool.should.push(buildFilter(facet, facetKey, v));
            });
          } else {
            filter = buildFilter(facet, facetKey, facet.value[0]);
          }
        } else { // AND
          filter = [];
          facet.value.forEach(v => {
            filter.push(buildFilter(facet, facetKey, v));
          });
        }
      }
      break;
    }
    case "exists":
    {
      filter = {
        exists: {
          field: facetKey
        }
      };
      break;
    }
    default:
      break;
    }
    if (filter) {
      filters[facet.id] = {
        facet: facet,
        filter: filter
      };
    }
  });
  return filters;
};


const setFilters = (filters, key) => {
  const filtered = Object.entries(filters).filter(([id, { facet }]) => {
    const active = !!facet.value;
    switch (facet.filterType) {
    case "exists":
      if (id === key) {
        return true;
      }
      return active && id !== key;
    case "type":
    case "list":
    default:
      return active && id !== key;
    }
  });
  const res = filtered.reduce((acc, [, { filter }]) => {
    if (Array.isArray(filter)) {
      acc.push(...filter);
    } else {
      acc.push(filter);
    }
    return acc;
  }, []);
  if (res.length > 1) {
    return {
      bool: {
        must: res
      }
    };
  } else if (res.length === 1) {
    return res[0];
  }
  return {
    match_all: {}
  };
};


const setAggs = (key, count, orderDirection, size) => {
  const aggs = {};
  aggs[key] = {
    terms: {
      field: key,
      size: size
    }
  };
  if (orderDirection) {
    aggs[key].terms.order = {
      _count: orderDirection
    };
  }
  aggs[count] = {
    cardinality: {
      field: key
    }
  };
  return aggs;
};


const setListFacetAggs = (aggs, facet) => {

  const orderKey = facet.filterOrder && facet.filterOrder === "byvalue" ? "_term" : "_count";
  const orderDirection = orderKey === "_term" ? "asc" : "desc";

  if (facet.isChild) {
    if (facet.isHierarchical) {
      const key = `${facet.name}.value.keyword`;
      const count = `${facet.name}.value.keyword_count`;
      aggs[facet.id] = {
        aggs: setAggs(key, count, orderDirection, facet.size)
      };
      aggs[facet.id].aggs[key].terms.missing = facet.missingTerm;
      const subKey = `${facet.childName}.value.keyword`;
      const subCount = `${facet.childName}.value.keyword_count`;
      aggs[facet.id].aggs[key].aggs  = setAggs(subKey, subCount, orderDirection, facet.size);
    } else {
      const key = `${facet.name}.children.${facet.childName}.value.keyword`;
      const count = `${facet.name}.children.${facet.childName}.value.keyword_count`;
      aggs[facet.id] = {
        aggs: {
          inner: {
            aggs: setAggs(key, count, orderDirection, facet.size),
            nested: {
              path: `${facet.name}.children`
            }
          }
        }
      };
    }
  } else {
    const key = `${facet.name}.value.keyword`;
    const count = `${facet.name}.value.keyword_count`;
    aggs[facet.id] = {
      aggs: setAggs(key, count, orderDirection, facet.size)
    };
  }
};

const setFacetAggs = (aggs, facets) => {
  facets.forEach(facet => {
    switch (facet.filterType) {
    case "type":
    {
      aggs[facet.id] = {
        aggs: setAggs(facet.name, `${facet.name}_count`, null, 50)
      };
      break;
    }
    case "list":
    {
      setListFacetAggs(aggs, facet);
      break;
    }
    case "exists":
    default:
      aggs[facet.id] = {};
      break;
    }
  });
};

const setFacetFilter = (aggs, facets, facetFilters) => {
  facets.forEach(facet => {
    const filters = setFilters(facetFilters, facet.id);
    if (filters) {
      aggs[facet.id].filter = filters;
    } else {
      aggs[facet.id].filter = {
        match_all: {}
      };
    }
  });
};

const getAggs = (facets, allFilters) => {
  const aggs = {};
  setFacetAggs(aggs, facets);
  setFacetFilter(aggs, facets, allFilters);

  const typeFilters = setFilters(allFilters, "facet_type");
  if (typeFilters) {
    aggs.facet_type.filter = typeFilters;
  } else {
    aggs.facet_type.filter = {
      match_all: {}
    };
  }
  return aggs;
};

const getSort = name => {
  if (name === "newestFirst") {
    return [
      {
        _score: {
          order: "desc"
        }
      },
      {
        "first_release.value": {
          order: "desc",
          missing: "_last"
        }
      }
    ];
  }
  return [
    {
      [`${name}.value.keyword`]: {
        order: "asc"
      }
    }
  ];
};

export const getQueryPayload = ({ queryString, selectedType, queryFieldsByType, facets, sort, from, hitsPerPage }) => {
  const queryFields = getQueryFields(queryFieldsByType, selectedType);
  const customHighlightFields = getCustomHighlightFields(queryFieldsByType, selectedType);

  const typeFacet = {
    id: "facet_type",
    name: "type.value",
    filterType: "type",
    value: selectedType
  };

  const queryFacets = [...facets, typeFacet];

  const query = queryString ? {
    query_string: {
      query: sanitizeQueryString(queryString),
      lenient: true,
      analyze_wildcard: true
    }
  } : null;
  if (query && query.query_string && queryFields.length) {
    query.query_string.fields = queryFields;
  }

  const allFilters = getAllFilters(queryFacets);

  const payload = {
    aggs: getAggs(queryFacets.filter(facet => facet.type === selectedType || facet.filterType === "type"), allFilters),
    from: from,
    post_filter: setFilters(allFilters),
    size: hitsPerPage
  };

  if (customHighlightFields) {
    payload.highlight = {
      fields: customHighlightFields,
      encoder: "html"
    };
  }

  if (sort) {
    payload.sort = getSort(sort);
  }

  if (query) {
    payload.query = query;
  }
  return payload;
};