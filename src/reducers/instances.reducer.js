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
  currentInstanceId: null,
  currentInstanceType: null,
  isLoading: false,
  currentInstance: null,
  previousInstances: [],
  image: null
};

const loadInstanceRequest = (state, action) => {
  return {
    ...state,
    currentInstanceType: action.instanceType,
    currentInstanceId: action.instanceId,
    isLoading: true,
    image: null
  };
};

const loadInstanceSuccess = (state, action) => {
  let previousInstances = (state && Array.isArray(state.previousInstances))?state.previousInstances:[];
  previousInstances = (state && state.currentInstance)?[...previousInstances,state.currentInstance]:[...previousInstances];
  return  {
    ...state,
    isLoading: false,
    currentInstance: action.data,
    previousInstances: previousInstances,
    image: null
  };
};

const loadInstanceNoData = state => {
  return {
    ...state,
    error: "The instance is not available",
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
  let previousInstances = (state && Array.isArray(state.previousInstances))?state.previousInstances:[];
  previousInstances = (state && state.currentInstance)?[...previousInstances,state.currentInstance]:[...previousInstances];
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
    const previousInstances = (state && Array.isArray(state.previousInstances))?[...state.previousInstances]:[];
    const currentInstance = previousInstances.pop() || null;
    return {
      ...state,
      isLoading: false,
      currentInstanceType: (currentInstance && currentInstance._type)?currentInstance._type:null,
      currentInstanceId: (currentInstance && currentInstance._id)?currentInstance._id:null,
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
    currentInstanceType: null,
    currentInstanceId: null,
    currentInstance: null,
    previousInstances: [],
    image: null
  };
};

const setCurrentInstanceFromBrowserLocation = state => {
  let instance = state && state.currentInstance;

  // if no current instance
  if (!instance) {
    return state;
  }

  const [,,instanceType, instanceId] = window.location.href.match(/#((.+)\/(.+))$/) || [];

  // no instance reference available in url, unset current instance
  if (!instanceType || !instanceId) {
    return {
      ...state,
      isLoading: false,
      currentInstanceType: null,
      currentInstanceId: null,
      currentInstance: null,
      previousInstances: [],
      image: null
    };
  }

  // instance reference url is already matching current instance, do notthing
  if (instance && instance._type === instanceType && instance._id === instanceId) {
    return state;
  }

  // no previous instances available, unset current instance
  if (!state || !state.previousInstances.length) {
    return {
      ...state,
      isLoading: false,
      currentInstanceType: null,
      currentInstanceId: null,
      currentInstance: null,
      previousInstances: [],
      image: null
    };
  }

  const previousInstances = (state && Array.isArray(state.previousInstances))?[...state.previousInstances]:[];
  instance = previousInstances.pop() || null;
  while(previousInstances.length && instance && !(instance._type === instanceType && instance._id === instanceId))  {
    instance = previousInstances.pop();
  }
  if (instance && instance._type === instanceType && instance._id === instanceId) {
    return {
      ...state,
      isLoading: false,
      currentInstanceType: (instance && instance._type)?instance._type:null,
      currentInstanceId: (instance && instance._id)?instance._id:null,
      currentInstance: instance,
      previousInstances: previousInstances,
      image: null
    };
  }

  return {
    ...state,
    isLoading: false,
    currentInstanceType: null,
    currentInstanceId: null,
    currentInstance: null,
    previousInstances: [],
    image: null
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
  case types.SET_CURRENT_INSTANCE_FROM_BROWSER_LOCATION:
    return setCurrentInstanceFromBrowserLocation(state, action);
  case types.SHOW_IMAGE:
    return showImage(state, action);
  default:
    return state;
  }
}