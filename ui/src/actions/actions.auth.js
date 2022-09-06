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

export const setAuthSettings = settings => {
  return {
    type: types.SET_AUTH_SETTINGS,
    settings: settings
  };
};

export const setLoginRequired = (required=true) => {
  return {
    type: types.AUTH_MODE,
    required: required
  };
};

export const authInialize = () => {
  return {
    type: types.AUTH_INITIALIZE
  };
};

export const authInializationFailure = error => {
  return {
    type: types.AUTH_INIALIZATION_FAILURE,
    error: error
  };
};

export const clearAuthError = () => {
  return {
    type: types.CLEAR_AUTH_ERROR
  };
};

export const setAuthReady = () => {
  return {
    type: types.SET_AUTH_READY
  };
};

export const loginRequest = () => {
  return {
    type: types.LOGIN
  };
};

export const loginSuccess = () => {
  return {
    type: types.LOGIN_SUCCESS
  };
};

export const logoutRequest = () => {
  return {
    type: types.LOGOUT
  };
};

export const logoutSuccess = () => {
  return {
    type: types.LOGOUT_SUCCESS
  };
};

export const authFailure = error => {
  return {
    type: types.AUTH_FAILURE,
    error: error
  };
};

export const sessionExpired = () => {
  return {
    type: types.SESSION_EXPIRED
  };
};

export const sessionFailure = error => {
  return {
    type: types.SESSION_FAILURE,
    error: error
  };
};

export const login = () => {
  return dispatch => {
    API.login();
    dispatch(loginRequest());
  };
};

export const logout = () => {
  return dispatch => {
    dispatch(logoutRequest());
    setTimeout(async () => {
      await API.logout();
      dispatch(logoutSuccess());
    }, 0);
  };
};

const initializeKeycloak = (settings, loginRequired, dispatch) => {
  try {
    const keycloak = window.Keycloak(settings);
    API.setKeycloak(keycloak);
    keycloak.onReady = () => { // authenticated => {
      dispatch(setAuthReady());
    };
    keycloak.onAuthSuccess = () => {
      dispatch(loginSuccess());
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
        });
    };
    keycloak.init({
      onLoad: loginRequired?"login-required":"check-sso",
      pkceMethod: "S256",
      checkLoginIframe: !window.location.host.startsWith("localhost") || !loginRequired // avoid CORS error with UI running on localhost with Firefox
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
export const setUpAuthentication = (settings, loginRequired) => {
  return async dispatch => {
    if(settings && settings.url) {
      dispatch(authInialize());
      const keycloakScript = document.createElement("script");
      keycloakScript.src = settings.url + "/js/keycloak.js";
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
  };
};
