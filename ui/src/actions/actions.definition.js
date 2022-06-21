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


export const setAuthEndpoint = authEndpoint => {
  return {
    type: types.SET_AUTH_ENDPOINT,
    authEndpoint: authEndpoint
  };
};

export const setCommit = commit => {
  return {
    type: types.SET_COMMIT,
    commit: commit
  };
};

export const loadDefinitionRequest = () => {
  return {
    type: types.LOAD_DEFINITION_REQUEST
  };
};

export const loadDefinitionSuccess = (typesList, typeMappings) => {
  return {
    type: types.LOAD_DEFINITION_SUCCESS,
    types: typesList,
    typeMappings: typeMappings
  };
};

export const loadDefinitionFailure = error => {
  return {
    type: types.LOAD_DEFINITION_FAILURE,
    error: error
  };
};

export const clearDefinitionError = () => {
  return {
    type: types.CLEAR_DEFINITION_ERROR
  };
};

export const loadDefinition = () => {
  return dispatch => {
    dispatch(loadDefinitionRequest());
    API.axios
      .get(API.endpoints.definition())
      .then(({ data }) => {
        data.authEndpoint && dispatch(setAuthEndpoint(data.authEndpoint));
        data.commit && dispatch(setCommit(data.commit));
        dispatch(loadDefinitionSuccess(data?.types, data?.typeMappings));
      })
      .catch(e => {
        const { response } = e;
        const status  = response?.status;
        switch (status) {
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
          const error = `The service is temporary unavailable. Please retry in a moment. (${e.message?e.message:e})`;
          dispatch(loadDefinitionFailure(error));
        }
        }
      });
  };
};