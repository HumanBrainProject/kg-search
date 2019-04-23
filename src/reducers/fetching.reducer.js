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
  active: false
};

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.LOAD_DEFINITION_REQUEST:
    return {
      message: "Initializing search engine...",
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
      message: "Performing search request...",
      active: true
    };
  case types.LOAD_DEFINITION:
  case types.LOAD_DEFINITION_SUCCESS:
  case types.LOAD_DEFINITION_FAILURE:
  case types.LOAD_GROUPS:
  case types.LOAD_GROUPS_SUCCESS:
  case types.LOAD_GROUPS_FAILURE:
  case types.LOAD_SEARCH_SUCCESS:
  case types.LOAD_SEARCH_BAD_REQUEST:
  case types.LOAD_SEARCH_SERVICE_FAILURE:
  case types.LOAD_INSTANCE:
  case types.LOAD_INSTANCE_SUCCESS:
  case types.LOAD_INSTANCE_NO_DATA:
  case types.LOAD_INSTANCE_FAILURE:
    return {
      message: null,
      active: false
    };
  case types.CANCEL_INSTANCE_LOADING:
    if (/[?&]?search=false&?/.test(window.location.search.toLowerCase())) {
      window.location.href = window.location.href.replace(/(\?)?search=false&?/gi, "$1");
    }
    return {
      message: null,
      active: false
    };
  default:
    return state;
  }
}