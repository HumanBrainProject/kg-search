/*
 *   Copyright (c) 2018, EPFL/Human Brain Project PCO
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import * as types from "../actions/actions.types";
import { ElasticSearchHelpers } from "../helpers/ElasticSearchHelpers";

const initialState = {
  queryFields: ["title", "description"],
  error: null,
  message: "",
  initialParams: {},
  facets: [],
  types: [],
  sort: null,
  page: 1,
  totalPages: 0,
  sortFields: [],
  facetTypesOrder: {},
  facetDefaultSelectedType: null,
  initialRequestDone: false,
  isLoading: false,
  queryString: "",
  selectedType: null,
  groupsSettings: {},
  hitsPerPage: 20,
  hits: [],
  total: 0,
  from: 0
};

const setupSearch = (state, action) => {
  const definition = action.definition;

  if (!definition) {
    return state;
  } else {

    const queryValuesBoost = ElasticSearchHelpers.getQueryValuesBoost(definition);
    const sortFields = ElasticSearchHelpers.getSortFields(definition);
    const facetTypesOrder = ElasticSearchHelpers.getFacetTypesOrder(definition);
    const defaultType = ElasticSearchHelpers.getDefaultSelectedType(definition, facetTypesOrder);
    const facets = ElasticSearchHelpers.constructFacets(definition);
    const types = Object.entries(definition).map(([type, typeDefinition]) => ({
      type: type,
      label: typeDefinition.name,
      count: 0
    }));

    const getType = (types, type, defaultType) => {
      const typeValue = Array.isArray(type)?type[0]:type;
      if (types.some(t => t.type === typeValue)) {
        return typeValue;
      }
      return defaultType;
    };

    const getSort = (sortFields, sort) => {
      const filtered = sortFields.filter(t => t.param === sort);
      if (filtered.length) {
        return filtered[0];
      }
      if (sortFields.length) {
        return sortFields[0];
      }
      return null;
    };
    const queryString = state.initialParams["q"]?state.initialParams["q"]:"";
    const selectedType = getType(types, state.initialParams["facet_type"], defaultType);
    const sort = getSort(sortFields, state.initialParams["sort"]);
    const pageNumber = Number(state.initialParams["p"]);
    const page = isNaN(pageNumber)?1:(pageNumber > 0?Math.floor(pageNumber):1);
    const from = (page -1) * state.hitsPerPage;
    facets.forEach(facet => {
      const value = state.initialParams[facet.id];
      if (value) {
        switch (facet.filterType) {
        case "list":
          facet.value = Array.isArray(value)?value:[];
          break;
        case "exists":
          facet.value = !!value;
          break;
        default:
          break;
        }
      }
    });

    return {
      ...state,
      queryString:  queryString,
      queryFields: queryValuesBoost,
      facets: facets,
      types: types,
      sort: sort,
      sortFields: sortFields,
      facetTypesOrder: facetTypesOrder,
      selectedType: selectedType,
      facetDefaultSelectedType: defaultType,
      page: page,
      from: from
    };
  }
};

const setQueryString = (state, action) => {
  return {
    ...state,
    queryString: action.queryString,
    page: 1,
    from: 0
  };
};

const setInitialSearchParams  = (state, action) => {
  return {
    ...state,
    initialParams: action.params
  };
};

const setFacet = (state, action) => {
  return {
    ...state,
    facets: state.facets.map(f => {
      if (f.id !== action.id) {
        return f;
      }
      switch (f.filterType) {
      case "list":
      {
        if (!action.keyword) {
          return f;
        }
        const facet = {
          ...f
        };
        if (action.active) {
          const values = Array.isArray(facet.value) ? facet.value : [];
          if (Array.isArray(action.keyword)) {
            action.keyword.forEach(keyword => {
              if (!values.includes(keyword)) {
                values.push(keyword);
              }
            });
          } else if (!values.includes(action.keyword)) {
            values.push(action.keyword);
          }
          facet.value = values;
        } else {
          if (Array.isArray(facet.value)) {
            if (Array.isArray(action.keyword)) {
              facet.value = facet.value.filter(value => !action.keyword.includes(value));
            } else {
              facet.value = facet.value.filter(value => value !== action.keyword);
            }
          }
        }
        return facet;
      }
      case "exists":
      {
        return {
          ...f,
          value: action.active
        };
      }
      default:
        return f;
      }
    }),
    page: 1,
    from: 0
  };
};

const getResetFacets = facets => {
  return facets.map(f => {
    switch (f.filterType) {
    case "list":
      return {
        ...f,
        value: null,
        size: (f.isHierarchical || f.isFilterable)?ElasticSearchHelpers.listFacetAllSize:ElasticSearchHelpers.listFacetDefaultSize
      };
    case "exists":
    default:
      return {
        ...f,
        value: null
      };
    }
  });
};

const resetFacets = state => ({
  ...state,
  facets: getResetFacets(state.facets),
  page: 1
});

const setFacetSize = (state, action) => {
  return {
    ...state,
    facets: state.facets.map(f => {
      if (f.id !== action.id) {
        return f;
      }
      switch (f.filterType) {
      case "list":
        return {
          ...f,
          size: action.size
        };
      case "exists":
      default:
        return f;
      }
    })
  };
};

const setSort = (state, action) => {
  const match = state.sortFields.filter(f => f.key === action.value);
  if (match.length) {
    return {
      ...state,
      sort: match[0]
    };
  }
  return state;
};

const setPage = (state, action) => {
  return {
    ...state,
    page: action.value,
    from: (action.value -1) * state.hitsPerPage
  };
};

const setType = (state, action) => {
  const selectedType = action.value;
  return {
    ...state,
    selectedType: selectedType,
    page: 1,
    from: 0
  };
};


const resetTypeForGroup = (state, action) => {

  const getSelectedType = (settings, group, defaultType) => (group && settings && settings[group] && settings[group].facetDefaultSelectedType) ? settings[group].facetDefaultSelectedType : defaultType;

  const selectedType = getSelectedType(state.groupsSettings, action.group, state.facetDefaultSelectedType);

  return {
    ...state,
    selectedType: selectedType,
    facets: getResetFacets(state.facets)
  };
};

const loadSearchRequest = state => {
  return {
    ...state,
    isLoading: true,
    error: null,
    message: ""
  };
};

const loadSearchResult = (state, action) => {

  const getUpdatedFacets = (facets, results) => {

    const getKeywords = (keywords, childName) => {
      if (!keywords || !Array.isArray(keywords.buckets)) {
        return [];
      }
      return keywords.buckets.map(bucket => {
        const child = childName && bucket[`${childName}.value.keyword`];
        if (child) {
          return {
            value: bucket.key,
            count: bucket.doc_count,
            children: {
              keywords: getKeywords(child),
              others: getOthers(child)
            }
          };
        } else {
          return {
            value: bucket.key,
            count: bucket.doc_count
          };
        }
      });
    };

    const getOthers = keywords => {
      if (!keywords || !keywords.sum_other_doc_count) {
        return 0;
      }
      const count = Number(keywords.sum_other_doc_count);
      return isNaN(count) ? 0 : count;
    };

    const aggs = (results && results.aggregations) ? results.aggregations : {};
    return facets.map(f => {
      const facet = {
        ...f
      };

      const res = aggs[facet.id];
      if (facet.filterType === "list") {
        if (facet.isChild) {
          if (facet.isHierarchical) {
            const keywords = res && res[`${facet.name}.value.keyword`];
            facet.keywords = getKeywords(keywords, facet.childName);
            facet.others = getOthers(keywords);
          } else { // nested
            const name =  `${facet.name}.children.${facet.childName}`;
            const keywords = res && res.inner && res.inner[`${name}.value.keyword`];
            facet.keywords = getKeywords(keywords);
            facet.others = getOthers(keywords);
          }
        } else {
          const keywords = res && res[`${facet.name}.value.keyword`];
          facet.keywords = getKeywords(keywords);
          facet.others = getOthers(keywords);
        }
      }
      const count = (res && res.doc_count) ? res.doc_count : 0;
      facet.count = count;
      return facet;
    });
  };

  const getUpdatedTypes = (types, selectedType, group, groupsSettings, defaultOrder, results) => {

    const buckets = Array.isArray(results?.aggregations?.facet_type?.type?.value?.buckets)?
      results.aggregations.facet_type.type.value.buckets.buckets : [];

    const counts = buckets.reduce((acc, current) => {
      const count = Number(current.doc_count);
      acc[current.key] = isNaN(count) ? 0 : count;
      return acc;
    }, {});

    const order = (group && groupsSettings && groupsSettings[group] && groupsSettings[group].facetTypesOrder) ?
      groupsSettings[group].facetTypesOrder : defaultOrder;

    return types
      .map(t => ({
        ...t,
        count: counts[t.type] ? counts[t.type] : 0
      }))
      .sort((a, b) => {
        if (order) {
          if (order[a.type] !== undefined && order[b.type] !== undefined) {
            return order[a.type] - order[b.type];
          }
          if (order[a.type] !== undefined) {
            return -1;
          }
          if (order[b.type] !== undefined) {
            return 1;
          }
        }
        return b.count - a.count;
      });
  };

  const total = (action.results && action.results.hits && action.results.hits.total) ?(isNaN(Number(action.results.hits.total))?0:Number(action.results.hits.total)): 0;

  return {
    ...state,
    message: "",
    initialRequestDone: true,
    isLoading: false,
    facets: getUpdatedFacets(state.facets, action.results),
    types: getUpdatedTypes(state.types, state.selectedType, state.group, state.groupsSettings, state.facetTypesOrder, action.results),
    hits: (action.results && action.results.hits && Array.isArray(action.results.hits.hits)) ? action.results.hits.hits : [],
    total: total,
    totalPages: Math.ceil(total / state.hitsPerPage)
  };
};

const loadSearchBadRequest = (state, action) => {
  return {
    ...state,
    message: action.error,
    isLoading: false,
    hits: [],
    from: 0,
    page: 1,
    totalPages: 0
  };
};

const loadSearchFail = (state, action) => {
  return {
    ...state,
    message: "",
    error: action.error,
    isLoading: false,
    hits: [],
    from: 0,
    page: 1,
    totalPages: 0
  };
};

const clearSearchError = state => {
  return {
    ...state,
    error: null
  };
};

const logout = state => {
  return {
    ...state,
    page: 1,
    from: 0
  };
};

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.SET_INITIAL_SEARCH_PARAMS:
    return setInitialSearchParams(state, action);
  case types.LOAD_DEFINITION_SUCCESS:
    return setupSearch(state, action);
  case types.SET_QUERY_STRING:
    return setQueryString(state, action);
  case types.SET_TYPE:
    return setType(state, action);
  case types.RESET_TYPE_FOR_GROUP:
    return resetTypeForGroup(state, action);
  case types.SET_SORT:
    return setSort(state, action);
  case types.SET_PAGE:
    return setPage(state, action);
  case types.SET_FACET:
    return setFacet(state, action);
  case types.SET_FACET_SIZE:
    return setFacetSize(state, action);
  case types.RESET_FACETS:
    return resetFacets(state, action);
  case types.LOAD_SEARCH_REQUEST:
    return loadSearchRequest(state, action);
  case types.LOAD_SEARCH_SUCCESS:
    return loadSearchResult(state, action);
  case types.CLEAR_SEARCH_ERROR:
    return clearSearchError(state, action);
  case types.LOAD_SEARCH_BAD_REQUEST:
    return loadSearchBadRequest(state, action);
  case types.LOAD_SEARCH_SERVICE_FAILURE:
    return loadSearchFail(state, action);
  case types.LOGOUT:
    return logout(state, action);
  default:
    return state;
  }
}