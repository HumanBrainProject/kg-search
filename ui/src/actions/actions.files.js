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

export const loadFilesRequest = reset => {
  return {
    type: types.LOAD_FILES_REQUEST,
    reset: reset
  };
};

export const loadFilesSuccess = (result, reset) => {
  return {
    type: types.LOAD_FILES_SUCCESS,
    files: result.data,
    total: result.total,
    searchAfter: result.searchAfter,
    reset: reset
  };
};


export const loadFilesFailure = error => {
  return {
    type: types.LOAD_FILES_FAILURE,
    error: error
  };
};

export const clearFiles = () => {
  return {
    type: types.CLEAR_LOAD_FILES
  };
};

export const loadFiles = (url, reset) => {
  return dispatch => {
    dispatch(loadFilesRequest(reset));
    API.axios
      .get(url)
      .then(response => dispatch(loadFilesSuccess(response.data, reset)))
      .catch(e => {
        const { response } = e;
        const { status } = response;
        switch (status) {
        case 401: // Unauthorized
        case 403: // Forbidden
        case 511: // Network Authentication Required
        {
          const error = "Your session has expired. Please login again.";
          dispatch(sessionFailure(error));
          break;
        }
        case 500:
        case 404:
        default:
        {
          const error = `The service is temporarily unavailable. Please retry in a few minutes. (${e.message?e.message:e})`;
          dispatch(loadFilesFailure(error));
        }
        }
      });
  };
};