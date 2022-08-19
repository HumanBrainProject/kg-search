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
import { termsCurrentVersion } from "../data/termsShortNotice";

const TermsShortNoticeLocalStorageKey = "ebrains-search-terms-conditions-consent";

const initialState = {
  settingsError: null,
  hasSettings: false,
  isLoading: false,
  isReady: false,
  info: null,
  showTermsShortNotice:
    typeof Storage === "undefined" ||
    !localStorage.getItem(TermsShortNoticeLocalStorageKey),
  showTermsShortUpdateNotice:
    typeof Storage !== "undefined" &&
    localStorage.getItem(TermsShortNoticeLocalStorageKey) &&
    localStorage.getItem(TermsShortNoticeLocalStorageKey) !==
      termsCurrentVersion,
  theme: localStorage.getItem("currentTheme")
};

const setSettings = (state, action) => ({
  ...state,
  hasSettings: true,
  commit: action.commit,
  sentrySettings: action.sentrySettings,
  matomoSettings: action.matomoSettings,
  isLoading: false
});

const loadSettingsRequest = state => ({
  ...state,
  isLoading: true
});

const loadSettingsFailure = (state, action) => ({
  ...state,
  isLoading: false,
  settingsError: action.error
});

const clearSettingsError = state => ({
  ...state,
  settingsError: null
});

const setApplicationReady = state => ({
  ...state,
  isReady: true
});

const setTheme = (state, action) => ({
  ...state,
  theme: action.theme
});

const agreeTermsShortNotice = state => {
  if (typeof Storage !== "undefined") {
    localStorage.setItem(TermsShortNoticeLocalStorageKey, termsCurrentVersion);
  }
  setTimeout(() => window.dispatchEvent(new Event("resize")), 250);
  return {
    ...state,
    showTermsShortNotice: false,
    showTermsShortUpdateNotice: false
  };
};

const setInfo = (state, action) => {
  return {
    ...state,
    info: action.text
  };
};

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.LOAD_SETTINGS_REQUEST:
    return loadSettingsRequest(state);
  case types.LOAD_SETTINGS_FAILURE:
    return loadSettingsFailure(state, action);
  case types.SET_APPLICATION_SETTINGS:
    return setSettings(state, action);
  case types.CLEAR_SETTINGS_ERROR:
    return clearSettingsError(state);
  case types.SET_APPLICATION_READY:
    return setApplicationReady(state);
  case types.SET_THEME:
    return setTheme(state, action);
  case types.AGREE_TERMS_SHORT_NOTICE:
    return agreeTermsShortNotice(state);
  case types.SET_INFO:
    return setInfo(state, action);
  case types.LOAD_SEARCH_REQUEST:
  case types.LOAD_INSTANCE_REQUEST:
  case types.CANCEL_INSTANCE_LOADING:
  case types.SET_PREVIOUS_INSTANCE:
  case types.CLEAR_ALL_INSTANCES:
  default:
    return state;
  }
}
