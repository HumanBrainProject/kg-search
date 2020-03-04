import { store } from "../store";
import axios from "axios";

const oidcUri = "https://services.humanbrainproject.eu/oidc/authorize";
const oidcClientId = "nexus-kg-search";

const endpoints = {
  "definition":() => "/proxy/kg_labels/labels/labels",
  "groups": () => "/auth/groups",
  "search": () => "/proxy/search/kg/_search",
  "instance": (type, id) => `/proxy/default/kg/${type}/${id}`,
  "preview": (type, id) => `/query/${type}/search/templates/searchUi/libraries/instancesDynamic/instances/${id}`,
  "auth": (redirectUri, stateKey, nonceKey) => `${oidcUri}?response_type=id_token%20token&client_id=${oidcClientId}&redirect_uri=${escape(redirectUri)}&scope=openid%20profile&state=${stateKey}&nonce=${nonceKey}`
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
      if (state.auth.accessToken && state.groups.group !== null) {
        header["index-hint"] = state.groups.group;
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
