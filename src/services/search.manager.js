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
import { SearchKitHelpers } from "../helpers/SearchKitHelpers";
import { generateKey} from "../helpers/OIDCHelpers";

export default class SearchManager {
  constructor(store, searchInterfaceIsDisabled){
    this.searchInterfaceIsDisabled = !!searchInterfaceIsDisabled;
    this.searchkit = null;
    this.store = store;
    store.subscribe(() => {this.handleStateChange();});
    this.fromParamRequest = 0;
  }
  initializeSearchKit({searchApiHost="", timeout=5000, queryTweaking, searchOnLoad}) {

    const store = this.store;

    this.searchkit = new SearchkitManager(API.endpoints.search(searchApiHost), {
      multipleSearchers:false,
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
      this.fromParamRequest = config.data.from?Number(config.data.from):0;
      header.nonce = nonce;
      if (state.auth.accessToken) {
        header.Authorization = "Bearer " + state.auth.accessToken;
      }
      if (state.auth.accessToken && state.search.group !== null) {
        header["index-hint"] = state.search.group;
      }
      store.dispatch(actions.loadSearchRequest(nonce));
      return config;
    }, err => {
      return Promise.reject(err);
    });

    this.searchkit.transport.axios.interceptors.response.use(response => {
      //const {config, data, headers, request, status, statusText} = response;
      const {data, headers} = response;      const reg = /^kg_(.*)$/;
      const state = this.store && this.store.getState();
      const [,group] = reg.test(headers["x-selected-group"])?headers["x-selected-group"].match(reg):[null,state.search.group];
      let order = null;
      if (group && state && state.groups && state.groups.groups && state.groups.groups.length && state.groups.groupSettings && state.groups.groupSettings[group]) {
        order = state.groups.groupSettings[group].facetTypesOrder;
      } else {
        order = state && state.definition && state.definition.facetTypesOrder;
      }
      if (order) {
        const uuid = this.searchkit.accessors.statefulAccessors["facet_type"] && this.searchkit.accessors.statefulAccessors["facet_type"].uuid;
        uuid &&  data.aggregations && data.aggregations[uuid] &&  data.aggregations[uuid]._type && data.aggregations[uuid]._type.buckets instanceof Array && data.aggregations[uuid]._type.buckets.sort((a,b) => {
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
      store.dispatch(actions.loadSearchResult(data, group, this.fromParamRequest));
      return response;
    }, error => {
      //const {config, request, response} = error;
      const {response} = error;
      // const {config, data, headers, request, status, statusText} = response;
      const {config, status} = response;
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

    //const queryProcessorFunction = SearchKitHelpers.getQueryProcessor(store, this.searchkit, queryTweaking);
    const queryProcessorFunction = SearchKitHelpers.getQueryProcessor(this.searchkit, queryTweaking);
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
      this.groupAccessor.setQueryString(state.search.group === API.defaultGroup?null:state.search.group);
    }

    if (state.search.hasRequest) {
      this.reloadSearch();
    }
    if (state.definition.hasRequest) {
      this.loadDefinition();
    }
    if (state.groups.hasRequest) {
      this.loadGroups();
    }
    if (state.instances.hasRequest) {
      this.loadInstance(state.instances.requestReference);
    }
  }
  loadDefinition() {
    const store = this.store;
    const state = store.getState();
    if (!state.definition.isReady && !state.definition.isLoading) {
      setTimeout(() => {
        store.dispatch(actions.loadDefinitionRequest());
        API.fetch(API.endpoints.definition(state.configuration.searchApiHost))
          .then(definition => {
            store.dispatch(actions.loadDefinitionSuccess(definition));
          })
          .catch(error => {
            store.dispatch(actions.loadDefinitionFailure(error));
          });
      });
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
  loadInstance(id) {
    //window.console.debug("SearchManager loadInstance: " + id);
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
        API.fetch(API.endpoints.instance(state.configuration.searchApiHost, id), options)
          .then(data => {
            if (data.found) {
              store.dispatch(actions.loadInstanceSuccess(data));
            } else {
              store.dispatch(actions.loadInstanceNoData(id));
            }
          })
          .catch(error => {
            store.dispatch(actions.loadInstanceFailure(id, error));
          });
      });
    }
  }
  reloadSearch() {
    this.searchkit && this.searchkit.reloadSearch();
  }
}