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
import { createSlice } from '@reduxjs/toolkit';

import { resetFacet, constructFacet } from '../../helpers/Facets';
import { api } from '../../services/api';


const resolveType = (type, list) => {
  const value = Array.isArray(type)?type[0]:type;
  let defaultType = null;
  let selectedType = null;
  list.some(t => {
    if (!defaultType) {
      defaultType = t;
    }
    if (t.defaultSelection) {
      defaultType = t.type;
    }
    if (t.type === value) {
      selectedType = t.type;
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
      case 'list':
        facet.value = Array.isArray(value)?value:[];
        break;
      case 'exists':
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

const getFacet = (types, typeName, facetName) => {
  const type = types.find(t => t.type === typeName);
  if (!Array.isArray(type?.facets)) {
    return null;
  }
  return type.facets.find(f => f.name === facetName);
};

const updateListFacet = (facet, payload) => {
  if (payload.keyword) {
    if (payload.active) {
      const values = Array.isArray(facet.value) ? facet.value : [];
      if (Array.isArray(payload.keyword)) {
        payload.keyword.forEach(keyword => {
          if (!values.includes(keyword)) {
            values.push(keyword);
          }
        });
      } else if (!values.includes(payload.keyword)) {
        values.push(payload.keyword);
      }
      facet.value = values;
    } else {
      if (Array.isArray(facet.value)) {
        facet.value = facet.value.filter(value => {
          if(Array.isArray(payload.keyword)) {
            return !payload.keyword.includes(value);
          }
          return value !== payload.keyword;
        });
      }
    }
  }
};

const updateExistFacet = (facet, payload) => {
  facet.value = !!(payload?.active);
};

const updateFacet = (facet, payload) => {
  if (facet.type === 'list') {
    updateListFacet(facet, payload);
  } else if (facet.type === 'exists') {
    updateExistFacet(facet, payload);
  }
};

const updateFacetsFromResults = (facets, isSelectedType, results) => {
  const aggs = (results?.aggregations)?results.aggregations:{};
  facets.forEach(facet => {
    if (isSelectedType) {
      const res = aggs[facet.name];
      if (facet.type === 'list') {
        facet.keywords = (res?.keywords)?res.keywords:[];
        facet.others =  (res?.others)?res.others:0;
      }
      facet.count = (res?.count)?res.count:0;
    }
  });
};

const updateTypesFromResults = (types, selectedType, results) => {
  types.forEach(type => {
    const count = Number(results?.types?.[type.type]?.count);
    type.count = isNaN(count)?0:count;
    if (Array.isArray(type.facets)) {
      updateFacetsFromResults(type.facets, type.type === selectedType, results);
    }
  });
};

const resetAllFacets = state => {
  state.types.forEach(type => {
    if (Array.isArray(type.facets)) {
      type.facets.forEach(facet => resetFacet(facet));
    }
  });
};

const syncParameters = (state, payload) => {
  const {q, category, p} = (payload instanceof Object)?payload:{};

  const queryString = q??'';
  const selectedType = resolveType(category, state.types);
  const page = resolvePage(p);
  const from = (page -1) * state.hitsPerPage;
  const type = state.types.find(t => t.type === selectedType);
  resetAllFacets(state);
  resolveFacets(type?.facets, payload);
  state.queryString = queryString;
  state.selectedType = selectedType;
  state.page = page;
  state.from = from;
};

const initialState = {
  types: [],
  page: 1,
  totalPages: 0,
  isInitialized: false,
  isFetching: false,
  queryString: '',
  selectedType: null,
  hitsPerPage: 20,
  hits: [],
  suggestions: {},
  total: 0,
  from: 0,
  isUpToDate: false
};

const searchSlice = createSlice({
  name: 'search',
  initialState,
  reducers: {
    initializeSearch(state, action) {
      syncParameters(state, action.payload);
      state.isInitialized = true;
    },
    syncSearchParameters(state, action) {
      syncParameters(state, action.payload);
      state.isUpToDate = false;
    },
    setQueryString(state, action) {
      state.queryString = action.payload;
      state.page = 1;
      state.from = 0;
      state.isUpToDate = false;
    },
    setFacet(state, action) {
      const facet = getFacet(state.types, state.selectedType, action.payload.name);
      if (facet) {
        updateFacet(facet, action.payload);
        state.page = 1;
        state.from = 0;
        state.isUpToDate = false;
      }
    },
    resetFacets(state) {
      resetAllFacets(state);
      state.page = 1;
      state.from = 0;
      state.isUpToDate = false;
    },
    setFacetSize(state, action) {
      const facet = getFacet(state.types, state.selectedType, action.payload.name);
      if (facet) {
        if (facet.type === 'list') {
          facet.size = action.payload.size;
          state.isUpToDate = false;
        }
      }
    },
    setPage(state, action) {
      state.page = action.payload;
      state.from =  (action.payload - 1) * state.hitsPerPage;
      state.isUpToDate = false;
    },
    setType(state, action) {
      const type = state.types.find(t => t.type === action.payload);
      if (type) {
        state.selectedType = type.type;
        state.page = 1;
        state.from = 0;
        state.isUpToDate = false;
      }
    },
    setSearchResults(state, action) {
      const results = action.payload;
      updateTypesFromResults(state.types, state.selectedType, results);
      state.hits = Array.isArray(results?.hits)?results.hits:[];
      state.suggestions = (results?.suggestions instanceof Object)?results.suggestions:{};

      const total = isNaN(Number(results?.total))?0:Number(results.total);
      state.total = total;
      state.totalPages = Math.ceil(total / state.hitsPerPage);
    }
  },
  extraReducers(builder) {
    builder
      .addMatcher(
        api.endpoints.getSettings.matchFulfilled,
        (state, { payload }) => {
          state.types = Array.isArray(payload?.types)?payload.types.map(t => {
            const instanceType = {
              ...t,
              count: 0
            };
            if (Array.isArray(t.facets)) {
              instanceType.facets = t.facets.map(f => constructFacet(f));
            }
            return instanceType;
          }):[];
        }
      )
      .addMatcher(
        api.endpoints.getSearch.matchFulfilled,
        state => {
          state.isUpToDate = true;
          state.isFetching = false;
        }
      )
      .addMatcher(
        api.endpoints.getSearch.matchPending,
        state => {
          state.isFetching = true;
        }
      )
      .addMatcher(
        api.endpoints.getSearch.matchRejected,
        state => {
          state.hits = [];
          state.suggestions = {};
          state.from = 0;
          state.page = 1;
          state.totalPages = 0;
          state.isFetching = false;
        }
      );
  }
});

export const selectType = (state, typeName) => state.search.types.find(t => t.type === typeName);

export const selectFacets = (state, typeName) => {
  const type = selectType(state, typeName);
  if (!Array.isArray(type?.facets)) {
    return [];
  }
  return type.facets;
};

export const { initializeSearch, syncSearchParameters, setQueryString, setFacet, resetFacets, setFacetSize, setPage, setType, setSearchResults} = searchSlice.actions;

export default searchSlice.reducer;