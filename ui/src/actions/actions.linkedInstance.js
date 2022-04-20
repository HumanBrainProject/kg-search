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

export const loadLinkedInstanceRequest = () => {
  return {
    type: types.LOAD_LINKED_INSTANCE_REQUEST
  };
};

export const loadLinkedInstanceSuccess = data => {
  return {
    type: types.LOAD_LINKED_INSTANCE_SUCCESS,
    data: data
  };
};

export const loadLinkedInstanceFailure = error => {
  return {
    type: types.LOAD_LINKED_INSTANCE_FAILURE,
    error: error
  };
};

export const loadLinkedInstance = (group, id,) => {
  return dispatch => {
    dispatch(loadLinkedInstanceRequest());
    API.axios
      .get(API.endpoints.instance(group, id))
      .then(response => {
        if (response.data && response.data._source && !response.data.error) {
          response.data._id = id;
          dispatch(loadLinkedInstanceSuccess(response.data._source));
        } else if (response.data && response.data.error) {
          dispatch(loadLinkedInstanceFailure(response.data.message ? response.data.message : response.data.error));
        } else {
          const error = `The instance with id ${id} is not available.`;
          dispatch(loadLinkedInstanceFailure(error));
        }
      })
      .catch(e => {
        const { response } = e;
        const { status } = response;
        switch (status) {
        case 400: // Bad Request
        {
          const error = `The service is temporarily unavailable. Please retry in a few minutes. (${e.message?e.message:e})`;
          dispatch(loadLinkedInstanceFailure(error));
          break;
        }
        case 401: // Unauthorized
        case 403: // Forbidden
        case 511: // Network Authentication Required
        {
          const error = "Your session has expired. Please login again.";
          dispatch(sessionFailure(error));
          break;
        }
        case 404:
        {
          const error = `The instance with id ${id} is not available.`;
          dispatch(loadLinkedInstanceFailure(error));
          break;
        }
        default:
        {
          const error = `The service is temporarily unavailable. Please retry in a few minutes. (${e.message?e.message:e})`;
          dispatch(loadLinkedInstanceFailure(error));
        }
        }
      });
  };
};

export const loadLinkedInstancePreview = id => {
  return dispatch => {
    dispatch(loadLinkedInstanceRequest());
    API.axios
      .get(API.endpoints.preview(id))
      .then(response => {
        if (response.data && response.data._source && !response.data.error) {
          response.data._id = id;
          dispatch(loadLinkedInstanceSuccess(response.data._source));
        } else if (response.data && response.data.error) {
          dispatch(loadLinkedInstanceFailure(response.data.message ? response.data.message : response.data.error));
        } else {
          const error = `The instance with id ${id} is not available.`;
          dispatch(loadLinkedInstanceFailure(error));
        }
      })
      .catch(e => {
        if (e.stack === "SyntaxError: Unexpected end of JSON input" || e.message === "Unexpected end of JSON input") {
          dispatch(loadLinkedInstanceFailure(e));
        } else {
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
          case 404:
          {
            const error = `The instance with id ${id} is not available.`;
            dispatch(loadLinkedInstanceFailure(error));
            break;
          }
          default:
          {
            const error = `The service is temporarily unavailable. Please retry in a few minutes. (${e.message?e.message:e})`;
            dispatch(loadLinkedInstanceFailure(error));
          }
          }
        }
      });
  };
};