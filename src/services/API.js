import { store } from "../store";
import axios from "axios";

const endpoints = {
  "definition": (host) => `${host}/proxy/kg_labels/labels/labels`,
  "groups": (host) => `${host}/auth/groups`,
  "search": (host) => `${host}/proxy/search/kg`,
  "instance": (host, id) => `${host}/proxy/default/kg/${id}`,
  "preview": (host, path, instanceId) => `${host}/query/${path}/search/templates/searchUi/libraries/instancesDynamic/instances/${instanceId}`
};

const default_group = "public";

class API{
  defaultGroup = default_group;
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
