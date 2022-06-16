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

import * as types from "../actions/actions.types";
import { getResetFacets, constructFacets } from "../helpers/ElasticSearch/Facets";

const getFacetTypesOrder = definition => {
  const facetTypesOrder = {};
  Object.entries(definition).forEach(([type, typeDefinition]) => {
    const order = Number(typeDefinition.order);
    if (!isNaN(order)) {
      facetTypesOrder[type] = order;
    }
  });
  return facetTypesOrder;
};

const getDefaultSelectedType = (definition, facetTypesOrder) => {
  let selectedType = null;
  let defaultSelectionDefined = false;
  Object.keys(definition).forEach(type => {
    const order = Number(definition[type].order);
    if (!isNaN(order)) {
      facetTypesOrder[type] = order;
      if (definition[type].defaultSelection) {
        selectedType = type;
        defaultSelectionDefined = true;
      }
      if (!defaultSelectionDefined && (!selectedType || facetTypesOrder[type] < facetTypesOrder[selectedType])) {
        selectedType = type;
      }
    }
  });
  return selectedType;
};

const resolveType = (type, list, defaultType) => {
  const typeValue = Array.isArray(type)?type[0]:type;
  if (list.some(t => t.type === typeValue)) {
    return typeValue;
  }
  return defaultType;
};

const resolveSort = (sort, sortFields) => {
  const exists = sortFields.some(t => t.value === sort);
  if (exists) {
    return sort;
  }
  if (sortFields.length) {
    return sortFields[0].value;
  }
  return null;
};

const resolveFacets = (facets, type, params) => {
  const list = Array.isArray(facets[type])?facets[type]:[];
  list.forEach(facet => {
    const value = params[facet.name];
    if (value) {
      switch (facet.type) {
      case "list":
        facet.value = Array.isArray(value)?value:[];
        break;
      case "exists":
        facet.value = true;
        break;
      default:
        break;
      }
    }
  });
};

const resolvePage = page => {
  const pageNumber = Number(page);
  if(!isNaN(pageNumber) && pageNumber > 0) {
    return Math.floor(pageNumber);
  }
  return 1;
};

const initialState = {
  error: null,
  message: "",
  initialParams: {},
  facets: {},
  types: [],
  sort: null,
  page: 1,
  totalPages: 0,
  sortFields: [],
  initialRequestDone: false,
  isLoading: false,
  queryString: "",
  selectedType: null,
  hitsPerPage: 20,
  hits: [],
  total: 0,
  from: 0,
  isUpToDate: false
};


const getSortFields = typesDefinition => {
  const sortFields = {
    _score: { value: "newestFirst", label: "Relevance" }
  };
  if (typeof typesDefinition === "object" && !Array.isArray(typesDefinition)) {
    Object.values(typesDefinition).forEach(mapping => {
      Object.entries(mapping.fields).forEach(([name, field]) => {
        if (field.sort && sortFields[name] === undefined) {
          sortFields[name] = {value: name, label: field.label};
        }
      });
    });
  }
  return Object.values(sortFields);
};

const setupSearch = (state, action) => {
  const typeMappings = action.typeMappings;
  if (!typeMappings) {
    return state;
  }

  const sortFields = getSortFields(typeMappings);
  const facetTypesOrder = getFacetTypesOrder(typeMappings);
  const defaultType = getDefaultSelectedType(typeMappings, facetTypesOrder);
  const facets = constructFacets(typeMappings);
  const instanceTypes = Object.entries(typeMappings)
    .filter(([, typeDefinition]) => typeDefinition.searchable)
    .map(([type, typeDefinition]) => ({
      type: type,
      label: typeDefinition.name,
      count: 0
    }))
    .sort(getTypesComparatorForOrder(facetTypesOrder));

  const queryString = state.initialParams["q"]?state.initialParams["q"]:"";
  const selectedType = resolveType(state.initialParams["category"], instanceTypes, defaultType);
  const sort = resolveSort(state.initialParams["sort"], sortFields);
  const page = resolvePage(state.initialParams["p"]);
  const from = (page -1) * state.hitsPerPage;
  resolveFacets(facets, selectedType, state.initialParams);

  return {
    ...state,
    queryString:  queryString,
    facets: facets,
    types: instanceTypes,
    sort: sort,
    sortFields: sortFields,
    selectedType: selectedType,
    page: page,
    from: from
  };
};

const setQueryString = (state, action) => {
  return {
    ...state,
    queryString: action.queryString,
    page: 1,
    from: 0,
    isUpToDate: false
  };
};

const setInitialSearchParams  = (state, action) => {
  return {
    ...state,
    initialParams: action.params
  };
};

