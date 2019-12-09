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

const hitsPerPage = 20;
const timeout = 5000;
const queryTweaking = {
  wildcard: {
    maxNbOfTerms: -1, // -1 = apply on all terms, 0 = do not apply, positive number n = apply on first n terms
    minNbOfChars: 3 // nb of character under which wildcard is not applied
  },
  fuzzySearch: {
    maxNbOfTerms: -1, // -1 = apply on all terms, 0 = do not apply, positive number n = apply on first n terms
    minNbOfChars: 4 // nb of character under which fuzzy search is not applied
  },
  maxNbOfTermsTrigger: Number.MAX_VALUE // maximum number of terms before tweaking is turned off
};
const oidcUri = "https://services.humanbrainproject.eu/oidc/authorize";
const oidcClientId = "nexus-kg-search";

const initialState = {
  isReady: false,
  searchApiHost: "",
  timeout: timeout,
  hitsPerPage: hitsPerPage,
  queryTweaking: queryTweaking,
  oidcUri: oidcUri,
  oidcClientId: oidcClientId
};

const initializeConfig = (state, action) => {
  return {
    ...state,
    ...action.options,
    isReady: true};
};

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.INITIALIZE_CONFIG:
    return initializeConfig(state, action);
  default:
    return state;
  }
}