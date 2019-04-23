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
  accessToken: null,
  authenticate: false,
  isAuthenticated: false
};

const requestAuthentication = state => {
  return {
    ...state,
    accessToken: null,
    authenticate: true,
    isAuthenticated: false
  };
};

const authenticate = (state, action) => {
  return {
    ...state,
    accessToken: action.accessToken,
    authenticate: false,
    isAuthenticated: !!action.accessToken
  };
};

const logout = state => {
  return {
    ...state,
    accessToken: null,
    authenticate: false,
    isAuthenticated: null
  };
};

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.REQUEST_AUTHENTICATION:
    return requestAuthentication(state, action);
  case types.AUTHENTICATE:
    return authenticate(state, action);
  case types.LOAD_SEARCH_SESSION_FAILURE:
    return authenticate(state, {});
  case types.LOGOUT:
    return logout(state, action);
  default:
    return state;
  }
}