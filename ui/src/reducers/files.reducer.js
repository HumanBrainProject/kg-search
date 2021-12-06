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
  filesError: null,
  isFilesInitialized: false,
  isFilesLoading: false,
  fileFormatsError: null,
  isFileFormatsInitialized: false,
  isFileFormatsLoading: false,
  fileBundlesError: null,
  isFileBundlesInitialized: false,
  isFileBundlesLoading: false,
  files: [],
  fileBundles: [],
  fileFormats: [],
  totalFiles: 0,
  searchFilesAfter: null,
  fileBundle: null,
  fileFormat: null
};

const loadFilesRequest = (state, action) => {
  return {
    ...state,
    isFilesInitialized: true,
    isFilesLoading: true,
    files: action.reset?[]:state.files,
    totalFiles: action.reset?0:state.totalFiles,
    searchFilesAfter: action.reset?null:state.searchFilesAfter,
    filesError: null,
    fileBundle: action.fileBundle,
    fileFormat: action.fileFormat
  };
};

const loadFilesSuccess = (state, action) => {
  return {
    ...state,
    files: action.reset?action.files:[...state.files, ...action.files],
    totalFiles: action.total,
    searchFilesAfter: action.searchAfter,
    isFilesLoading: false,
    filesError: null
  };
};

const loadFilesFailure = (state, action) => {
  return {
    ...state,
    filesError: action.error,
    isFilesLoading: false,
    files: [],
    totalFiles: 0,
    searchFilesAfter: null
  };
};

const loadFileBundlesRequest = state => {
  return {
    ...state,
    isFileBundlesInitialized: true,
    isFileBundlesLoading: true,
    fileBundles: state.fileBundles,
    fileBundlesError: null
  };
};

const loadFileBundlesSuccess = (state, action) => {
  return {
    ...state,
    fileBundles: action.fileBundles,
    isFileBundlesLoading: false,
    fileBundlesError: null
  };
};

const loadFileBundlesFailure = (state, action) => {
  return {
    ...state,
    fileBundlesError: action.error,
    isFileBundlesLoading: false,
    fileBundles: []
  };
};

const loadFileFormatsRequest = state => {
  return {
    ...state,
    isFileFormatsInitialized: true,
    isFileFormatsLoading: true,
    fileFormats: state.fileFormats,
    fileFormatsError: null
  };
};

const loadFileFormatsSuccess = (state, action) => {
  return {
    ...state,
    fileFormats: action.fileFormats,
    isFileFormatsLoading: false,
    fileFormatsError: null
  };
};

const loadFileFormatsFailure = (state, action) => {
  return {
    ...state,
    fileFormatsError: action.error,
    isFileFormatsLoading: false,
    fileFormats: []
  };
};

const clearFiles = state => {
  return {
    ...state,
    filesError: null,
    isFilesInitialized: false,
    isFilesLoading: false,
    fileBundlesError: null,
    isFileBundlesInitialized: false,
    isFileBundlesLoading: false,
    fileFormatsError: null,
    isFileFormatsInitialized: false,
    isFileFormatsLoading: false,
    files: [],
    fileBundles: [],
    fileFormats: [],
    totalFiles: 0,
    searchFilesAfter: null,
    fileBundle: null,
    fileFormat: null
  };
};

export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.LOAD_FILES_REQUEST:
    return loadFilesRequest(state, action);
  case types.LOAD_FILES_SUCCESS:
    return loadFilesSuccess(state, action);
  case types.LOAD_FILES_FAILURE:
    return loadFilesFailure(state, action);
  case types.LOAD_FILE_BUNDLES_REQUEST:
    return loadFileBundlesRequest(state, action);
  case types.LOAD_FILE_BUNDLES_SUCCESS:
    return loadFileBundlesSuccess(state, action);
  case types.LOAD_FILE_BUNDLES_FAILURE:
    return loadFileBundlesFailure(state, action);
  case types.LOAD_FILE_FORMATS_REQUEST:
    return loadFileFormatsRequest(state, action);
  case types.LOAD_FILE_FORMATS_SUCCESS:
    return loadFileFormatsSuccess(state, action);
  case types.LOAD_FILE_FORMATS_FAILURE:
    return loadFileFormatsFailure(state, action);
  case types.CLEAR_LOAD_FILES:
    return clearFiles(state, action);
  default:
    return state;
  }
}