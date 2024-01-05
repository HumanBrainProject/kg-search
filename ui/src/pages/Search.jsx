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

import React, { useEffect, useRef } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { useLocation, useNavigate } from 'react-router-dom';

import BgError from '../components/BgError/BgError';
import TermsShortNotice from '../features/TermsShortNotice';
import { setGroup } from '../features/groups/groupsSlice';
import { syncHistory } from '../features/instance/instanceSlice';
import KnowledgeSpaceLink from '../features/search/KnowledgeSpaceLink';
import SearchBox from '../features/search/SearchBox';
import {
  initializeSearch,
  syncSearchParameters,
  setSearchResults,
  selectFacets
} from '../features/search/searchSlice';
import {
  getUpdatedQuery,
  getLocationSearchFromQuery,
  searchToObj
} from '../helpers/BrowserHelpers';
import { getAggregation } from '../helpers/Facets';
import { withTabKeyNavigation } from '../helpers/withTabKeyNavigation';
import Matomo from '../services/Matomo';
import {
  useGetSearchQuery,
  getError,
} from '../services/api';

import Detail from './Search/Detail/Detail';
import FiltersPanel from './Search/Facet/FiltersPanel';
import TypesFilterPanel from './Search/Facet/TypesFilterPanel';
import Footer from './Search/Footer/Footer';
import Hits from './Search/Hit/Hits';
import HitsInfo from './Search/HitsInfo/HitsInfo';
import SearchFetching from './Search/SearchFetching';

import './Search.css';

const calculateFacetList = facets => facets.reduce((acc, facet) => {
  switch (facet.type) {
  case 'list':
    if (facet.isHierarchical) {
      facet.keywords.forEach(keyword => {
        keyword.children &&
              Array.isArray(keyword.children.keywords) &&
              keyword.children.keywords.forEach(child => {
                acc.push({
                  name: facet.name,
                  value: child.value,
                  checked: Array.isArray(facet.value)
                    ? facet.value.includes(child.value)
                    : false,
                  many: true
                });
              });
      });
    } else {
      facet.keywords.forEach(keyword => {
        acc.push({
          name: facet.name,
          value: keyword.value,
          checked: Array.isArray(facet.value)
            ? facet.value.includes(keyword.value)
            : false,
          many: true
        });
      });
    }
    break;
  case 'exists':
    acc.push({
      name: facet.name,
      value: !!facet.value,
      checked: !!facet.value,
      many: false
    });
    break;
  default:
    break;
  }
  return acc;
}, []);

