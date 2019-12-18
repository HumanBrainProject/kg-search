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
import { setGroup } from "./actions.groups";
import { sessionFailure } from "./actions";
import { history } from "../store";

export const loadInstanceRequest = () => {
  return {
    type: types.LOAD_INSTANCE_REQUEST
  };
};

export const loadInstanceSuccess = data => {
  return {
    type: types.LOAD_INSTANCE_SUCCESS,
    data: data
  };
};

export const loadInstanceNoData = error => {
  return {
    type: types.LOAD_INSTANCE_NO_DATA,
    error: error
  };
};

export const loadInstanceFailure = error => {
  return {
    type: types.LOAD_INSTANCE_FAILURE,
    error: error
  };
};

export const clearInstanceError = () => {
  return {
    type: types.CLEAR_INSTANCE_ERROR
  };
};


export const setInstance = data => {
  return {
    type: types.SET_INSTANCE,
    data: data
  };
};

export const cancelInstanceLoading = () => {
  return {
    type: types.CANCEL_INSTANCE_LOADING
  };
};

export const setPreviousInstance = () => {
  return {
    type: types.SET_PREVIOUS_INSTANCE
  };
};

export const clearAllInstances = () => {
  return {
    type: types.CLEAR_ALL_INSTANCES
  };
};

export const goToSearch = (group, defaultGroup) => {
  return dispatch => {
    dispatch(clearInstanceError());
    dispatch(clearAllInstances());
    history.replace(`/${(group && group !== defaultGroup)?("?group=" + group):""}`);
  };
};

export const loadInstance = (type, id) => {
  return dispatch => {
    dispatch(loadInstanceRequest());
    API.axios
      .get(API.endpoints.instance(type, id))
      .then(response => {
        dispatch(loadInstanceSuccess(response.data));
      })
      .catch(e => {
        const { response } = e;
        const { status } = response;
        switch (status) {
        case 400: // Bad Request
        {
          const error = `The service is temporary unavailable. Please retry in a moment. (${e.message?e.message:e})`;
          dispatch(loadInstanceFailure(error));
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
        default:
        {
          const index = response.headers["x-selected-index"];
          if (index) {
            dispatch(setGroup(index.slice(3)));
          }
          const error = `The service is temporary unavailable. Please retry in a moment. (${e.message?e.message:e})`;
          dispatch(loadInstanceFailure(error));
        }
        }
      });
  };
};

export const loadPreview = (type, id) => {
  return dispatch => {
    dispatch(loadInstanceRequest());
    API.axios
      .get(API.endpoints.preview(type, id))
      .then(response => {
        if (response.data && !response.data.error) {
          response.data._id = id;
          dispatch(loadInstanceSuccess(response.data));
        } else if (response.data && response.data.error) {
          dispatch(loadInstanceFailure(response.data.message ? response.data.message : response.data.error));
        } else {
          const error = `The instance with id ${id} is not available.`;
          dispatch(loadInstanceNoData(error));
        }
      })
      .catch(e => {
        if (e.stack === "SyntaxError: Unexpected end of JSON input" || e.message === "Unexpected end of JSON input") {
          dispatch(loadInstanceNoData(e));
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
          default:
          {
            const error = `The service is temporary unavailable. Please retry in a moment. (${e.message?e.message:e})`;
            dispatch(loadInstanceFailure(error));
          }
          }
        }
      });
  };
};