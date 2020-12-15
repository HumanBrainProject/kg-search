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
  error: null,
  accessToken: null,
  authenticate: false,
  isAuthenticated: false,
  authEndpoint: null
};

const authenticate = (state, action) => {
  return {
    ...state,
    accessToken: action.accessToken,
    authenticate: false,
    isAuthenticated: !!action.accessToken
  };
};

const setAuthEndpoint = (state, action) => {
  return {
    ...state,
    authEndpoint: action.authEndpoint
  };
};


const setToken = (state, action) => {
  return {
    ...state,
    accessToken: action.accessToken,
    authenticate: false,
    isAuthenticated: true
  };
};

const logout = state => {
  localStorage.removeItem("group");
  return {
    ...state,
    accessToken: null,
    authenticate: false,
    isAuthenticated: null
  };
};

const authenticationExpired = (state, action) => {
  localStorage.removeItem("group");
  return {
    ...state,
    error: action.error,
    accessToken: null,
    authenticate: false,
    isAuthenticated: null
  };
};

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.SET_AUTH_ENDPOINT:
    return setAuthEndpoint(state, action);
  case types.SET_TOKEN:
    return setToken(state, action);
  case types.AUTHENTICATE:
    return authenticate(state, action);
  case types.SESSION_FAILURE:
    return authenticationExpired(state, action);
  case types.LOGOUT:
    return logout(state, action);
  default:
    return state;
  }
}