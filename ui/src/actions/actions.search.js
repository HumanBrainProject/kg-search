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

import { setGroup } from "./actions.groups";
import * as types from "./actions.types";
import API from "../services/API";
import ReactPiwik from "react-piwik";
import { ElasticSearchHelpers } from "../helpers/ElasticSearchHelpers";
import { sessionFailure } from "./actions";
import * as Sentry from "@sentry/browser";

export const loadSearchBadRequest = error => {
  return {
    type: types.LOAD_SEARCH_BAD_REQUEST,
    error: error
  };
};

export const loadSearchServiceFailure = error => {
  return {
    type: types.LOAD_SEARCH_SERVICE_FAILURE,
    error: error
  };
};

export const clearSearchError = () => {
  return {
    type: types.CLEAR_SEARCH_ERROR
  };
};

export const loadSearchRequest = () => {
  return {
    type: types.LOAD_SEARCH_REQUEST
  };
};

export const loadSearchResult = results => {
  return {
    type: types.LOAD_SEARCH_SUCCESS,
    results: results
  };
};

export const cancelSearch = () => {
  return {
    type: types.CANCEL_SEARCH
  };
};

export const setQueryString = value => {
  return {
    type: types.SET_QUERY_STRING,
    queryString: value
  };
};

export const setType = value => {
  return {
    type: types.SET_TYPE,
    value: value
  };
};

export const setSort = value => {
  return {
    type: types.SET_SORT,
    value: value
  };
};

export const setPage = value => {
  return {
    type: types.SET_PAGE,
    value: value
  };
};

export const setFacet = (id, active, keyword) => {
  return {
    type: types.SET_FACET,
    id: id,
    active: active,
    keyword: keyword,
  };
};

export const resetFacets = () => {
  return {
    type: types.RESET_FACETS
  };
};

export const setFacetSize = (id, size) => {
  return dispatch => {
    dispatch({
      type: types.SET_FACET_SIZE,
      id: id,
      size: size,
    });
    dispatch(search());
  };
};

export const setInitialSearchParams = params => {
  return {
    type: types.SET_INITIAL_SEARCH_PARAMS,
    params: params
  };
};

export const search = () => {
  return (dispatch, getState) => {
    const state = getState();
    const payload = ElasticSearchHelpers.buildRequest(state.search);
    dispatch(loadSearchRequest());
    ReactPiwik.push(["setCustomUrl", window.location.href]);
    ReactPiwik.push(["trackPageView"]);
    API.axios
      .post(API.endpoints.search(state.groups.group, state.search.selectedType), payload)
      //.get(API.endpoints.search(state.groups.group, state.search.selectedType), payload)
      .then(response => {
        const index = response.headers["x-selected-index"];
        if (index) {
          dispatch(setGroup(index));
        }
        dispatch(loadSearchResult(response.data));
      })
      .catch(e => {
        const { response } = e;
        const { status } = response;
        switch (status) {
        case 400: // Bad Request
        {
          const error = `Your search query is not well formed. Please refine your request (${status})`;
          dispatch(loadSearchBadRequest(error));
          break;
        }
        case 401: // Unauthorized
        case 403: // Forbidden
        case 511: // Network Authentication Required
        {
          const error = "Your session has expired. Please login again.";
          dispatch(sessionFailure(error));
          break;
        }
        case 500:
        {
          Sentry.captureException(e);
          break;
        }
        case 404:
        default:
        {
          const index = response.headers["x-selected-index"];
          if (index) {
            dispatch(setGroup(index));
          }
          const error = `Your search query is not well formed. Please refine your request (${status})`;
          dispatch(loadSearchServiceFailure(error));
        }
        }
      });
  };
};