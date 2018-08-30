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

import * as types from "../actions.types";

const default_index = "public";

const initialState = {
  isReady: false,
  initialRequestDone: false,
  hasRequest: false,
  isLoading: false,
  nonce: null,
  index: default_index,
  results: {},
  from: 0
};

const setSearchReady = (state, action) => {
  return Object.assign({}, state, {
    isReady: action.isReady
  });
};

const setIndex = (state, action) => {
  return Object.assign({}, state, {hasRequest: action.index !== state.index, index: action.index});
};

const loadSearch = state => {
  return Object.assign({}, state, {hasRequest: !state.isLoading});
};

const loadSearchRequest = (state, action) => {
  return Object.assign({}, state, {isLoading: true, hasRequest: false, nonce: action.nonce});
};

const loadSearchResult = (state, action) => {
  return Object.assign({}, state, {initialRequestDone: true, isLoading: false, nonce: null, index:action.index?action.index:state.index, results: action.results, from: action.from?Number(action.from):0});
};

const loadSearchFail = state => {
  return Object.assign({}, state, {isLoading: false, nonce: null, index:state.index, results: [], from: 0});
};

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.SET_SEARCH_READY:
    return setSearchReady(state, action);
  case types.SET_INDEX:
    return setIndex(state, action);
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
    return setIndex(state, {index: default_index});
  default:
    return state;
  }
}