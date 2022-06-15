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

import * as types from "./actions.types";
import API from "../services/API";
import { sessionFailure } from "./actions";

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

export const loadGroups = () => {
  return dispatch => {
    dispatch(loadGroupsRequest());
    API.axios
      .get(API.endpoints.groups())
      .then(response => {
        dispatch(loadGroupsSuccess(response.data));
      })
      .catch(e => {
        switch (e?.response?.status) {
        case 401: // Unauthorized
        case 403: // Forbidden
        case 511: // Network Authentication Required
        {
          const error = "Your session has expired. Please login again.";
          dispatch(sessionFailure(error));
          break;
        }
        default:
        {
          const error = `The service is temporarily unavailable. Please retry in a few minutes. (${e.message?e.message:e})`;
          dispatch(loadGroupsFailure(error));
        }
        }
      });
  };
};