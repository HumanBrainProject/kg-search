import { store } from "../store";
import axios from "axios";

const keycloakClientId = "kg";
const oidcClientId = "nexus-kg-search";

const endpoints = {
  //"definition": () => "/static/data/labels.json",
  "authEndpoint": () => "/api/auth/endpoint",
  "definition": () => "/api/labels",
  "groups": () => "/api/groups",
  //"search": () => "/static/data/search.json",
  "search": group => `/api/groups/${group}/search`,
  "instance": (group, type, id) => `/api/groups/${group}/types/${type}/documents/${id}`,
  "preview": (type, id) => `/api/${type}/${id}/live`,
  "keycloakAuth": (authEndpoint, redirectUri, stateKey, nonceKey) => `${authEndpoint}/realms/hbp/protocol/openid-connect/auth?client_id=${keycloakClientId}&redirect_uri=${encodeURIComponent(redirectUri)}&state=${stateKey}&nonce=${nonceKey}&response_type=token`,
  "oidcAuth": (authEndpoint, redirectUri, stateKey, nonceKey) => `${authEndpoint}?response_type=id_token%20token&client_id=${oidcClientId}&redirect_uri=${escape(redirectUri)}&scope=openid%20profile&state=${stateKey}&nonce=${nonceKey}`
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
        if(config.url.endsWith("/live")) {
          header["X-Legacy-Authorization"] = state.auth.accessToken;
        } else {
          header.Authorization = "Bearer " + state.auth.accessToken;
        }
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
