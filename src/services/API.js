import { store } from "../store";
import axios from "axios";

const oidcUri = "https://services.humanbrainproject.eu/oidc/authorize";
const oidcClientId = "nexus-kg-search";

const endpoints = {
  "definition":() => "/proxy/kg_labels/labels/labels",
  "groups": () => "/auth/groups",
  "search": () => "/proxy/search/kg/_search",
  "instance": (type, id) => `/proxy/default/kg/${type}/${id}`,
  "preview": (path, instanceId) => `/query/${path}/search/templates/searchUi/libraries/instancesDynamic/instances/${instanceId}`,
  "auth": (stateKey, nonceKey) => {
    const redirectUri = `${window.location.protocol}//${window.location.host}${window.location.pathname}`;
    return `${oidcUri}?response_type=id_token%20token&client_id=${oidcClientId}&redirect_uri=${escape(redirectUri)}&scope=openid%20profile&state=${stateKey}&nonce=${nonceKey}`;
  }
};

class API{
  constructor() {
    this._axios = axios.create({});
    this._axios.interceptors.request.use(config => {
      const state = store.getState();
      const header = config.headers[config.method];
      if (state.auth.accessToken) {
        header.Authorization = "Bearer " + state.auth.accessToken;
      }
      if (state.auth.accessToken && state.search.group !== null) {
        header["index-hint"] = state.search.group;
      }
      return Promise.resolve(config);
    });
  }

  get axios() {
    return this._axios;
  }

  get endpoints(){
    return endpoints;
  }
}

export default new API();