const getUrlParmeters = () => {
  const regParamWithBrackets = /^([^[]+)\[(\d+)\]$/; // name[number]
  const query = searchToObj();
  return Object.entries(query).reduce((acc, [key, value]) => {
    const [, name, count] = regParamWithBrackets.test(key)
      ? key.match(regParamWithBrackets)
      : [null, key, null];
    const val = decodeURIComponent(value);
    if (count) {
      if (!acc[name]) {
        acc[name] = [];
      }
      acc[name].push(val);
    } else {
      acc[name] = val;
    }
    return acc;
  }, {});
};

const getIdFromUrl = () => {
  const reg = /^#(.+)$/;
  const [, id] = reg.test(window.location.hash)
    ? window.location.hash.match(reg)
    : [null, null];
  return id;
};

const getSearchParametersFromUrl = () => {
  const params = getUrlParmeters();
  const searchParams = { ...params };
  delete searchParams.group;
  return searchParams;
};

const getGroupFromUrl = () => {
  const params = getUrlParmeters();
  return params.group;
};

const SearchBase = () => {
  const initializedRef = useRef(false);
  const locationSearchRef = useRef(null);
  const newLocationSearchRef = useRef(null);

  const location = useLocation();
  const navigate = useNavigate();

  const dispatch = useDispatch();

  const isActive = useSelector(
    state => !state.instance.instanceId && !state.application.info
  );
  const isInitialized = useSelector(state => state.search.isInitialized);
  const group = useSelector(state => state.groups.group);
  const defaultGroup = useSelector(state => state.groups.defaultGroup);
  const queryString = useSelector(state => state.search.queryString);
  const selectedType = useSelector(state => state.search.selectedType);
  const from = useSelector(state => state.search.from);
  const hitsPerPage = useSelector(state => state.search.hitsPerPage);

  const facets = useSelector(state => selectFacets(state, selectedType));

  const page = useSelector(state => state.search.page);
  const isUpToDate = useSelector(state => state.search.isUpToDate);

  const searchParams = isInitialized
    ? {
      group: group,
      q: queryString,
      type: selectedType,
      from: from,
      size: hitsPerPage,
      payload: getAggregation(facets)
    }
    : {};

  const {
    data,
    //currentData,
    error,
    //isUninitialized,
    //isLoading,
    //isFetching,
    //isSuccess,
    isError,
    refetch
  } = useGetSearchQuery(searchParams, { skip: !isInitialized });

  useEffect(() => {
    document.title = 'EBRAINS - Knowledge Graph Search';
    if (!initializedRef.current) {
      initializedRef.current = true;
      const params = getSearchParametersFromUrl();
      dispatch(initializeSearch(params));
    }
    const popstateHandler = () => {
      const id = getIdFromUrl();
      dispatch(syncHistory(id));

      const group = getGroupFromUrl();
      dispatch(setGroup(group));

      if (!id) {
        const params = getSearchParametersFromUrl();
        dispatch(syncSearchParameters(params));
      }
    };
    window.addEventListener('popstate', popstateHandler, false);
    return () => {
      window.removeEventListener('popstate', popstateHandler);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (isActive) {
      document.documentElement.style.overflow = '';
      document.body.style.overflow = '';
    } else {
      document.documentElement.style.overflow = 'hidden';
      document.body.style.overflow = 'hidden';
    }
  }, [isActive]);

  useEffect(() => {
    if (isInitialized && isActive) {
      let query = searchToObj();
      query = getUpdatedQuery(
        query,
        'q',
        queryString !== '',
        queryString,
        false
      );
      query = getUpdatedQuery(
        query,
        'category',
        !!selectedType,
        selectedType,
        false
      );
      const list = calculateFacetList(facets);
      query = list.reduce(
        (acc, item) =>
          getUpdatedQuery(acc, item.name, item.checked, item.value, item.many),
        query
      );
      query = getUpdatedQuery(query, 'p', page !== 1, page, false);
      query = getUpdatedQuery(
        query,
        'group',
        group && group !== defaultGroup,
        group,
        false
      );
      const newLocationSearch = getLocationSearchFromQuery(query);
      if (newLocationSearchRef.current !== newLocationSearch) {
        const first = !newLocationSearchRef.current;
        newLocationSearchRef.current = newLocationSearch;
        navigate(`${location.pathname}${newLocationSearch}`, {
          replace: first
        }); // replace no type at initialisation
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isActive, isInitialized, queryString, selectedType, page, group, facets]);

  useEffect(() => {
    if (isInitialized && isActive) {
      if (locationSearchRef.current !== window.location.href) {
        locationSearchRef.current = window.location.href;
        Matomo.setCustomUrl(window.location.href);
        Matomo.trackPageView();
      }
    }
  }, [isInitialized, isActive, location.search]);

  useEffect(() => {
    if (isInitialized && data) {
      dispatch(setSearchResults(data));
    }
  }, [data, isInitialized, isUpToDate, dispatch]);

  if (isError) {
    return (
      <div className="kgs-search-container">
        <BgError
          message={getError(error)}
          onRetryClick={refetch}
          retryVariant="primary"
        />
      </div>
    );
  }

  return (
    <>
      <div className="kgs-search-container">
        <div className="kgs-search">
          <SearchBox />
          <TermsShortNotice className="kgs-search__terms-short-notice" />
          <div className="kgs-search__panel">
            <TypesFilterPanel />
            <FiltersPanel />
            <div className="kgs-search__main">
              <HitsInfo />
              <Hits />
              <KnowledgeSpaceLink />
            </div>
          </div>
          <Footer />
        </div>
        <Detail />
      </div>
      <SearchFetching />
    </>
  );
};

export const Search = withTabKeyNavigation(
  'isActive',
  '.kgs-search',
  '.kgs-instance'
)(SearchBase);

export default Search;

