/* eslint-disable no-unused-vars */
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
import { clearGroupError } from "./actions.groups";
import { sessionFailure, logout } from "./actions";
import { history, store } from "../store";
import { getSearchKey } from "../helpers/BrowserHelpers";

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

export const goBackToInstance = id => {
  return {
    type: types.GO_BACK_TO_INSTANCE,
    id: id
  };
};

export const updateLocation = () => {
  const state = store.getState();
  const id = state.instances.currentInstance?._id;
  if(id) {
    history.push(`/${window.location.search}#${id}`);
  } else {
    history.push(`/${window.location.search}`);
  }
  return {
    type: types.UPDATE_LOCATION
  };
};

export const goToSearch = (group, defaultGroup) => {
  return dispatch => {
    if (!group) {
      dispatch(clearGroupError());
      dispatch(logout());
    }
    dispatch(clearInstanceError());
    dispatch(clearAllInstances());
    history.replace(`/${(group && group !== defaultGroup)?("?group=" + group):""}`);
  };
};

export const loadInstance = (group, id, shouldUpdateLocation=false) => {
  return dispatch => {
    dispatch(loadInstanceRequest());
    API.axios
      .get(API.endpoints.instance(group, id))
      .then(response => {
        dispatch(loadInstanceSuccess(response.data));
        if(shouldUpdateLocation) {
          dispatch(updateLocation());
        }
      })
      .catch(e => {
        const { response } = e;
        const { status } = response;
        switch (status) {
        case 400: // Bad Request
        {
          const error = `The service is temporarily unavailable. Please retry in a few minutes. (${e.message?e.message:e})`;
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
        {
          const url = `${window.location.protocol}//${window.location.host}${window.location.pathname}?group=curated`;
          const link = `<a href=${url}>${url}</a>`;
          const group = getSearchKey("group");
          const error = (group && group === "curated")? "The page you requested was not found." :
            `The page you requested was not found. It might not yet be public and authorized users might have access to it in the ${link} or in in-progress view`;
          dispatch(loadInstanceFailure(error));
          break;
        }
        case 500:
        default:
        {
          const error = `The service is temporarily unavailable. Please retry in a few minutes. (${e.message?e.message:e})`;
          dispatch(loadInstanceFailure(error));
        }
        }
      });
  };
};

export const loadPreview = id => {
  return dispatch => {
    dispatch(loadInstanceRequest());
    API.axios
      .get(API.endpoints.preview(id))
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
          case 500:
          case 404:
          default:
          {
            const error = `The service is temporarily unavailable. Please retry in a few minutes. (${e.message?e.message:e})`;
            dispatch(loadInstanceFailure(error));
          }
          }
        }
      });
  };
};