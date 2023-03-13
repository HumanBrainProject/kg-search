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
import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";

import { tagsToInvalidateOnLogout, api } from "../../app/services/api";

let keycloak = null;

const initialState = {
  isUnavailble: false,
  settings: null,
  userId: null,
  error: null,
  loginRequired: false,
  authenticationInitialized: false,
  authenticationInitializing: false,
  isAuthenticated: false,
  isAuthenticating: false,
  isLogingOut: false
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    clearAuthError(state) {
      state.error = null;
    },
    setLoginRequired(state, action) {
      state.error = null;
      state.loginRequired = action.payload;
    },
    initializeAuthentication(state) {
      state.error = null;
      state.authenticationInitialized = false;
      state.authenticationInitializing = true;
      state.isAuthenticated = false;
      state.isAuthenticating = false;
      state.isLogingOut = false;
    },
    setAuthReady(state) {
      state.authenticationInitialized = true;
      state.authenticationInitializing = false;
    },
    authInializationFailure(state, action) {
      state.error = action.payload;
      state.authenticationInitialized = false;
      state.authenticationInitializing = false;
    },
    authFailure(state, action) {
      state.error = action.payload;
      state.isAuthenticated = false;
      state.isAuthenticating = false;
      state.userId = null;
    },
    loginSuccess(state, action) {
      state.isAuthenticated = true;
      state.isAuthenticating = false;
      state.userId = action.payload;
    },
    loginRequest(state) {
      state.error = null;
      state.userId = null;
      state.loginRequired = true;
      state.isAuthenticated = false;
      state.isAuthenticating = true;
      state.isLogingOut = false;
    },
    logoutRequest(state) {
      localStorage.removeItem("group");
      state.loginRequired = false;
      state.isLogingOut = true;
    },
    logoutSuccess(state) {
      state.loginRequired = false;
      state.isAuthenticated = false;
      state.authenticationInitialized = false;
      state.authenticationInitializing = true;
      state.isLogingOut = false;
      state.userId = null;
    },
    sessionExpired(state) {
      localStorage.removeItem("group");
      state.error = "The session has expired. Please login again.";
      state.loginRequired = true; // ensure authenticated mode with back navigation
      state.isAuthenticated = false;
      state.isAuthenticating = false;
      state.isLogingOut = false;
      state.userId = null;
    },
    sessionFailure(state, action) {
      localStorage.removeItem("group");
      state.error = action.payload;
      state.loginRequired = true; // ensure authenticated mode with back navigation
      state.isAuthenticated = false;
      state.isAuthenticating = false;
      state.isLogingOut = false;
      state.userId = null;
    }
  },
  extraReducers(builder) {
    builder
      .addMatcher(
        api.endpoints.getSettings.matchFulfilled,
        (state, { payload }) => {
          state.settings = payload?.keycloak;
          state.isUnavailble = !((payload?.keycloak) instanceof Object);
        }
      );
  }
});

export const { clearAuthError, setLoginRequired, initializeAuthentication, setAuthReady, authInializationFailure, authFailure, loginSuccess, logoutSuccess, sessionExpired, sessionFailure } = authSlice.actions;
const {  loginRequest, logoutRequest } = authSlice.actions;

const initializeKeycloak = (settings, loginRequired, dispatch) => {
  try {
    keycloak = window.Keycloak(settings);
    keycloak.onReady = () => { // authenticated => {
      dispatch(setAuthReady());
    };
    keycloak.onAuthSuccess = () => {
      dispatch(loginSuccess(keycloak.subject));
    };
    keycloak.onAuthError = error => {
      const message = (error && error.error_description)?error.error_description:"Failed to authenticate";
      dispatch(authFailure(message));
    };
    keycloak.onTokenExpired = () => {
      keycloak
        .updateToken(30)
        .catch(() => {
          dispatch(sessionExpired());
          dispatch(api.util.invalidateTags(tagsToInvalidateOnLogout));
        });
    };
    keycloak.init({
      onLoad: loginRequired?"login-required":"check-sso",
      pkceMethod: "S256",
      checkLoginIframe: !window.location.host.startsWith("localhost") // avoid CORS error with UI running on localhost with Firefox
    }).catch(() => {
      if (loginRequired) {
        const message = "Failed to initialize authentication";
        dispatch(authInializationFailure(message));
      } else {
        dispatch(setAuthReady());
      }
    });
  } catch (e) { // if keycloak script url return unexpected content
    if (loginRequired) {
      const message = "Failed to initialize authentication";
      dispatch(authInializationFailure(message));
    } else {
      dispatch(setAuthReady());
    }
  }
};

//authenticate = (group=null)
export const setUpAuthentication = createAsyncThunk(
  "setUpAuthentication",
  async (_,  { dispatch, getState}) => {
    const state = getState();
    const { settings, loginRequired } = state.auth;
    if(settings && settings.url) {
    // if(settings && settings.url && (loginRequired || !window.location.host.startsWith("localhost"))) { // to test
      dispatch(initializeAuthentication());
      const keycloakScriptSrc = settings.url + "/js/keycloak.js";
      // dev testing
      // const scripts = document.getElementsByTagName("script");
      // let found = false;
      // for (let i=0; i<scripts.length && !found; i++) {
      //   found = found || scripts[i].src === keycloakScriptSrc;
      // }
      // if (found) {
      //   // eslint-disable-next-line no-debugger
      //   debugger;
      // }
      const keycloakScript = document.createElement("script");
      keycloakScript.src = keycloakScriptSrc;
      keycloakScript.async = true;

      document.head.appendChild(keycloakScript);
      keycloakScript.onload = () => {
        initializeKeycloak(settings, loginRequired, dispatch);
      };
      keycloakScript.onerror = () => {
        document.head.removeChild(keycloakScript);
        if (loginRequired) {
          const message = `Failed to load resource! (${keycloakScript.src})`;
          dispatch(authInializationFailure(message));
        } else {
          dispatch(setAuthReady());
        }
      };
    } else {
      if (loginRequired) {
        const message = "service endpoints configuration is not correctly set";
        dispatch(authInializationFailure(message));
      } else {
        dispatch(setAuthReady());
      }
    }
  }
);


export const getAccessToken = () => {
  return keycloak?.token;
};

export const login = createAsyncThunk(
  "login",
  async (_,  { dispatch}) => {
    keycloak && keycloak.login();
    dispatch(loginRequest());
  }
);

export const logout = createAsyncThunk(
  "logout",
  async (_,  { dispatch}) => {
    dispatch(api.util.invalidateTags(tagsToInvalidateOnLogout));
    dispatch(logoutRequest());
    if (keycloak) {
      await keycloak.logout({redirectUri: `${window.location.protocol}//${window.location.host}`});
    }
    dispatch(logoutSuccess());
  }
);

export default authSlice.reducer;