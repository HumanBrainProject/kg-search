/*
*   Copyright (c) 2018, EPFL/Human Brain Project PCO
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

import * as actions from "../actions";
import API from "./API";
import { SearchkitManager, BaseQueryAccessor } from "searchkit";
import { ElasticSearchHelpers } from "../helpers/ElasticSearchHelpers";
import { generateKey } from "../helpers/OIDCHelpers";
import ReactPiwik from "react-piwik";


const regReference = /^((.+)\/(.+))$/;
const regPreviewReference = /^(((.+)\/(.+)\/(.+)\/(.+))\/(.+))$/;

export default class SearchManager {
  constructor(store, searchInterfaceIsDisabled) {
    this.searchInterfaceIsDisabled = !!searchInterfaceIsDisabled;
    this.searchkit = null;
    this.store = store;
    store.subscribe(() => { this.handleStateChange(); });
    this.fromParamRequest = 0;
  }
  initializeSearchKit({ searchApiHost = "", timeout = 5000, queryTweaking, searchOnLoad }) {

    const store = this.store;

    this.searchkit = new SearchkitManager(API.endpoints.search(searchApiHost), {
      multipleSearchers: false,
      timeout: timeout,
      searchOnLoad: searchOnLoad
    });

    this.groupAccessor = new BaseQueryAccessor("group");
    this.searchkit.addAccessor(this.groupAccessor);

    this.searchkit.transport.axios.interceptors.request.use(config => {
      const store = this.store;
      const header = config.headers[config.method];
      const state = store.getState();
      const nonce = generateKey();
      this.fromParamRequest = config.data.from ? Number(config.data.from) : 0;
      header.nonce = nonce;
      if (state.auth.accessToken) {
        header.Authorization = "Bearer " + state.auth.accessToken;
      }
      if (state.auth.accessToken && state.search.group !== null) {
        header["index-hint"] = state.search.group;
      }
      // store.dispatch(actions.loadSearchRequest(nonce));
      return config;
    }, err => {
      return Promise.reject(err);
    });

    this.searchkit.transport.axios.interceptors.response.use(response => {
      //const {config, data, headers, request, status, statusText} = response;
      const { data, headers } = response; const reg = /^kg_(.*)$/;
      const state = this.store && this.store.getState();
      const [, group] = reg.test(headers["x-selected-group"]) ? headers["x-selected-group"].match(reg) : [null, state.search.group];
      let order = null;
      if (group && state && state.groups && state.groups.groups && state.groups.groups.length && state.groups.groupSettings && state.groups.groupSettings[group]) {
        order = state.groups.groupSettings[group].facetTypesOrder;
      } else {
        order = state && state.definition && state.definition.facetTypesOrder;
      }
      if (order) {
        const uuid = this.searchkit.accessors.statefulAccessors["facet_type"] && this.searchkit.accessors.statefulAccessors["facet_type"].uuid;
        uuid && data.aggregations && data.aggregations[uuid] && data.aggregations[uuid]._type && data.aggregations[uuid]._type.buckets instanceof Array && data.aggregations[uuid]._type.buckets.sort((a, b) => {
          if (order[a.key] !== undefined && order[b.key] !== undefined) {
            return order[a.key] - order[b.key];
          }
          if (order[a.key] !== undefined) {
            return -1;
          }
          if (order[b.key] !== undefined) {
            return 1;
          }
          const aCount = Number(a.doc_count);
          const bCount = Number(b.doc_count);
          if (!isNaN(aCount) && !isNaN(bCount)) {
            return bCount - aCount;
          }
          if (!isNaN(bCount)) {
            return -1;
          }
          if (!isNaN(aCount)) {
            return 1;
          }
          return 0;
        });
      }
      ReactPiwik.push(["setCustomUrl", window.location.href]);
      ReactPiwik.push(["trackPageView"]);
      store.dispatch(actions.loadSearchResult(data, group, this.fromParamRequest));
      return response;
    }, error => {
      //const {config, request, response} = error;
      const { response } = error;
      // const {config, data, headers, request, status, statusText} = response;
      const { config, status } = response;
      const headers = config.headers;
      const nonce = headers.nonce;
      const state = store.getState();
      if (!state.search.nonce || (nonce && state.search.nonce && nonce === state.search.nonce)) {
        switch (status) {
        case 400: // Bad Request
        case 404: // Not Found
          store.dispatch(actions.loadSearchBadRequest(status));
          break;
        case 401: // Unauthorized
        case 403: // Forbidden
        case 511: // Network Authentication Required
          store.dispatch(actions.loadSearchSessionFailure(status));
          break;
        default: {
          store.dispatch(actions.loadSearchServiceFailure(status, state.search.group));
        }
        }
      }
      return Promise.reject(error);
    });

    //const queryProcessorFunction = ElasticSearchHelpers.getQueryProcessor(store, this.searchkit, queryTweaking);
    const queryProcessorFunction = ElasticSearchHelpers.getQueryProcessor(this.searchkit, queryTweaking, store);
    this.searchkit.setQueryProcessor(queryProcessorFunction);
  }
  handleStateChange = () => {
    const store = this.store;
    const state = store.getState();

    if (state.configuration.isReady && !state.search.isReady) {
      if (!this.searchInterfaceIsDisabled) {
        this.initializeSearchKit(state.configuration);
      }
      store.dispatch(actions.setSearchReady(true));
      return;
    }

    if (state.search.group && this.groupAccessor && state.search.group !== this.groupAccessor.getQueryString()) {
      this.groupAccessor.setQueryString(state.search.group === API.defaultGroup ? null : state.search.group);
    }

    if (state.search.hasRequest) {
      this.reloadSearch();
    }
    if (state.groups.hasRequest) {
      this.loadGroups();
    }
    if (state.instances.hasRequest) {
      this.loadInstance(state.instances.requestReference);
    }
  }
  loadGroups() {
    const store = this.store;
    const state = store.getState();
    if (!state.groups.isReady && !state.groups.isLoading) {
      setTimeout(() => {
        store.dispatch(actions.loadGroupsRequest());
        if (state.auth.accessToken) {
          const options = {
            method: "get",
            headers: new Headers({
              "Authorization": "Bearer " + state.auth.accessToken
            })
          };
          API.fetch(API.endpoints.groups(state.configuration.searchApiHost), options)
            .then(groups => {
              store.dispatch(actions.loadGroupsSuccess(groups));
            })
            .catch(error => {
              store.dispatch(actions.loadGroupsFailure(error));
            });
        } else {
          store.dispatch(actions.loadGroupsSuccess([]));
        }
      });
    }
  }
  _loadPreview(reference) {
    const store = this.store;
    const state = store.getState();
    if (!state.instances.isLoading) {
      setTimeout(() => {
        store.dispatch(actions.loadInstanceRequest());
        let options = null;
        if (state.auth.accessToken) {
          options = {
            method: "get",
            headers: new Headers({
              "Authorization": "Bearer " + state.auth.accessToken
            })
          };
        }
        const [, , path, , , , , id] = reference.match(regPreviewReference);
        API.fetch(API.endpoints.preview(state.configuration.searchApiHost, path, id), options)
          .then(data => {
            if (data && !data.error) {
              data._id = reference;
              //data._source = data._source instanceof Array?(data._source.length >= 1?data._source[0]:{}):data._source;
              store.dispatch(actions.loadInstanceSuccess(data));
            } else if (data && data.error) {
              store.dispatch(actions.loadInstanceFailure(reference, data.message ? data.message : data.error));
            } else {
              store.dispatch(actions.loadInstanceNoData(reference));
            }
          })
          .catch(error => {
            if (error.stack === "SyntaxError: Unexpected end of JSON input" || error.message === "Unexpected end of JSON input") {
              store.dispatch(actions.loadInstanceNoData(reference));
            } else {
              store.dispatch(actions.loadInstanceFailure(reference, error));
            }
          });
      });
    }
  }
  _loadReference(reference) {
    const store = this.store;
    const state = store.getState();
    if (!state.instances.isLoading) {
      setTimeout(() => {
        store.dispatch(actions.loadInstanceRequest());
        let options = null;
        if (state.auth.accessToken) {
          options = {
            method: "get",
            headers: new Headers({
              "Authorization": "Bearer " + state.auth.accessToken,
              "index-hint": state.search.group
            })
          };
        }
        API.fetch(API.endpoints.instance(state.configuration.searchApiHost, reference), options)
          .then(data => {
            if (data.found) {
              store.dispatch(actions.loadInstanceSuccess(data));
            } else {
              store.dispatch(actions.loadInstanceNoData(reference));
            }
          })
          .catch(error => {
            store.dispatch(actions.loadInstanceFailure(reference, error));
          });
      });
    }
  }
  loadInstance(reference) {
    //window.console.debug("SearchManager loadInstance: " + reference);
    if (regPreviewReference.test(reference)) {
      this._loadPreview(reference);
    } else if (regReference.test(reference)) {
      this._loadReference(reference);
    } else {
      const store = this.store;
      store.dispatch(actions.loadInstanceNoData(reference));
    }
  }
  reloadSearch() {
    this.searchkit && this.searchkit.reloadSearch();
  }
}