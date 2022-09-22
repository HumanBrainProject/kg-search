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
import { configureStore } from "@reduxjs/toolkit";
import { logger } from "redux-logger";

import { api } from "./services/api";
import applicationReducer from "../features/application/applicationSlice";
import authReducer, { sessionFailure } from "../features/auth/authSlice";
import groupsReducer from "../features/groups/groupsSlice";
import searchReducer, { searchCacheActions } from "../features/search/searchSlice";
import instanceReducer, { instancesCacheActions } from "../features/instance/instanceSlice";

const rootReducer = {
  application: applicationReducer,
  auth: authReducer,
  groups: groupsReducer,
  search: searchReducer,
  instance: instanceReducer,
  [api.reducerPath]: api.reducer
};

const cacheActions = [
  searchCacheActions,
  instancesCacheActions
];

const dispatchQueryResultFromCacheMiddleware = ({ dispatch, getState }) => next => action => {
  if (action?.error?.name === "ConditionError") {
    const endpointName = action?.meta?.arg?.endpointName;
    const queryCacheKey = action?.meta?.arg?.queryCacheKey;
    if (endpointName && queryCacheKey) {
      const state = getState();
      const payload = state?.api?.queries?.[queryCacheKey]?.data;
      cacheActions.forEach(item => {
        const cacheAction = item[endpointName];
        if (cacheAction) {
          dispatch(cacheAction(payload));
        }
      });
    }
  }
  return next(action);
};

const sessionFailureMiddleware = ({ dispatch }) => next => action => {
  switch (action?.payload?.originalStatus) {
  case 401: // Unauthorized
  case 403: // Forbidden
  case 511: // Network Authentication Required
    dispatch(sessionFailure());
    break;
  }
  return next(action);
};

const prodConfiguration = {
  reducer: rootReducer,
  middleware: getDefaultMiddleware => getDefaultMiddleware()
    .concat(api.middleware)
    .concat(sessionFailureMiddleware)
    .concat(dispatchQueryResultFromCacheMiddleware)
};

const developmentConfiguration = {
  reducer: rootReducer,
  middleware: (getDefaultMiddleware) => getDefaultMiddleware()
    .concat(api.middleware)
    .concat(sessionFailureMiddleware)
    .concat(dispatchQueryResultFromCacheMiddleware)
    .concat(logger)
};

export default configureStore(process.env.NODE_ENV === "production"?prodConfiguration:developmentConfiguration);
