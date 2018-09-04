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
  case types.LOAD_DEFINITION_FAILURE: {
    return {
      message: "The search engine is temporary unavailable. Please retry in a moment.",
      retry: {
        label: "Retry",
        action: types.LOAD_DEFINITION
      },
      cancel: null
    };
  }
  case types.LOAD_INDEXES_FAILURE: {
    return {
      message: "The search engine is temporary unavailable. Please retry in a moment.",
      retry: {
        label: "Retry",
        action: types.LOAD_INDEXES
      },
      cancel: null
    };
  }
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
    if (action.status === 404 && action.index) {
      message = `The index of group "${action.index}" is temporary not available${serviceStatus}. Please retry in a moment.`;
    }
    return {
      message: message,
      retry: {
        label: "Retry",
        action: types.LOAD_SEARCH
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
        action: types.REQUEST_AUTHENTICATION
      }
    };
  }
  case types.LOAD_HIT_NO_DATA: {
    return {
      message: `${(typeof action.reference === "string")?action.reference.replace(/\//g, " "):"This data"} is currently not available.`,
      retry: {
        label: "Retry",
        action: types.LOAD_HIT
      },
      cancel: {
        label: "Cancel",
        action: types.CANCEL_HIT_LOADING
      }
    };
  }
  case types.LOAD_HIT_FAILURE: {
    return {
      message: "The search engine is temporary unavailable. Please retry in a moment.",
      retry: {
        label: "Retry",
        action: types.LOAD_HIT
      },
      cancel: {
        label: "Cancel",
        action: types.CANCEL_HIT_LOADING
      }
    };
  }
  case types.LOAD_DEFINITION:
  case types.LOAD_DEFINITION_REQUEST:
  case types.LOAD_DEFINITION_SUCCESS:
  case types.LOAD_INDEXES:
  case types.LOAD_INDEXES_REQUEST:
  case types.LOAD_INDEXES_SUCCESS:
  case types.LOAD_SEARCH_REQUEST:
  case types.LOAD_SEARCH_SUCCESS:
  case types.CANCEL_SEARCH:
  case types.LOAD_HIT_REQUEST:
  case types.LOAD_HIT_SUCCESS:
  case types.CANCEL_HIT_LOADING:
    return {
      message: null,
      retry: null,
      cancel: null
    };
  default:
    return state;
  }
}