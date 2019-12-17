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
  message: null,
  retry: null, // { label: "", action: null }
  cancel: null // { label: "", action: null }
};

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.LOAD_SEARCH_BAD_REQUEST: {
    return {
      message: "Your search query is not well formed. Please refine your request.",
      cancel: {
        label: "OK",
        action: types.CANCEL_SEARCH
      }
    };
  }
  case types.LOAD_SEARCH_SERVICE_FAILURE: {
    const serviceStatus = action.status?` [code ${action.status}]`:"";
    let message = `The search engine is temporary unavailable${serviceStatus}. Please retry in a moment.`;
    if (action.status === 404 && action.group) {
      message = `The group of group "${action.group}" is temporary not available${serviceStatus}. Please retry in a moment.`;
    }
    return {
      message: message,
      retry: {
        label: "Retry",
        action: types.LOAD_SEARCH_REQUEST
      },
      cancel: {
        label: "Cancel",
        action: types.CANCEL_SEARCH
      }
    };
  }
  case types.LOAD_SEARCH_SESSION_FAILURE: {
    const sessionStatus = action.status?` [code ${action.status}]`:"";
    return {
      message: `Your session has expired${sessionStatus}. Please login again.`,
      retry: {
        label: "Login",
        action: types.AUTHENTICATE
      }
    };
  }
    // search interface
    // return {
    //   message: `${action.path} ${action.id} is currently not available.`,
    //   retry: {
    //     label: "Retry",
    //     action: types.CLEAR_INSTANCE_ERROR
    //   },
    //   cancel: {
    //     label: "Go back to search",
    //     action:  //types.CANCEL_INSTANCE_LOADING
    //   }
    // };
  }
  case types.SET_APPLICATION_READY:
  case types.AGREE_TERMS_SHORT_NOTICE:
  case types.SET_LAYOUT_MODE:
  case types.SET_INFO:
  case types.SHOW_IMAGE:
  case types.INITIALIZE_CONFIG:
  case types.LOAD_DEFINITION:
  case types.LOAD_DEFINITION_REQUEST:
  case types.LOAD_DEFINITION_SUCCESS:
  case types.LOAD_GROUPS:
  case types.LOAD_GROUPS_REQUEST:
  case types.LOAD_GROUPS_SUCCESS:
  case types.SET_INITIAL_SEARCH_PARAMS:
  case types.SET_INITIAL_GROUP:
  case types.SET_GROUP:
  case types.SET_TOKEN:
  case types.SET_SEARCH_READY:
  case types.SET_QUERY_STRING:
  case types.SET_TYPE:
  case types.RESET_TYPE_FOR_GROUP:
  case types.SET_SORT:
  case types.SET_FACET:
  case types.RESET_FACETS:
  case types.SET_FACET_SIZE:
  case types.SET_PAGE:
  case types.LOAD_SEARCH_REQUEST:
  case types.LOAD_SEARCH_SUCCESS:
  case types.CANCEL_SEARCH:
  case types.LOAD_INSTANCE_REQUEST:
  case types.LOAD_INSTANCE_SUCCESS:
  case types.CANCEL_INSTANCE_LOADING:
  case types.SET_INSTANCE:
  case types.SET_PREVIOUS_INSTANCE:
  case types.CLEAR_ALL_INSTANCES:
  case types.AUTHENTICATE:
  case types.AUTHENTICATING:
  case types.LOGOUT:
    return {
      message: null,
      retry: null,
      cancel: null
    };
  default:
    return state;
  }
}