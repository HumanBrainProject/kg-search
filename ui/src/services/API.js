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

import axios from "axios";
import ReactPiwik from "react-piwik";
import * as Sentry from "@sentry/browser";

const endpoints = {
  "authSettings": () => "/api/auth/settings",
  //"settings": () => "/static/data/settings.json",
  "settings": () => "/api/settings",
  "groups": () => "/api/groups",
  //"search": () => "/static/data/search.json",
  "search": (group, q, type, from, size) => `/api/groups/${group}/search?${q?("q=" + encodeURIComponent(q) + "&"):""}type=${encodeURIComponent(type)}&from=${from}&size=${size}`,
  //"instance": () => "/static/data/instance.json",
  "instance": (group, id) => `/api/groups/${group}/documents/${id}`,
  "preview": id => `/api/${id}/live?skipReferenceCheck=true`,
  "citation": (doi, citationStyle, contentType) => `/api/citation?doi=${encodeURIComponent(doi)}&style=${citationStyle}&contentType=${contentType}`
};

class API {
  constructor() {
    this._axios = axios.create({});
    this._keycloak = null;
    this._axios.interceptors.request.use(config => {
      const header = config.headers[config.method];
      if (this._keycloak && this._keycloak.token && config.url && !config.url.endsWith("/settings") && !config.url.startsWith("/api/citation")) {
        header.Authorization = "Bearer " + this._keycloak.token;
      }
      return Promise.resolve(config);
    });
  }

  get axios() {
    return this._axios;
  }

  get accessToken() {
    return this._keycloak?.token;
  }

  setKeycloak(keycloak) {
    this._keycloak = keycloak;
  }

  setSentry(commit, sentry) {
    if (commit && sentry) {
      Sentry.init({
        ...sentry,
        release: commit,
        environment: window.location.host
      });
    }
  }

  setMatomo(settings) {
    if (settings?.url && settings?.siteId) {
      this._matomo = new ReactPiwik({
        url: settings.url,
        siteId:settings.siteId,
        trackErrors: true
      });
    }
  }

  trackCustomUrl(url) {
    if (this._matomo && url) {
      ReactPiwik.push(["setCustomUrl", url]);
    }
  }

  trackPageView() {
    if (this._matomo) {
      ReactPiwik.push(["trackPageView"]);
    }
  }

  trackEvent(category, name, value) {
    if (this._matomo) {
      ReactPiwik.push(["trackEvent", category, name, value]);
    }
  }

  trackLink(category, name) {
    if (this._matomo) {
      ReactPiwik.push(["trackLink", category, name]);
    }
  }

  login() {
    this._keycloak && this._keycloak.login();
  }

  async logout() {
    if (this._keycloak) {
      await this._keycloak.logout({redirectUri: `${window.location.protocol}//${window.location.host}/logout`});
    }
  }

  get endpoints() {
    return endpoints;
  }
}

export default new API();
