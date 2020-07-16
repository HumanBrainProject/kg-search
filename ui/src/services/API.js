import { store } from "../store";
import axios from "axios";

const oidcUri = "https://services.humanbrainproject.eu/oidc/authorize";
const oidcClientId = "nexus-kg-search";

const endpoints = {
  //"definition": () => "/static/data/labels.json",
  "definition": () => "/search/api/labels",
  "groups": () => "/search/api/groups",
  //"search": () => "/static/data/search.json",
  "search": group => `/search/api/groups/${group}/search`,
  "instance": (group, type, id) => `/search/api/groups/${group}/types/${type}/documents/${id}`,
  "preview": (type, id) => `/search/api/types/${type}/documents/${id}/preview`,
  "auth": (redirectUri, stateKey, nonceKey) => `${oidcUri}?response_type=id_token%20token&client_id=${oidcClientId}&redirect_uri=${escape(redirectUri)}&scope=openid%20profile&state=${stateKey}&nonce=${nonceKey}`
};

class API {
  constructor() {
    this._axios = axios.create({});
    this._axios.interceptors.request.use(config => {
      const state = store.getState();
      const header = config.headers[config.method];
      if (state.auth.accessToken) {
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
