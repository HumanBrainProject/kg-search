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
import * as types from "../actions/actions.types";

const initialState = {
  authEndpoint: null,
  isLoading: false,
  error: null,
  authenticatedMode: false,
  authenticationInitialized: false,
  authenticationInitializing: false,
  isAuthenticated: false,
  isAuthenticating: false,
  isloginOut: false
};

const loadAuthEndpointRequest = (state, action) => ({
  ...state,
  isLoading: true,
  authEndpoint: action.authEndpoint
});

const loadAuthEndpointFailure = (state, action) => ({
  ...state,
  isLoading: false,
  error: action.error
});

const clearAuthError = state => ({
  ...state,
  error: null
});

const setAuthEndpoint = (state, action) => ({
  ...state,
  isLoading: false,
  authEndpoint: action.authEndpoint
});

const setAuthMode = (state, action) => ({
  ...state,
  error: null,
  authenticatedMode: action.active
});

const initializeAuthentication = state => ({
  ...state,
  error: null,
  authenticationInitialized: false,
  authenticatedMode: true,
  authenticationInitializing: true,
  isAuthenticated: false,
  isAuthenticating: false,
  isloginOut: false
});

const setAuthReady = state => ({
  ...state,
  authenticationInitialized: true,
  authenticationInitializing: false
});

const authInializationFailure = (state, action) => ({
  ...state,
  error: action.error,
  authenticationInitialized: false,
  authenticationInitializing: false
});

const authFailure = (state, action) => ({
  ...state,
  error: action.error,
  isAuthenticated: false,
  isAuthenticating: false
});

const loginSuccess = state => ({
  ...state,
  isAuthenticated: true,
  isAuthenticating: false
});

const login = state => ({
  ...state,
  error: false,
  authenticatedMode: true,
  isAuthenticated: false,
  isAuthenticating: true,
  isloginOut: false
});

const logout = state => {
  localStorage.removeItem("group");
  return {
    ...state,
    isloginOut: true
  };
};

const logoutSuccess = state => ({
  ...state,
  authenticatedMode: false,
  isAuthenticated: false,
  isloginOut: false
});

const sessionExpired = state => {
  localStorage.removeItem("group");
  return {
    ...state,
    error: "The session has expired",
    authenticatedMode: true, // ensure authenticated mode with back navigation
    isAuthenticated: false,
    isAuthenticating: false,
    isloginOut: false
  };
};

const sessionFailure = (state, action) => {
  localStorage.removeItem("group");
  return {
    ...state,
    error: action.error,
    authenticatedMode: true, // ensure authenticated mode with back navigation
    isAuthenticated: false,
    isAuthenticating: false,
    isloginOut: false
  };
};


export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.LOAD_AUTH_ENDPOINT_REQUEST:
    return loadAuthEndpointRequest(state, action);
  case types.LOAD_AUTH_ENDPOINT_FAILURE:
    return loadAuthEndpointFailure(state, action);
  case types.CLEAR_AUTH_ENDPOINT_ERROR:
    return clearAuthError(state, action);
  case types.SET_AUTH_ENDPOINT:
    return setAuthEndpoint(state, action);
  case types.AUTH_MODE:
    return setAuthMode(state, action);
  case types.AUTH_INITIALIZE:
    return initializeAuthentication(state, action);
  case types.SET_AUTH_READY:
    return setAuthReady(state, action);
  case types.AUTH_INIALIZATION_FAILURE:
    return authInializationFailure(state, action);
  case types.LOGIN:
    return login(state, action);
  case types.AUTH_FAILURE:
    return authFailure(state, action);
  case types.LOGIN_SUCCESS:
    return loginSuccess(state);
  case types.LOGOUT:
    return logout(state);
  case types.LOGOUT_SUCCESS:
    return logoutSuccess(state);
  case types.SESSION_EXPIRED:
    return sessionExpired(state, action);
  case types.SESSION_FAILURE:
    return sessionFailure(state, action);
  default:
    return state;
  }
}
