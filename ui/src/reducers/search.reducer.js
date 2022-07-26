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
import { getResetFacets, constructFacet } from "../helpers/Facets";

const resolveType = (type, types) => {
  const value = Array.isArray(type)?type[0]:type;
  let defaultType = null;
  let selectedType = null;
  types.some(t => {
    if (!defaultType) {
      defaultType = t;
    }
    if (t.defaultSelection) {
      defaultType = t;
    }
    if (t.type === value) {
      selectedType = t;
      return true;
    }
    return false;
  });
  if (selectedType) {
    return selectedType;
  }
  return defaultType;
};

const resolveFacets = (facets, params) => {
  if (!Array.isArray(facets)) {
    return;
  }
  facets.forEach(facet => {
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
  types: [],
  page: 1,
  totalPages: 0,
  isInitialized: false,
  isLoading: false,
  queryString: "",
  selectedType: null,
  hitsPerPage: 20,
  hits: [],
  suggestions: {},
  total: 0,
  from: 0,
  isUpToDate: false
};

const setupSearch = (state, action) => {
  if (!action.types) {
    return state;
  }

  const instanceTypes = Array.isArray(action.types)?action.types.map(t => {
    const instanceType = {
      ...t,
      count: 0
    };
    if (Array.isArray(t.facets)) {
      instanceType.facets = t.facets.map(f => constructFacet(f));
    }
    return instanceType;
  }):[];

  return {
    ...state,
    types: instanceTypes
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

const initializeSearch = (state, action) => {
  const {q, category, p} = (action?.params instanceof Object)?action.params:{};

  const queryString = q?q:"";
  const selectedType = resolveType(category, state.types);
  const page = resolvePage(p);
  const from = (page -1) * state.hitsPerPage;
  resolveFacets(selectedType?.facets, action.params);

  return {
    ...state,
    queryString:  queryString,
    selectedType: selectedType,
    page: page,
    from: from
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

const setFacet = (state, action) => {
  if (Array.isArray(state.selectedType?.facets)) {
    const types = state.types.map(t => {
      if (t.type === state.selectedType.type) {
        return {
          ...t,
          facets: state.selectedType.facets.map(f => {
            if ( f.name === action.name) {
              return updateFacet(f, action);
            }
            return f;
          })
        };
      }
      return t;
    });
    return {
      ...state,
      types: types,
      selectedType: state.selectedType?types.find(t => t.type === state.selectedType?.type):null,
      page: 1,
      from: 0,
      isUpToDate: false
    };
  }
  return state;
};

const resetFacets = state => {
  const types = state.types.map(t => {
    if (Array.isArray(t.facets)) {
      return {
        ...t,
        facets: getResetFacets(t.facets),
      };
    }
    return t;
  });
  return {
    ...state,
    types: types,
    selectedType: state.selectedType?types.find(t => t.type === state.selectedType?.type):null,
    isUpToDate: false
  };
};

const setFacetSize = (state, action) => {
  if (Array.isArray(state.selectedType?.facets)) {
    const types = state.types.map(t => {
      if (t.type === state.selectedType.type) {
        return {
          ...t,
          facets: state.selectedType.facets.map(f => {
            if (f.name === action.name) {
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
          })
        };
      }
      return t;
    });
    return {
      ...state,
      types: types,
      selectedType: state.selectedType?types.find(t => t.type === state.selectedType?.type):null,
      isUpToDate: false
    };
  }
  return state;
};

const setPage = (state, action) => {
  return {
    ...state,
    page: action.value,
    from: (action.value - 1) * state.hitsPerPage,
    isUpToDate: false
  };
};

const setType = (state, action) => {
  const selectedType = state.types.find(t => t.type === action.value);
  if (selectedType) {
    return {
      ...state,
      selectedType: selectedType,
      page: 1,
      from: 0,
      isUpToDate: false
    };
  }
  return state;
};

const loadSearchRequest = state => {
  return {
    ...state,
    isInitialized: true,
    isLoading: true,
    error: null,
    message: ""
  };
};

const getUpdatedTypesFromResults = (instanceTypes, selectedType, results) => instanceTypes.map(t => {
  const count = Number(results?.types?.[t.type]?.count);
  const instanceType = {
    ...t,
    count: isNaN(count)?0:count
  };
  if (Array.isArray(t.facets)) {
    instanceType.facets = getUpdatedFacetsFromResults(t.facets, t.type === selectedType, results);
  }
  return instanceType;
});

const getUpdatedFacetsFromResults = (facets, isSelectedType, results) => {
  const aggs = (results?.aggregations) ? results.aggregations : {};
  return facets.map(f => {
    const facet = {...f};
    if (isSelectedType) {
      const res = aggs[facet.name];
      if (facet.type === "list") {
        facet.keywords = (res?.keywords)?res.keywords:[];
        facet.others =  (res?.others)?res.others:0;
      }
      facet.count = (res?.count)?res.count:0;
    }
    return facet;
  });
};

const loadSearchResult = (state, action) => {
  const total = isNaN(Number(action.results?.total))?0:Number(action.results.total);
  const types = getUpdatedTypesFromResults(state.types, state.selectedType?.type, action.results);
  return {
    ...state,
    message: "",
    isLoading: false,
    types: types,
    selectedType: state.selectedType?types.find(t => t.type === state.selectedType?.type):null,
    hits: Array.isArray(action.results?.hits) ? action.results.hits : [],
    suggestions: action.results.suggestions,
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
    suggestions: {},
    from: 0,
    page: 1,
    totalPages: 0,
    isUpToDate: true
  };
};

const loadSearchFail = (state, action) => {
  return {
    ...state,
    message: "",
    error: action.error,
    isLoading: false,
    hits: [],
    suggestions: {},
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

const abortLoadSearch = state => {
  return {
    ...state,
    isLoading: false
  };
};

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.INITIALIZE_SEARCH:
    return initializeSearch(state, action);
  case types.LOAD_SETTINGS_SUCCESS:
    return setupSearch(state, action);
  case types.SET_QUERY_STRING:
    return setQueryString(state, action);
  case types.SET_TYPE:
    return setType(state, action);
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
  case types.SESSION_EXPIRED:
  case types.SESSION_FAILURE:
    return abortLoadSearch(state, action);
  default:
    return state;
  }
}