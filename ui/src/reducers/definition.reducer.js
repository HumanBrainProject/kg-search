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
  isReady: false,
  isLoading: false,
  error: null,
  typeMappings: {}
};

const loadDefinitionRequest = state => {
  return {
    ...state,
    isReady: false,
    isLoading: true,
    error: null
  };
};

const loadDefinitionSuccess = (state, action) => {
  return {
    ...state,
    isReady: true,
    isLoading: false,
    typeMappings: action.definition
  };
};

const loadDefinitionFailure = (state, action) => {
  return {
    ...state,
    isLoading: false,
    error: action.error
  };
};

const clearDefinitionError = state => {
  return {
    ...state,
    error: null
  };
};

const setCommit = (state, action) => {
  return {
    ...state,
    commit: action.commit
  };
};


export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.LOAD_DEFINITION_REQUEST:
    return loadDefinitionRequest(state, action);
  case types.LOAD_DEFINITION_SUCCESS:
    return loadDefinitionSuccess(state, action);
  case types.LOAD_DEFINITION_FAILURE:
    return loadDefinitionFailure(state, action);
  case types.CLEAR_DEFINITION_ERROR:
    return clearDefinitionError(state, action);
  case types.SET_COMMIT:
    return setCommit(state, action);
  default:
    return state;
  }
}