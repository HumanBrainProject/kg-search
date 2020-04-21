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

import * as types from "../actions/actions.types";

const DEFAULT_GROUP = "public";

const initialState = {
  isReady: false,
  isLoading: false,
  error: null,
  groups: [],
  group: DEFAULT_GROUP,
  defaultGroup: DEFAULT_GROUP,
  initialGroup: DEFAULT_GROUP
};

const setInitialGroup = (state, action) => {
  return {
    ...state,
    initialGroup: action.group
  };
};

const setGroup = (state, action) => {
  return {
    ...state,
    group: action.group
  };
};

const loadGroupsRequest = state => {
  return {
    ...state,
    isReady: false,
    isLoading: true,
    error: null
  };
};

const loadGroupsSuccess = (state, action) => {

  const getGroupLabel = name => {
    if (name === "public") {
      return "publicly released";
    }
    if (name === "curated") {
      return "in progress";
    }
    return name;
  };
  const groups = Array.isArray(action.groups) ? [...action.groups.map(e => ({ label: getGroupLabel(e.name), value: e.name }))] : [];
  const group = (state.initialGroup && groups.some(g => g.value === state.initialGroup)) ? state.initialGroup : state.defaultGroup;
  debugger;
  return {
    ...state,
    isReady: true,
    isLoading: false,
    groups: groups,
    group: group
  };
};

const loadGroupsFailure = (state, action) => ({
  ...state,
  isLoading: false,
  error: action.error
});


const resetGroups = state => ({
  ...state,
  groups: [],
  group: DEFAULT_GROUP
});

const clearGroupsError = state => {
  return {
    ...state,
    isReady: false,
    error: null,
    group: DEFAULT_GROUP
  };
};

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
    case types.SET_INITIAL_GROUP:
      return setInitialGroup(state, action);
    case types.SET_GROUP:
      return setGroup(state, action);
    case types.LOAD_GROUPS_REQUEST:
      return loadGroupsRequest(state, action);
    case types.LOAD_GROUPS_SUCCESS:
      return loadGroupsSuccess(state, action);
    case types.LOAD_GROUPS_FAILURE:
      return loadGroupsFailure(state, action);
    case types.LOGOUT:
      return resetGroups(state, action);
    case types.CLEAR_GROUPS_ERROR:
      return clearGroupsError(state, action);
    default:
      return state;
  }
}