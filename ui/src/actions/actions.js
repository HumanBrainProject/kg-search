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

import * as types from "./actions.types";
import API from "../services/API";
import { getHashKey, generateKey, getSearchKey } from "../helpers/BrowserHelpers";
import { history } from "../store";

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


export const authenticate = (group=null, isKeycloak=true) => {
  return () => {
    const stateKey= btoa(JSON.stringify({
      queryString: window.location.search
    }));
    const nonceKey=  generateKey();
    const redirectUri = `${window.location.protocol}//${window.location.host}${window.location.pathname}${group?("?group=" + group):""}`;
    const auth = isKeycloak?API.endpoints.keycloakAuth:API.endpoints.oidcAuth;
    window.location.href = auth(redirectUri, stateKey, nonceKey);
  };
};

export const initialize = location => {
  return dispatch => {
    const accessToken = getHashKey("token");
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
          dispatch(authenticate(group, true));
        }
      } else {
        dispatch(setApplicationReady());
      }
    }
  };
};