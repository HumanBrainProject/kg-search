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

export const setApplicationReady = isReady => {
  return {
    type: types.SET_APPLICATION_READY,
    isReady: isReady
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

export const setLayoutMode = gridLayoutMode => {
  return {
    type: types.SET_LAYOUT_MODE,
    gridLayoutMode: gridLayoutMode
  };
};

export const initializeConfig = options => {
  return {
    type: types.INITIALIZE_CONFIG,
    options: options
  };
};

export const loadDefinition = () => {
  return {
    type: types.LOAD_DEFINITION
  };
};

export const loadDefinitionRequest = () => {
  return {
    type: types.LOAD_DEFINITION_REQUEST
  };
};

export const loadDefinitionSuccess = definition => {
  return {
    type: types.LOAD_DEFINITION_SUCCESS,
    definition: definition
  };
};

export const loadDefinitionFailure = error => {
  return {
    type: types.LOAD_DEFINITION_FAILURE,
    error: error
  };
};

export const loadIndexes = () => {
  return {
    type: types.LOAD_INDEXES
  };
};

export const loadIndexesRequest = () => {
  return {
    type: types.LOAD_INDEXES_REQUEST
  };
};

export const loadIndexesSuccess = indexes => {
  return {
    type: types.LOAD_INDEXES_SUCCESS,
    indexes: indexes
  };
};

export const loadIndexesFailure = error => {
  return {
    type: types.LOAD_INDEXES_FAILURE,
    error: error
  };
};

export const setSearchReady = isReady => {
  return {
    type: types.SET_SEARCH_READY,
    isReady: isReady
  };
};

export const loadSearchBadRequest = status => {
  return {
    type: types.LOAD_SEARCH_BAD_REQUEST,
    status: status
  };
};

export const loadSearchServiceFailure = (status, index) => {
  return {
    type: types.LOAD_SEARCH_SERVICE_FAILURE,
    status: status,
    index: index
  };
};

export const loadSearchSessionFailure = status => {
  return {
    type: types.LOAD_SEARCH_SESSION_FAILURE,
    status: status
  };
};

export const loadSearchRequest = nonce => {
  return {
    type: types.LOAD_SEARCH_REQUEST,
    nonce: nonce
  };
};

export const loadSearchResult = (results, index, from) => {
  return {
    type: types.LOAD_SEARCH_SUCCESS,
    results: results,
    index: index,
    from: from
  };
};

export const cancelSearch = () => {
  return {
    type: types.CANCEL_SEARCH
  };
};

export const setIndex = index => {
  return {
    type: types.SET_INDEX,
    index: index
  };
};

export const loadHit = reference => {
  return {
    type: types.LOAD_HIT,
    reference: reference
  };
};

export const loadHitRequest = () => {
  return {
    type: types.LOAD_HIT_REQUEST
  };
};

export const loadHitSuccess = data => {
  return {
    type: types.LOAD_HIT_SUCCESS,
    data: data
  };
};

export const loadHitNoData = reference => {
  return {
    type: types.LOAD_HIT_NO_DATA,
    reference: reference
  };
};

export const loadHitFailure = (id, error) => {
  return {
    type: types.LOAD_HIT_FAILURE,
    id: id,
    error: error
  };
};

export const setHit = data => {
  return {
    type: types.SET_HIT,
    data: data
  };
};

export const cancelHitLoading = () => {
  return {
    type: types.CANCEL_HIT_LOADING
  };
};

export const setPreviousHit = () => {
  return {
    type: types.SET_PREVIOUS_HIT
  };
};

export const clearAllHits = () => {
  return {
    type: types.CLEAR_ALL_HITS
  };
};

export const setCurrentHitFromBrowserLocation = () => {
  return {
    type: types.SET_CURRENT_HIT_FROM_BROWSER_LOCATION
  };
};

export const authenticate = accessToken => {
  return {
    type: types.AUTHENTICATE,
    accessToken: accessToken
  };
};

export const requestAuthentication = () => {
  return {
    type: types.REQUEST_AUTHENTICATION
  };
};

export const logout = () => {
  return {
    type: types.LOGOUT
  };
};

export const updateEmailToLink = () => {
  return {
    type: types.UPDATE_EMAIL_TO_LINK
  };
};