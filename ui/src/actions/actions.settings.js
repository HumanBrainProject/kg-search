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

export const setCommit = commit => {
  return {
    type: types.SET_COMMIT,
    commit: commit
  };
};

export const loadSettingsRequest = () => {
  return {
    type: types.LOAD_SETTINGS_REQUEST
  };
};

export const loadSettingsSuccess = (typesList, typeMappings) => {
  return {
    type: types.LOAD_SETTINGS_SUCCESS,
    types: typesList,
    typeMappings: typeMappings
  };
};

export const loadSettingsFailure = error => {
  return {
    type: types.LOAD_SETTINGS_FAILURE,
    error: error
  };
};

export const clearSettingsError = () => {
  return {
    type: types.CLEAR_SETTINGS_ERROR
  };
};

export const loadSettings = () => {
  return dispatch => {
    dispatch(loadSettingsRequest());
    API.axios
      .get(API.endpoints.settings())
      .then(({ data }) => {
        API.setMatomo(data.matomo);
        API.setSentry(data.sentry);
        data.commit && dispatch(setCommit(data.commit));
        dispatch(loadSettingsSuccess(data?.types, data?.typeMappings));
      })
      .catch(e => {
        const error = `The service is temporary unavailable. Please retry in a moment. (${e.message?e.message:e})`;
        dispatch(loadSettingsFailure(error));
      });
  };
};