const updateListFacet = (f, action) => {
  if (!action.keyword) {
    return f;
  }
  const facet = {...f};
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
      facet.value = facet.value.filter(value => {
        if(Array.isArray(action.keyword)) {
          return !action.keyword.includes(value);
        }
        return value !== action.keyword;
      });
    }
  }
  return facet;
};

const updateExistFacet = (facet, action) => ({
  ...facet,
  value: action.active
});

const updateFacet = (facet, action) => {
  switch (facet.type) {
  case "list":
  {
    return updateListFacet(facet, action);
  }
  case "exists":
  {
    return updateExistFacet(facet, action);
  }
  default:
    return facet;
  }
};

const setFacet = (state, action) => ({
  ...state,
  facets: Object.entries(state.facets).reduce((acc, [type, list]) => {
    acc[type] = list.map(f => {
      if (type === state.selectedType && f.name === action.name) {
        return updateFacet(f, action);
      }
      return f;
    });
    return acc;
  }, {}),
  page: 1,
  from: 0,
  isUpToDate: false
});

const resetFacets = state => ({
  ...state,
  facets: getResetFacets(state.facets),
  page: 1,
  isUpToDate: false
});

const setFacetSize = (state, action) => {
  return {
    ...state,
    facets: Object.entries(state.facets).reduce((acc, [type, list]) => {
      acc[type] = list.map(f => {
        if (type === state.selectedType && f.name === action.name) {
          switch (f.type) {
          case "list":
            return {
              ...f,
              size: action.size
            };
          case "exists":
          default:
            return f;
          }
        }
        return f;
      });
      return acc;
    }, {}),
    isUpToDate: false
  };
};

const setSort = (state, action) => {
  const exists = state.sortFields.some(f => f.value === action.value);
  if (exists) {
    return {
      ...state,
      sort: action.value,
      isUpToDate: false
    };
  }
  return state;
};

const setPage = (state, action) => {
  return {
    ...state,
    page: action.value,
    from: (action.value -1) * state.hitsPerPage,
    isUpToDate: false
  };
};

const setType = (state, action) => {
  const selectedType = action.value;
  return {
    ...state,
    selectedType: selectedType,
    page: 1,
    from: 0,
    isUpToDate: false
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

const getTypesComparatorForOrder = order => (a, b) => {
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
};

const getUpdatedTypesFromResults = (instanceTypes, results) => instanceTypes.map(t => {
  const count = Number(results?.types?.[t.type]?.count);
  return {
    ...t,
    count: isNaN(count)?0:count
  };
});

const getUpdatedFacetsFromResults = (facets, selectedType, results) => {
  const aggs = (results && results.aggregations) ? results.aggregations : {};
  return Object.entries(facets).reduce((acc, [type, list]) => {
    acc[type] = list.map(f => {
      const facet = {...f};
      if (type === selectedType) {
        const res = aggs[facet.name];
        if (facet.type === "list") {
          facet.keywords = (res?.keywords)?res.keywords:[];
          facet.others =  (res?.others)?res.others:0;
        }
        facet.count = (res?.count)?res.count:0;
      }
      return facet;
    });
    return acc;
  }, {});
};

const loadSearchResult = (state, action) => {
  const total = isNaN(Number(action.results?.total))?0:Number(action.results.total);

  return {
    ...state,
    message: "",
    initialRequestDone: true,
    isLoading: false,
    facets: getUpdatedFacetsFromResults(state.facets, state.selectedType, action.results),
    types: getUpdatedTypesFromResults(state.types, action.results),
    hits: Array.isArray(action.results?.hits) ? action.results.hits : [],
    total: total,
    totalPages: Math.ceil(total / state.hitsPerPage),
    isUpToDate: true
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
    from: 0,
    isUpToDate: false
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
  case types.SET_SORT:
    return setSort(state, action);
  case types.SET_PAGE:
    return setPage(state, action);
  case types.SET_FACET:
    return setFacet(state, action);
  case types.SET_FACET_SIZE:
    return setFacetSize(state, action);
  case types.RESET_FACETS:
    return resetFacets(state);
  case types.LOAD_SEARCH_REQUEST:
    return loadSearchRequest(state);
  case types.LOAD_SEARCH_SUCCESS:
    return loadSearchResult(state, action);
  case types.CLEAR_SEARCH_ERROR:
    return clearSearchError(state);
  case types.LOAD_SEARCH_BAD_REQUEST:
    return loadSearchBadRequest(state, action);
  case types.LOAD_SEARCH_SERVICE_FAILURE:
    return loadSearchFail(state, action);
  case types.LOGOUT:
    return logout(state);
  default:
    return state;
  }
}