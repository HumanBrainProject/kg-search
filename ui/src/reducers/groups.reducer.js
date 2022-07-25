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

const DEFAULT_GROUP = "public";

const initialState = {
  useGroups: false,
  isReady: false,
  isLoading: false,
  error: null,
  groups: [],
  group: DEFAULT_GROUP,
  defaultGroup: DEFAULT_GROUP,
  initialGroup: DEFAULT_GROUP,
  hasInitialGroup: false
};

const setInitialGroup = (state, action) => {
  return {
    ...state,
    initialGroup: action.group,
    hasInitialGroup: true
  };
};

const setUseGroups = state => {
  return {
    ...state,
    useGroups: true
  };
};

const setGroup = (state, action) => {
  let group = action.group;
  if (!state.groups.some(g => g.value === group)) {
    group = state.defaultGroup;
  }
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
  const groups = Array.isArray(action.groups)
    ? [...action.groups.map(e => ({ label: e.label, value: e.name }))]
    : [];
  const group =
    state.initialGroup && groups.some(g => g.value === state.initialGroup)
      ? state.initialGroup
      : state.defaultGroup;

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
  useGroups: false,
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
  case types.SET_USE_GROUPS:
    return setUseGroups(state);
  case types.SET_GROUP:
    return setGroup(state, action);
  case types.LOAD_GROUPS_REQUEST:
    return loadGroupsRequest(state);
  case types.LOAD_GROUPS_SUCCESS:
    return loadGroupsSuccess(state, action);
  case types.LOAD_GROUPS_FAILURE:
    return loadGroupsFailure(state, action);
  case types.RESET_GROUPS:
  case types.LOGOUT_SUCCESS:
  case types.SESSION_EXPIRED:
  case types.SESSION_FAILURE:
    return resetGroups(state);
  case types.CLEAR_GROUPS_ERROR:
    return clearGroupsError(state);
  default:
    return state;
  }
}
