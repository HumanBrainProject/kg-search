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

const initialState = {
  error: null,
  isInitialized: false,
  isLoading: false,
  files: [],
  total: 0,
  searchAfter: null
};

const loadFilesRequest = state => {
  return {
    ...state,
    isInitialized: true,
    isLoading: true,
    files: [],
    total: 0,
    searchAfter: null,
    error: null
  };
};

const loadFilesSuccess = (state, action) => {
  return  {
    ...state,
    files: action.files,
    total: action.total,
    searchAfter: action.searchAfter,
    isLoading: false,
    error: null
  };
};

const loadFilesFailure = (state, action) => {
  return {
    ...state,
    error: action.error,
    isLoading: false,
    files: []
  };
};

const clearFiles = state => {
  return {
    ...state,
    error: null,
    isInitialized: false,
    isLoading: false,
    files: []
  };
};

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.LOAD_FILES_REQUEST:
    return loadFilesRequest(state, action);
  case types.LOAD_FILES_SUCCESS:
    return loadFilesSuccess(state, action);
  case types.LOAD_FILES_FAILURE:
    return loadFilesFailure(state, action);
  case types.CLEAR_LOAD_FILES:
    return clearFiles(state, action);
  default:
    return state;
  }
}