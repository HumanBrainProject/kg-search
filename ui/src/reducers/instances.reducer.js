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
    image: null,
    error: null
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
    currentInstance: null,
    previousInstances: [],
    image: null,
    error: null
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
    image: typeof action.url === "string"?{url: action.url, label: action.label, link: action.link}:null
  };
};

const goBackToInstance = (state, action) => {
  let instance = state && state.currentInstance;

  // if no current instance
  if (!instance) {
    return state;
  }

  const instanceId = action.id;

  // no instance reference available in url, unset current instance
  if (!instanceId) {
    return {
      ...state,
      currentInstance: null,
      previousInstances: [],
      image: null
    };
  }

  // instance reference url is already matching current instance, do notthing
  if (instance && instance._id === instanceId) {
    return state;
  }

  // no previous instances available, unset current instance
  if (!state || !state.previousInstances.length) {
    return {
      ...state,
      currentInstance: null,
      previousInstances: [],
      image: null
    };
  }

  const previousInstances = (state && state.previousInstances instanceof Array)?[...state.previousInstances]:[];
  instance = previousInstances.pop() || null;
  while(previousInstances.length && instance && instance._id !== instanceId)  {
    instance = previousInstances.pop();
  }
  if (instance && instance._id === instanceId) {
    return {
      ...state,
      currentInstance: instance,
      previousInstances: previousInstances,
      image: null
    };
  }

  return {
    ...state,
    currentInstance: null,
    previousInstances: [],
    image: null
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
  case types.GO_BACK_TO_INSTANCE:
    return goBackToInstance(state, action);
  default:
    return state;
  }
}