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

import API from "../services/API";
import * as types from "../actions.types";
import { ElasticSearchHelpers } from "../helpers/ElasticSearchHelpers";

const initialState = {
  queryFields: ["title", "description"],
  facets: [],
  types: [],
  hasFilters: false,
  sort: null,
  sortFields: [],
  facetTypesOrder: {},
  facetDefaultSelectedType: null,
  isReady: false,
  initialRequestDone: false,
  hasRequest: false,
  isLoading: false,
  queryString: "",
  selectedType: null,
  nonce: null,
  group: API.defaultGroup,
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
    const sort = sortFields.length?sortFields[0]:null;
    const facetTypesOrder = ElasticSearchHelpers.getFacetTypesOrder(definition);
    const selectedType = ElasticSearchHelpers.getDefaultSelectedType(definition, facetTypesOrder);
    const facets = ElasticSearchHelpers.constructFacets(definition, selectedType);
    const types = Object.entries(definition).map(([type, typeDefinition]) => ({
      type: type,
      label: typeDefinition.name,
      count: 0,
      active: type === selectedType,
      visible: true
    }));
    const hasFilters = facets.some(f => f.visible);

    return {
      ...state,
      queryFields: queryValuesBoost,
      facets: facets,
      types: types,
      hasFilters: hasFilters,
      sort: sort,
      sortFields: sortFields,
      facetTypesOrder: facetTypesOrder,
      selectedType: selectedType,
      facetDefaultSelectedType: selectedType
    };
  }
};

const setSearchReady = (state, action) => {
  return {
    ...state,
    isReady: action.isReady
  };
};

const setQueryString = (state, action) => {
  return {
    ...state,
    queryString: action.queryString
  };
};

const getUpdatedTypes = (types, selectedType) => {
  return Array.isArray(types)?types.map(type => {
    if(type.type === selectedType) {
      const obj = Object.assign({}, type);
      obj.active = true;
      return obj;
    } else if(type.active) {
      const obj = Object.assign({}, type);
      obj.active = false;
      return obj;
    }
    return type;
  }):[];
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

const setType = (state, action) => {
  const selectedType = action.value;
  return {
    ...state,
    selectedType: selectedType,
    types: getUpdatedTypes(state.types, selectedType)
  };
};

const setGroup = (state, action) => {
  if (action && action.group) {
    if (action.group === state.group) {
      return state;
    }

    const getSelectedType = (groups, group, defaultType) => {
      if (group && groups && groups.groups && groups.groups.length && groups.groupSettings && groups.groupSettings[group]) {
        return groups.groupSettings[group].facetDefaultSelectedType;
      }
      return defaultType;
    };

    const selectedType = getSelectedType(state.groups, action.group, state.facetDefaultSelectedType);

    return {
      ...state,
      hasRequest: !action.initialize,
      group: action.group,
      selectedType: selectedType,
      types: getUpdatedTypes(state.types, selectedType)
    };
  }
  // Reset
  if (state.group === API.defaultGroup) {
    return state;
  }
  return {
    ...state,
    hasRequest: !action.initialize,
    group: API.defaultGroup
  };
};

const loadGroupsSuccess = (state, action) => {
  if (action.groups instanceof Array && action.groups.some(e => e.name === state.group)) {
    return state;
  }
  if (state.group === API.defaultGroup) {
    return state;
  }
  return {
    ...state,
    group: API.defaultGroup
  };
};

const loadGroupsFailure = state => {
  if (state.group === API.defaultGroup) {
    return state;
  }
  return {
    ...state,
    group: API.defaultGroup
  };
};

const loadSearch = state => {
  return {
    ...state,
    hasRequest: !state.isLoading
  };
};

const loadSearchRequest = (state, action) => {
  return {
    ...state,
    isLoading: true,
    hasRequest: false,
    nonce: action.nonce
  };
};

const loadSearchResult = (state, action) => {

  const getUpdatedFacets = (facets, results) => {

    const getKeywords = (aggs, name) => {
      const values = aggs && aggs[`${name}.value.keyword`];
      return (values && Array.isArray(values.buckets))?values.buckets.map(bucket => ({
        value: bucket.key,
        count: bucket.doc_count
      })):[];
    };
    const aggs = (results && results.aggregations)?results.aggregations:{};
    return facets.map(f => {
      const facet = Object.assign({}, f);
      const res = aggs[facet.id];
      if (facet.facet.filterType === "list") {
        facet.keywords = getKeywords(facet.facet.isChild?(res?res.inner:null):res, facet.name);
        // TODO: sum_other_doc_count
      }
      const count = (res && res.doc_count)?res.doc_count:0;
      facet.facet.count = count;
      facet.visible = count > 0;
      return facet;
    });
  };

  const getUpdatedTypes = (types, selectedType, results) => {
    const buckets = (results && results.aggregations && results.aggregations.facet_type && results.aggregations.facet_type._type && Array.isArray(results.aggregations.facet_type._type.buckets))?results.aggregations.facet_type._type.buckets:[];
    const counts = buckets.reduce((acc, current) => {
      acc[current.key] = current.doc_count;
      return acc;
    }, {});
    return types.map(type => {
      const res = Object.assign({}, type);
      const count = counts[res.type]?counts[res.type]:0;
      res.count = count;
      res.visible = count > 0 || res.type === selectedType;
      return res;
    });
  }

  return {
    ...state,
    initialRequestDone: true,
    isLoading: false,
    nonce: null,
    group:action.group?action.group:state.group,
    facets: getUpdatedFacets(state.facets, action.results),
    types: getUpdatedTypes(state.types, state.selectedType, action.results),
    hits: (action.results && action.results.hits && Array.isArray(action.results.hits.hits))?action.results.hits.hits:[],
    total: (action.results && action.results.hits && action.results.hits.total)?action.results.hits.total:0,
    from: action.from?Number(action.from):0
  };
};

const loadSearchFail = state => {
  return {
    ...state,
    isLoading: false,
    nonce: null,
    group:state.group,
    results: [],
    from: 0
  };
};

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.LOAD_DEFINITION_SUCCESS:
    return setupSearch(state, action);
  case types.SET_QUERY_STRING:
    return setQueryString(state, action);
  case types.SET_TYPE:
    return setType(state, action);
  case types.SET_SORT:
    return setSort(state, action);
  case types.SET_SEARCH_READY:
    return setSearchReady(state, action);
  case types.SET_GROUP:
    return setGroup(state, action);
  case types.LOAD_SEARCH:
    return loadSearch(state, action);
  case types.LOAD_SEARCH_REQUEST:
    return loadSearchRequest(state, action);
  case types.LOAD_SEARCH_SUCCESS:
    return loadSearchResult(state, action);
  case types.LOAD_SEARCH_BAD_REQUEST:
  case types.LOAD_SEARCH_SESSION_FAILURE:
    return loadSearchFail(state, action);
  case types.LOGOUT:
    return setGroup(state, {group: API.defaultGroup});
  case types.LOAD_GROUPS_SUCCESS:
    return loadGroupsSuccess(state, action);
  case types.LOAD_GROUPS_FAILURE:
    return loadGroupsFailure(state, action);
  default:
    return state;
  }
}