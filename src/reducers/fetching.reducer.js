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

import * as types from "../actions/actions.types";

const initialState = {
  message: null,
  active: false
};

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.LOAD_DEFINITION_REQUEST:
    return {
      message: "Initializing...",
      active: true
    };
  case types.LOAD_GROUPS_REQUEST:
    return {
      message: "Retrieving your profile...",
      active: true
    };
  case types.LOAD_SEARCH_REQUEST:
    return {
      message: "Performing search request...",
      active: true
    };
  case types.LOAD_INSTANCE_REQUEST:
    return {
      message: "Loading instance...",
      active: true
    };
  case types.SET_APPLICATION_READY:
  case types.AGREE_TERMS_SHORT_NOTICE:
  case types.SET_LAYOUT_MODE:
  case types.SET_INFO:
  case types.SHOW_IMAGE:
  case types.LOAD_DEFINITION_SUCCESS:
  case types.LOAD_DEFINITION_FAILURE:
  case types.CLEAR_DEFINITION_ERROR:
  case types.LOAD_GROUPS_SUCCESS:
  case types.LOAD_GROUPS_FAILURE:
  case types.CLEAR_GROUPS_ERROR:
  case types.SET_INITIAL_SEARCH_PARAMS:
  case types.SET_INITIAL_GROUP:
  case types.SET_GROUP:
  case types.SET_TOKEN:
  case types.SET_QUERY_STRING:
  case types.SET_TYPE:
  case types.RESET_TYPE_FOR_GROUP:
  case types.SET_SORT:
  case types.SET_FACET:
  case types.RESET_FACETS:
  case types.SET_FACET_SIZE:
  case types.SET_PAGE:
  case types.LOAD_SEARCH_SUCCESS:
  case types.LOAD_SEARCH_BAD_REQUEST:
  case types.LOAD_SEARCH_SERVICE_FAILURE:
  case types.SESSION_FAILURE:
  case types.CANCEL_SEARCH:
  case types.LOAD_INSTANCE_SUCCESS:
  case types.LOAD_INSTANCE_NO_DATA:
  case types.LOAD_INSTANCE_FAILURE:
  case types.CLEAR_INSTANCE_ERROR:
  case types.CANCEL_INSTANCE_LOADING:
  case types.SET_INSTANCE:
  case types.SET_PREVIOUS_INSTANCE:
  case types.CLEAR_ALL_INSTANCES:
  case types.AUTHENTICATE:
  case types.AUTHENTICATING:
  case types.LOGOUT:
    return {
      message: null,
      active: false
    };
  default:
    return state;
  }
}