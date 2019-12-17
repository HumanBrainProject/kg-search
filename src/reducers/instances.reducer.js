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
  error: null,
  isLoading: false,
  currentInstance: null,
  previousInstances: [],
  image: null
};

const loadInstanceRequest = state => {
  return {
    ...state,
    isLoading: true,
    image: null
  };
};

const loadInstanceSuccess = (state, action) => {
  let previousInstances = Array.isArray(state.previousInstances)?state.previousInstances:[];
  previousInstances = state.currentInstance?[...previousInstances,state.currentInstance]:[...previousInstances];
  return  {
    ...state,
    isLoading: false,
    currentInstance: action.data,
    previousInstances: previousInstances,
    image: null
  };
};

const loadInstanceNoData = (state, action) => {
  return {
    ...state,
    error: action.error,
    isLoading: false,
    image: null
  };
};

const loadInstanceFailure = (state, action) => {
  return {
    ...state,
    error: action.error,
    isLoading: false,
    image: null
  };
};

const setInstance = (state, action) => {
  let previousInstances = Array.isArray(state.previousInstances)?state.previousInstances:[];
  previousInstances = state.currentInstance?[...previousInstances,state.currentInstance]:[...previousInstances];
  return {
    ...state,
    isLoading: false,
    currentInstance: action.data,
    previousInstances: previousInstances,
    image: null
  };
};

const setPreviousInstance = state => {
  if (state.currentInstance) {
    const previousInstances = Array.isArray(state.previousInstances)?[...state.previousInstances]:[];
    const currentInstance = previousInstances.pop() || null;
    return {
      ...state,
      isLoading: false,
      currentInstance: currentInstance,
      previousInstances: previousInstances,
      image: null
    };
  }

  return state;
};

const clearAllInstances = state => {
  return {
    ...state,
    isLoading: false,
    currentInstance: null,
    previousInstances: [],
    image: null
  };
};

const clearInstanceError = state => {
  return {
    ...state,
    error: null
  };
};

const showImage = (state, action) => {
  return {
    ...state,
    image: typeof action.url === "string"?{url: action.url, label: action.label}:null
  };
};

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.LOAD_INSTANCE_REQUEST:
    return loadInstanceRequest(state, action);
  case types.LOAD_INSTANCE_SUCCESS:
    return loadInstanceSuccess(state, action);
  case types.LOAD_INSTANCE_NO_DATA:
    return loadInstanceNoData(state, action);
  case types.LOAD_INSTANCE_FAILURE:
    return loadInstanceFailure(state, action);
  case types.SET_INSTANCE:
    return setInstance(state, action);
  case types.SET_PREVIOUS_INSTANCE:
    return setPreviousInstance(state, action);
  case types.CLEAR_ALL_INSTANCES:
    return clearAllInstances(state, action);
  case types.CLEAR_INSTANCE_ERROR:
    return clearInstanceError(state);
  case types.SHOW_IMAGE:
    return showImage(state, action);
  default:
    return state;
  }
}