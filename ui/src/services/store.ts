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
import { configureStore, combineReducers, Middleware, Dispatch, ConfigureStoreOptions } from "@reduxjs/toolkit";
import { logger } from "redux-logger";

import applicationReducer from "../features/application/applicationSlice";
import groupsReducer from "../features/groups/groupsSlice";
import searchReducer from "../features/search/searchSlice";
import instanceReducer from "../features/instance/instanceSlice";
import { api } from "./api";
import authConnector from "./authConnector";
import { CurriedGetDefaultMiddleware } from "@reduxjs/toolkit/dist/getDefaultMiddleware";

const rootReducer = combineReducers({
  application: applicationReducer,
  groups: groupsReducer,
  search: searchReducer,
  instance: instanceReducer,
  [api.reducerPath]: api.reducer
});

export type RootState = ReturnType<typeof rootReducer>;

const sessionFailureMiddleware: Middleware = () => (next: Dispatch) => action => {
  switch (action?.payload?.originalStatus) {
  case 401: // Unauthorized
  case 403: // Forbidden
  case 511: // Network Authentication Required
    authConnector.authAdapter?.unauthorizedRequestResponseHandlerProvider?.unauthorizedRequestResponseHandler && authConnector.authAdapter.unauthorizedRequestResponseHandlerProvider.unauthorizedRequestResponseHandler();
    break;
  }
  return next(action);
};

const prodConfiguration: ConfigureStoreOptions = {
  reducer: rootReducer,
  middleware: (getDefaultMiddleware: CurriedGetDefaultMiddleware) => getDefaultMiddleware()
    .concat(api.middleware)
    .concat(sessionFailureMiddleware)
};

const developmentConfiguration: ConfigureStoreOptions = {
  reducer: rootReducer,
  middleware: (getDefaultMiddleware: CurriedGetDefaultMiddleware) => getDefaultMiddleware({serializableCheck: false})
    .concat(api.middleware)
    .concat(sessionFailureMiddleware)
    .concat(logger)
};

export default configureStore(process.env.NODE_ENV === "production"?prodConfiguration:developmentConfiguration);