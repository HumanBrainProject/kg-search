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

import { store } from "../store";
import axios from "axios";

const keycloakClientId = "kg";

const endpoints = {
  "authEndpoint": () => "/api/auth/endpoint",
  //"definition": () => "/static/data/labels.json",
  "definition": () => "/api/labels",
  "groups": () => "/api/groups",
  //"search": () => "/static/data/search.json",
  "search": group => `/api/groups/${group}/search`,
  "instance": (group, id) => `/api/groups/${group}/documents/${id}`,
  "preview": id => `/api/${id}/live`,
  "keycloakAuth": (authEndpoint, redirectUri, stateKey, nonceKey) => `${authEndpoint}/realms/hbp/protocol/openid-connect/auth?client_id=${keycloakClientId}&redirect_uri=${encodeURIComponent(redirectUri)}&state=${stateKey}&nonce=${nonceKey}&response_type=token`
};

//&response_mode=fragment
//&scope=openid

class API {
  constructor() {
    this._axios = axios.create({});
    this._axios.interceptors.request.use(config => {
      const state = store.getState();
      const header = config.headers[config.method];
      if (state.auth.accessToken && !config.url.endsWith("/labels")) {
        header.Authorization = "Bearer " + state.auth.accessToken;
      }
      return Promise.resolve(config);
    });
  }

  get axios() {
    return this._axios;
  }

  get endpoints() {
    return endpoints;
  }
}

export default new API();
