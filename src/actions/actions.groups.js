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

import * as types from "./actions.types";
import API from "../services/API";

export const loadGroupsRequest = () => {
  return {
    type: types.LOAD_GROUPS_REQUEST
  };
};

export const loadGroupsSuccess = groups => {
  return {
    type: types.LOAD_GROUPS_SUCCESS,
    groups: groups
  };
};

export const loadGroupsFailure = error => {
  return {
    type: types.LOAD_GROUPS_FAILURE,
    error: error
  };
};

export const clearGroupError = () => {
  return {
    type: types.CLEAR_GROUPS_ERROR
  };
};

export const setInitialGroup = group => {
  return {
    type: types.SET_INITIAL_GROUP,
    group: group
  };
};

export const setGroup = group => {
  return {
    type: types.SET_GROUP,
    group: group
  };
};

export const resetTypeForGroup = group => {
  return {
    type: types.RESET_TYPE_FOR_GROUP,
    group: group
  };
};

export const loadGroups = () => {
  return dispatch => {
    dispatch(loadGroupsRequest());
    API.axios
      .get(API.endpoints.groups())
      .then(response => {
        dispatch(loadGroupsSuccess(response.data));
      })
      .catch(e => {
        const error = `The service is temporary unavailable. Please retry in a moment. (${e.message?e.message:e})`;
        dispatch(loadGroupsFailure(error));
      });
  };
};