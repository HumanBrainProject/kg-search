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
  hasFilters: false,
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
  results: {},
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
    const selectedType = ElasticSearchHelpers.getDefaultSelectedType(definition, facetTypesOrder);
    const facets = ElasticSearchHelpers.constructFacets(definition, selectedType);
    const hasFilters = facets.some(f => f.isVisible);

    return {
      ...state,
      queryFields: queryValuesBoost,
      facets: facets,
      hasFilters: hasFilters,
      sortFields: sortFields,
      facetTypesOrder: facetTypesOrder,
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

const setQueryString= (state, action) => {
  return {
    ...state,
    queryString: action.queryString
  };
};

const setGroup = (state, action) => {
  if (action && action.group) {
    if (action.group === state.group) {
      return state;
    }
    return {
      ...state,
      hasRequest: !action.initialize,
      group: action.group
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
  return {
    ...state,
    initialRequestDone: true,
    isLoading: false,
    nonce: null,
    group:action.group?action.group:state.group,
    results: action.results,
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