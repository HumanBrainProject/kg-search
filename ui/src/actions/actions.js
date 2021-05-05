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
import { getHashKey, generateKey, getSearchKey } from "../helpers/BrowserHelpers";
import { history, store } from "../store";
import * as Sentry from "@sentry/browser";

export const setApplicationReady = () => {
  return {
    type: types.SET_APPLICATION_READY
  };
};

export const agreeTermsShortNotice = () => {
  return {
    type: types.AGREE_TERMS_SHORT_NOTICE
  };
};

export const setInfo = text => {
  return {
    type: types.SET_INFO,
    text: text
  };
};

export const setToken = accessToken => {
  return {
    type: types.SET_TOKEN,
    accessToken: accessToken
  };
};

export const logout = () => {
  return {
    type: types.LOGOUT
  };
};

export const showImage = (url, label) => {
  return {
    type: types.SHOW_IMAGE,
    url: url,
    label: label
  };
};

export const sessionFailure = error => {
  return {
    type: types.SESSION_FAILURE,
    error: error
  };
};

export const loadAuthEndpointRequest = () => {
  return {
    type: types.LOAD_AUTH_ENDPOINT_REQUEST
  };
};

export const loadAuthEndpointSuccess = authEndpoint => {
  return {
    type: types.LOAD_AUTH_ENDPOINT_SUCCESS,
    authEndpoint: authEndpoint
  };
};


export const loadAuthEndpointFailure = () => {
  return {
    type: types.LOAD_AUTH_ENDPOINT_FAILURE
  };
};


export const authenticate = (group=null, isKeycloak=true) => {
  return dispatch => {
    let authEndpoint = null;
    if(isKeycloak) {
      const state = store.getState();
      authEndpoint = state.auth.authEndpoint;
    } else {
      authEndpoint = "https://services.humanbrainproject.eu/oidc/authorize";
    }
    if(authEndpoint) {
      const stateKey= btoa(JSON.stringify({
        queryString: window.location.search
      }));
      const nonceKey=  generateKey();
      const redirectUri = `${window.location.protocol}//${window.location.host}${window.location.pathname}${group?("?group=" + group):""}`;
      const auth = isKeycloak?API.endpoints.keycloakAuth:API.endpoints.oidcAuth;
      window.location.href = auth(authEndpoint, redirectUri, stateKey, nonceKey);
    } else {
      dispatch(sessionFailure("Restricted area is currently not available, please retry in a few minutes!"));
    }
  };
};

export const getAuthEndpoint = (group=null) => {
  return dispatch => {
    dispatch(loadAuthEndpointRequest());
    API.axios
      .get(API.endpoints.authEndpoint())
      .then(response => {
        dispatch(loadAuthEndpointSuccess(response.data && response.data.authEndpoint));
        dispatch(authenticate(group, true));
      })
      .catch(e => {
        const { response } = e;
        const { status } = response;
        switch (status) {
        case 500:
        {
          Sentry.captureException(e);
          break;
        }
        case 404:
        default:
        {
          const error = `The service is temporarily unavailable. Please retry in a few minutes. (${e.message?e.message:e})`;
          dispatch(loadAuthEndpointFailure(error));
        }
        }
      });
  };
};

export const initialize = location => {
  return dispatch => {
    const accessToken = getHashKey("access_token");
    const group = getSearchKey("group");
    if (accessToken) {
      dispatch(setToken(accessToken));
      const stateValue = getHashKey("state");
      const state = stateValue?JSON.parse(atob(decodeURIComponent(stateValue))):{};
      const queryString = (state && state.queryString)?state.queryString:"";
      history.replace(`${location.pathname}${queryString}`);
      dispatch(setApplicationReady());
    } else {
      // backward compatibility test
      const instance = location.hash.substr(1);
      if (location.pathname === "/" && instance) {
        const url = `/instances/${instance}${group?("?group=" + group):""}`;
        history.replace(url);
      }
      if((group && (group === "public" || group === "curated")) || location.pathname.startsWith("/live/")) {
        const regLegacyIdReference = /^\/live\/(((.+)\/(.+)\/(.+)\/(.+))\/(.+))\??.*$/;
        if (regLegacyIdReference.test(location.pathname)) {
          dispatch(authenticate(group, false));
        } else {
          dispatch(getAuthEndpoint(group));
        }
      } else {
        dispatch(setApplicationReady());
      }
    }
  };
};