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

import { createStore, compose, applyMiddleware } from "redux";
import thunk from "redux-thunk";

import { createLogger } from "redux-logger";

import createRootReducer from "./reducers";


function configureStoreProd(initialState) {
  const middlewares = [thunk];

  const storeProd = createStore(
    createRootReducer(),
    initialState,
    compose(applyMiddleware(...middlewares))
  );

  return storeProd;
}

function configureStoreDev(initialState) {
  const loggerMiddleware = createLogger();
  const middlewares = [thunk, loggerMiddleware];

  const composeEnhancers =
    window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose; // add support for Redux dev tools
  const storeDev = createStore(
    createRootReducer(),
    initialState,
    composeEnhancers(applyMiddleware(...middlewares))
  );

  if (module.hot) {
    // Enable Webpack hot module replacement for reducers
    module.hot.accept("./reducers", () => {
      const nextReducer = require("./reducers").default; // eslint-disable-line global-require
      storeDev.replaceReducer(nextReducer);
    });
  }

  return storeDev;
}

const configureStore =
  process.env.NODE_ENV === "production"
    ? configureStoreProd
    : configureStoreDev;
export const store = configureStore();