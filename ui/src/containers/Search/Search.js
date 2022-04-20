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

import React, { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { connect } from "react-redux";
import * as actionsSearch from "../../actions/actions.search";
import * as actionsGroups from "../../actions/actions.groups";
import * as actionsDefinition from "../../actions/actions.definition";
import * as actionsInstances from "../../actions/actions.instances";
import { withTabKeyNavigation } from "../../helpers/withTabKeyNavigation";
import { SearchPanel } from "./SearchPanel/SearchPanel";
import { TypesFilterPanel } from "./Facet/TypesFilterPanel";
import { FiltersPanel } from "./Facet/FiltersPanel";
import { ResultsHeader } from "./ResultsHeader/ResultsHeader";
import { HitsPanel } from "./Hit/HitsPanel";
import { Footer } from "./Footer/Footer";
import { TermsShortNotice } from "../Notice/TermsShortNotice";
import { DetailView } from "./Detail/DetailView";
import { DefinitionErrorPanel, GroupErrorPanel, SearchInstanceErrorPanel, SearchErrorPanel } from "../Error/ErrorPanel";
import { getUpdatedQuery, getLocationFromQuery, searchToObj } from "../../helpers/BrowserHelpers";

import "./Search.css";

const SearchComponent = ({ show }) => (
  <div className="kgs-search-container" >
    {show && (
      <>
        <div className="kgs-search" >
          <SearchPanel />
          <TermsShortNotice className="kgs-search__terms-short-notice" />
          <div className="kgs-search__panel" >
            <TypesFilterPanel />
            <FiltersPanel />
            <div className="kgs-search__main" >
              <ResultsHeader />
              <HitsPanel />
            </div>
          </div>
          <Footer />
        </div>
        <DetailView />
      </>
    )}
    <DefinitionErrorPanel />
    <GroupErrorPanel />
    <SearchErrorPanel />
    <SearchInstanceErrorPanel />
  </div>
);


const calculateFacetList = facets => {
  return facets.reduce((acc, facet) => {
    switch (facet.filterType) {
    case "list":
      if (facet.isHierarchical) {
        facet.keywords.forEach(keyword => {
          keyword.children && Array.isArray(keyword.children.keywords) && keyword.children.keywords.forEach(child => {
            acc.push({
              name: facet.id,
              value: child.value,
              checked: Array.isArray(facet.value) ? facet.value.includes(child.value) : false,
              many: true
            });
          });
        });
      } else {
        facet.keywords.forEach(keyword => {
          acc.push({
            name: facet.id,
            value: keyword.value,
            checked: Array.isArray(facet.value) ? facet.value.includes(keyword.value) : false,
            many: true
          });
        });
      }
      break;
    case "exists":
      acc.push({
        name: facet.id,
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
};

const getUrlParmeters = () => {
  const regParamWithBrackets = /^([^[]+)\[(\d+)\]$/; // name[number]
  const query = searchToObj();
  const res = Object.entries(query).reduce((acc, [key, value]) => {
    const [, name, count] = regParamWithBrackets.test(key) ? key.match(regParamWithBrackets) : [null, key, null];
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
  return res;
};


const SearchBase = ({ setInitialSearchParams, goBackToInstance, isActive, queryString, selectedType, facets, sort, page, group, defaultGroup, definitionIsReady, definitionIsLoading, definitionHasError, loadDefinition, shouldLoadGroups, isGroupLoading, isGroupsReady, loadGroups, groupsHasError, searchHasError, search, isUpToDate }) => {

  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    document.title = "EBRAINS - Knowledge Graph Search";
    const params = getUrlParmeters();
    const searchParam = { ...params };
    delete searchParam.group;
    setInitialSearchParams(searchParam);
    window.onpopstate = () => {
      const reg = /^#(.+)$/;
      const [, id] = reg.test(location.hash) ? location.hash.match(reg) : [null, null];
      goBackToInstance(id);
    };
  }, []);

  useEffect(() => {
    if (isActive) {
      document.documentElement.style.overflow = "";
      document.body.style.overflow = "";
    } else {
      document.documentElement.style.overflow = "hidden";
      document.body.style.overflow = "hidden";
    }
  }, [isActive]);

  useEffect(() => {
    let query = searchToObj();
    query = getUpdatedQuery(query, "q", queryString !== "", queryString, false);
    query = getUpdatedQuery(query, "facet_type[0]", !!selectedType, selectedType, false);
    const list = calculateFacetList(facets);
    query = list.reduce((acc, item) => getUpdatedQuery(acc, item.name, item.checked, item.value, item.many), query);
    query = getUpdatedQuery(query, "sort", sort && sort !== "newestFirst", sort, false);
    query = getUpdatedQuery(query, "p", page !== 1, page, false);
    query = getUpdatedQuery(query, "group", group && group !== defaultGroup, group, false);
    const newUrl = getLocationFromQuery(query, location);
    if (newUrl) {
      navigate(newUrl);
    }
  }, [queryString, selectedType, sort, page, group, facets]);

  useEffect(() => {
    if (!definitionIsReady) {
      if (!definitionIsLoading && !definitionHasError) {
        loadDefinition();
      }
    } else if (shouldLoadGroups && !isGroupsReady) {
      if (!isGroupLoading && !groupsHasError) {
        loadGroups();
      }
    } else if (!isUpToDate) {
      search();
    }
  }, [definitionIsReady, definitionIsLoading, definitionHasError, shouldLoadGroups, isGroupsReady, isGroupLoading, groupsHasError, isUpToDate]);


  return (
    <SearchComponent show={definitionIsReady && !definitionHasError && !groupsHasError && !searchHasError} />
  );
};

export const SearchWithTabKeyNavigation = withTabKeyNavigation(
  "isActive",
  ".kgs-search",
  ".kgs-instance"
)(SearchBase);

export const Search = connect(
  state => ({
    isActive: !state.instances.currentInstance && !state.application.info,
    definitionIsReady: state.definition.isReady,
    definitionIsLoading: state.definition.isLoading,
    definitionHasError: !!state.definition.error,
    group: state.groups.group,
    defaultGroup: state.groups.defaultGroup,
    groupsHasError: !!state.groups.error,
    isGroupsReady: state.groups.isReady,
    isGroupLoading: state.groups.isLoading,
    shouldLoadGroups: !!state.auth.accessToken,
    searchHasError: !!state.search.error,
    queryString: state.search.queryString,
    selectedType: state.search.selectedType,
    facets: state.search.facets.filter(f =>
      state.search.selectedType === f.type &&
      f.count > 0 &&
      (f.filterType !== "list" || f.keywords.length)
    ),
    facetValues: state.search.facets.reduce((acc, facet) => {
      acc += Array.isArray(facet.value) ? facet.value.toString() : facet.value;
      return acc;
    }, ""),
    sort: state.search.sort ? state.search.sort.param : null,
    page: state.search.page,
    isUpToDate: state.search.isUpToDate
  }),
  dispatch => ({
    setInitialSearchParams: params => dispatch(actionsSearch.setInitialSearchParams(params)),
    loadDefinition: () => dispatch(actionsDefinition.loadDefinition()),
    loadGroups: () => dispatch(actionsGroups.loadGroups()),
    search: () => dispatch(actionsSearch.search()),
    goBackToInstance: id => dispatch(actionsInstances.goBackToInstance(id))
  })
)(SearchWithTabKeyNavigation);