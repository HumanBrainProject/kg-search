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

const initialState = {
  isReady: false,
  hasRequest: false,
  isLoading: false,
  hasError: false,
  indexes: []
};

const loadIndexes = (state) => {
  return Object.assign({}, state, {
    hasRequest: true,
    isLoading: false,
    hasError: false
  });
};

const loadIndexesRequest = (state) => {
  return Object.assign({}, state, {
    hasRequest: false,
    isLoading: true,
    hasError: false
  });
};

const loadIndexesSuccess  = (state, action) => {

  const indexes = (action.indexes instanceof Array)?[...action.indexes.map(e => ({label: e, value: e}))]:[];

  return Object.assign({}, state, {
    isReady: true,
    hasRequest: false,
    isLoading: false,
    hasError: false,
    indexes: indexes
  });
};

const loadIndexesFailure  = state => {
  return Object.assign({}, state, {
    isReady: false,
    hasRequest: false,
    isLoading: false,
    hasError: true,
    indexes: []
  });
};

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.LOAD_INDEXES:
    return loadIndexes(state, action);
  case types.LOAD_INDEXES_REQUEST:
    return loadIndexesRequest(state, action);
  case types.LOAD_INDEXES_SUCCESS:
    return loadIndexesSuccess(state, action);
  case types.LOAD_INDEXES_FAILURE:
    return loadIndexesFailure(state, action);
  case types.LOGOUT:
    return loadIndexesSuccess(state, {indexes: []});
  default:
    return state;
  }
}