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

import { store, dispatch } from "./store";
import * as actions from "./actions";
import API from "./API";
import { SearchkitManager } from "searchkit";
import { SearchKitHelpers } from "./Helpers/SearchKitHelpers";
import { generateKey} from "./Helpers/OIDCHelpers";

export default class SearchManager {
  constructor(){
    this.searchkit = null;
    document.addEventListener("state", this.handleStateChange.bind(this), false);
    this.fromParamRequest = 0;
  }
  initializeSearchKit({searchApiHost="", timeout=5000, queryTweaking, searchOnLoad}) {
    this.searchkit = new SearchkitManager(API.endpoints.search(searchApiHost), {
      multipleSearchers:false,
      timeout: timeout,
      searchOnLoad: searchOnLoad
    });

    this.searchkit.transport.axios.interceptors.request.use(config => {
      const header = config.headers[config.method];
      const state = store.getState();
      const nonce = generateKey();
      this.fromParamRequest = config.data.from?Number(config.data.from):0;
      header.nonce = nonce;
      if (state.auth.accessToken) {
        header.Authorization = "Bearer " + state.auth.accessToken;
      }
      if (state.auth.accessToken && state.search.index !== null) {
        header["index-hint"] = state.search.index;
      }
      dispatch(actions.loadSearchRequest(nonce));
      return config;
    }, err => {
      return Promise.reject(err);
    });
    this.searchkit.transport.axios.interceptors.response.use(response => {
      //const {config, data, headers, request, status, statusText} = response;
      const {data, headers} = response;
      const reg = /^kg_(.*)$/;
      const [,index] = reg.test(headers.selected_index)?headers.selected_index.match(reg):[];
      dispatch(actions.loadSearchResult(data, index, this.fromParamRequest));
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
          dispatch(actions.loadSearchBadRequest(status));
          break;
        case 401: // Unauthorized
        case 403: // Forbidden
        case 511: // Network Authentication Required
          dispatch(actions.loadSearchSessionFailure(status));
          break;
        default: {
          dispatch(actions.loadSearchServiceFailure(status, state.search.index));
        }
        }
      }
      return Promise.reject(error);
    });

    const queryProcessorFunction = SearchKitHelpers.getQueryProcessor(this.searchkit, queryTweaking);
    this.searchkit.setQueryProcessor(queryProcessorFunction);
  }
  handleStateChange() {
    //setTimeout(() => {
    const state = store.getState();

    if (state.configuration.isReady && !state.search.isReady) {
      this.initializeSearchKit(state.configuration);
      dispatch(actions.setSearchReady(true));
      return;
    }

    if (state.search.hasRequest) {
      this.reloadSearch();
    }
    if (state.definition.hasRequest) {
      this.loadDefinition();
    }
    if (state.indexes.hasRequest) {
      this.loadIndexes();
    }
    if (state.hits.hasRequest) {
      this.loadInstance(state.hits.nextHitReference);
    }
    //});
  }
  loadDefinition() {
    const state = store.getState();
    if (!state.definition.isReady && !state.definition.isLoading) {
      dispatch(actions.loadDefinitionRequest());
      setTimeout(() => {
        API.fetch(API.endpoints.definition(state.configuration.searchApiHost))
          .then(definition => {
            dispatch(actions.loadDefinitionSuccess(definition));
          })
          .catch(error => {
            dispatch(actions.loadDefinitionFailure(error));
          });
      });
    }
  }
  loadIndexes() {
    const state = store.getState();
    if (!state.indexes.isReady && !state.indexes.isLoading) {
      dispatch(actions.loadIndexesRequest());
      setTimeout(() => {
        if (state.auth.accessToken) {
          const options = {
            method: "get",
            headers: new Headers({
              "Authorization": "Bearer " + state.auth.accessToken
            })
          };
          API.fetch(API.endpoints.indexes(state.configuration.searchApiHost), options)
            .then(indexes => {
              dispatch(actions.loadIndexesSuccess(indexes));
            })
            .catch(error => {
              dispatch(actions.loadIndexesFailure(error));
            });
        } else {
          dispatch(actions.loadIndexesSuccess([]));
        }
      });
    }
  }
  loadInstance(id) {
    //window.console.debug("SearchManager loadInstance: " + id);
    const state = store.getState();
    if (!state.hits.isLoading) {
      dispatch(actions.loadHitRequest());
      setTimeout(() => {
        let options = null;
        if (state.auth.accessToken) {
          options = {
            method: "get",
            headers: new Headers({
              "Authorization": "Bearer " + state.auth.accessToken,
              "index-hint": state.search.index
            })
          };
        }
        API.fetch(API.endpoints.instance(state.configuration.searchApiHost, id), options)
          .then(data => {
            if (data.found) {
              dispatch(actions.loadHitSuccess(data));
            } else {
              dispatch(actions.loadHitNoData(id));
            }
          })
          .catch(error => {
            dispatch(actions.loadHitFailure(id, error));
          });
      });
    }
  }
  reloadSearch() {
    this.searchkit.reloadSearch();
  }
}