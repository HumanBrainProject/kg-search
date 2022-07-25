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

export const setAuthEndpoint = authEndpoint => {
  return {
    type: types.SET_AUTH_ENDPOINT,
    authEndpoint: authEndpoint
  };
};

export const setAuthMode = (active=true) => {
  return {
    type: types.AUTH_MODE,
    active: active
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

const initializeKeycloakAndLogin = (endpoint, dispatch) => {
  const keycloak = window.Keycloak({
    "realm": "hbp",
    "url":  endpoint,
    "clientId": "kg"
  });
  API.setKeycloak(keycloak);
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
  keycloak.init({ onLoad: "login-required", pkceMethod: "S256" });
  dispatch(setAuthReady());
};

//authenticate = (group=null)
export const setUpAuthenticationAndLogin = endpoint => {
  return async dispatch => {
    if(endpoint) {
      dispatch(authInialize());
      try {
        const keycloakScript = document.createElement("script");
        keycloakScript.src = endpoint + "/js/keycloak.js";
        keycloakScript.async = true;

        document.head.appendChild(keycloakScript);
        keycloakScript.onload = () => {
          initializeKeycloakAndLogin(endpoint, dispatch);
        };
        keycloakScript.onerror = () => {
          document.head.removeChild(keycloakScript);
          const message = `Failed to load resource! (${keycloakScript.src})`;
          dispatch(authInializationFailure(message));
        };
      } catch (e) {
        const message = `Failed to load service endpoints configuration (${e && e.message?e.message:e})`;
        dispatch(authInializationFailure(message));
      }
    } else {
      const message = "service endpoints configuration is not correctly set";
      dispatch(authInializationFailure(message));
    }
  };
};
export const loadAuthEndpointRequest = () => {
  return {
    type: types.LOAD_AUTH_ENDPOINT_REQUEST
  };
};

export const loadAuthEndpointFailure = error => {
  return {
    type: types.LOAD_AUTH_ENDPOINT_FAILURE,
    error: error
  };
};

export const clearAuthEndpointError = () => {
  return {
    type: types.CLEAR_AUTH_ENDPOINT_ERROR
  };
};

export const loadAuthEndpoint = () => {
  return dispatch => {
    dispatch(loadAuthEndpointRequest());
    API.axios
      .get(API.endpoints.authEndpoint())
      .then(({ data }) => {
        data.authEndpoint && dispatch(setAuthEndpoint(data.authEndpoint));
      })
      .catch(e => {
        const error = `The service is temporary unavailable. Please retry in a moment. (${e.message?e.message:e})`;
        dispatch(loadAuthEndpointFailure(error));
      });
  };
};