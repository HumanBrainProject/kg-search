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
import {
  ElasticSearchHelpers
} from "../helpers/ElasticSearchHelpers";

const initialState = {
  queryFields: ["title", "description"],
  facets: [],
  types: [],
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
  groupsSettings: {},
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
    const sort = sortFields.length ? sortFields[0] : null;
    const facetTypesOrder = ElasticSearchHelpers.getFacetTypesOrder(definition);
    const selectedType = ElasticSearchHelpers.getDefaultSelectedType(definition, facetTypesOrder);
    const facets = ElasticSearchHelpers.constructFacets(definition);
    const types = Object.entries(definition).map(([type, typeDefinition]) => ({
      type: type,
      label: typeDefinition.name,
      count: 0
    }));
    return {
      ...state,
      queryFields: queryValuesBoost,
      facets: facets,
      types: types,
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

const setFacet = (state, action) => {
  return {
    ...state,
    facets: state.facets.map(f => {
      if (f.name !== action.name) {
        return f;
      }
      if (!action.keyword) {
        return {
          ...f,
          value: action.active
        };
      }
      const facet = {
        ...f
      };
      if (action.active) {
        const values = Array.isArray(facet.value) ? facet.value : [];
        if (!values.includes(action.keyword)) {
          values.push(action.keyword);
        }
        facet.value = values;
      } else {
        if (Array.isArray(facet.value)) {
          facet.value = facet.value.filter(value => value !== action.keyword);
        }
      }
      return facet;
    })
  };
};

const resetFacets = state => ({
  ...state,
  facets: state.facets.map(f => ({
    ...f,
    value: null
  }))
});

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
    selectedType: selectedType
  };
};

const setGroupsSettings = (state, action) => {

  const groupsSettings = {};
  if (action.groups instanceof Array) {
    action.groups.forEach(group => {
      if (group.spec && group.spec.order instanceof Array && group.spec.order.length) {
        const order = {
          "$all": 0
        };
        groupsSettings[group.name] = {
          facetTypesOrder: order,
          facetDefaultSelectedType: group.spec.order[0]
        };
        group.spec.order.forEach((type, group) => order[type] = group + 1);
      }
    });
  }

  return {
    ...state,
    groupsSettings: groupsSettings
  };
};

const setGroup = (state, action) => {
  if (action && action.group) {
    if (action.group === state.group) {
      return state;
    }

    const getSelectedType = (settings, group, defaultType) => (group && settings && settings[group] && settings[group].facetDefaultSelectedType) ? settings[group].facetDefaultSelectedType : defaultType;

    const selectedType = getSelectedType(state.groupsSettings, action.group, state.facetDefaultSelectedType);

    return {
      ...state,
      hasRequest: !action.initialize,
      group: action.group,
      selectedType: selectedType
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
      return (values && Array.isArray(values.buckets)) ? values.buckets.map(bucket => ({
        value: bucket.key,
        count: bucket.doc_count
      })) : [];
    };

    const getOthers = (aggs, name) => {
      const values = aggs && aggs[`${name}.value.keyword`];
      if (!values || !values.sum_other_doc_count) {
        return 0;
      }
      const count = Number(values.sum_other_doc_count);
      return isNaN(count) ? 0 : count;
    };

    const aggs = (results && results.aggregations) ? results.aggregations : {};
    return facets.map(f => {
      const facet = {
        ...f
      };
      const res = aggs[facet.id];
      if (facet.filterType === "list") {
        const faggs = facet.isChild ? (res ? res.inner : null) : res;
        facet.keywords = getKeywords(faggs, facet.name);
        facet.others = getOthers(faggs, facet.name);
      }
      const count = (res && res.doc_count) ? res.doc_count : 0;
      facet.count = count;
      return facet;
    });
  };

  const getUpdatedTypes = (types, selectedType, group, groupsSettings, defaultOrder, results) => {

    const buckets = (results && results.aggregations && results.aggregations.facet_type && results.aggregations.facet_type._type && Array.isArray(results.aggregations.facet_type._type.buckets)) ?
      results.aggregations.facet_type._type.buckets : [];

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

  return {
    ...state,
    initialRequestDone: true,
    isLoading: false,
    nonce: null,
    group: action.group ? action.group : state.group,
    facets: getUpdatedFacets(state.facets, action.results),
    types: getUpdatedTypes(state.types, state.selectedType, state.group, state.groupsSettings, state.facetTypesOrder, action.results),
    hits: (action.results && action.results.hits && Array.isArray(action.results.hits.hits)) ? action.results.hits.hits : [],
    total: (action.results && action.results.hits && action.results.hits.total) ? action.results.hits.total : 0,
    from: action.from ? Number(action.from) : 0
  };
};

const loadSearchFail = state => {
  return {
    ...state,
    isLoading: false,
    nonce: null,
    group: state.group,
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
  case types.SET_FACET:
    return setFacet(state, action);
  case types.RESET_FACETS:
    return resetFacets(state, action);
  case types.SET_SEARCH_READY:
    return setSearchReady(state, action);
  case types.LOAD_GROUPS:
    return setGroupsSettings(state, action);
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
    return setGroup(state, {
      group: API.defaultGroup
    });
  case types.LOAD_GROUPS_SUCCESS:
    return loadGroupsSuccess(state, action);
  case types.LOAD_GROUPS_FAILURE:
    return loadGroupsFailure(state, action);
  default:
    return state;
  }
}