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
import { init as SentryInit, captureException as SentryCaptureException, showReportDialog as SentryShowReportDialog } from "@sentry/browser";

const endpoints = {
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
    this._is_sentry_initilized = false;
    this._matomo = null;
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

  setSentry(settings) {
    if (settings && !this._is_sentry_initilized) {
      this._is_sentry_initilized = true;
      SentryInit({
        ...settings,
        autoSessionTracking: false
      });
    }
  }

  captureException(e) {
    if (this._is_sentry_initilized) {
      SentryCaptureException(e);
    }
  }

  showReportDialog(customSettings) {
    if (this._is_sentry_initilized) {
      const defaultSettings = {
        title: "An unexpected error has occured.",
        subtitle2: "We recommend you to save all your changes and reload the application in your browser. The KG team has been notified. If you'd like to help, tell us what happened below.",
        labelEmail: "Email",
        labelName: "Name",
        labelComments: "Please fill in a description of your error use case"
      };
      const settings = {
        ...defaultSettings,
        ...customSettings
      };
      SentryCaptureException(new Error(settings.title)); //We generate a custom error as report dialog is only linked to an error.
      SentryShowReportDialog(settings);
    }
  }

  setMatomo(settings) {
    if (settings?.url && settings?.siteId && !this._matomo) {
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
