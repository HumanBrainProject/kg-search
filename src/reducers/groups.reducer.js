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
  groups: []
};

const loadGroupsRequest = state => {
  return {
    ...state,
    hasRequest: false,
    isLoading: true,
    hasError: false
  };
};

const loadGroupsSuccess = (state, action) => ({
  ...state,
  isReady: true,
  hasRequest: false,
  isLoading: false,
  hasError: false,
  groups: (action.groups instanceof Array) ? [...action.groups.map(e => ({ label: e.name, value: e.name }))] : []
});

const loadGroupsFailure = state => ({
  ...state,
  isReady: false,
  hasRequest: false,
  isLoading: false,
  hasError: true,
  groups: []
});

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.LOAD_GROUPS_REQUEST:
    return loadGroupsRequest(state, action);
  case types.LOAD_GROUPS_SUCCESS:
    return loadGroupsSuccess(state, action);
  case types.LOAD_GROUPS_FAILURE:
    return loadGroupsFailure(state, action);
  case types.LOGOUT:
    return loadGroupsSuccess(state, {groups: []});
  default:
    return state;
  }
}