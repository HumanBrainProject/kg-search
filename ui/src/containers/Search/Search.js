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
import * as actionsInstances from "../../actions/actions.instances";
import { withTabKeyNavigation } from "../../helpers/withTabKeyNavigation";
import { SearchPanel } from "./SearchPanel/SearchPanel";
import { TypesFilterPanel } from "./Facet/TypesFilterPanel";
import { FiltersPanel } from "./Facet/FiltersPanel";
import { HitsInfo } from "./HitsInfo/HitsInfo";
import { Hits } from "./Hit/Hits";
import { Footer } from "./Footer/Footer";
import { TermsShortNotice } from "../Notice/TermsShortNotice";
import { DetailView } from "./Detail/DetailView";
import {
  SearchInstanceErrorPanel,
  SearchErrorPanel
} from "../Error/ErrorPanel";
import {
  getUpdatedQuery,
  getLocationSearchFromQuery,
  searchToObj
} from "../../helpers/BrowserHelpers";
import { getActiveFacets } from "../../helpers/Facets";

import "./Search.css";

const SearchComponent = ({ show, showKnowledgeSpaceLink, queryString }) => (
  <div className="kgs-search-container">
    {show && (
      <>
        <div className="kgs-search">
          <SearchPanel />
          <TermsShortNotice className="kgs-search__terms-short-notice" />
          <div className="kgs-search__panel">
            <TypesFilterPanel />
            <FiltersPanel />
            <div className="kgs-search__main">
              <HitsInfo />
              <Hits />
              {showKnowledgeSpaceLink && (
                <div className="kgs-search__knowledge-space">
                  Not found what you&apos;re looking for? Have a look at the&nbsp;
                  <a
                    href={`https://knowledge-space.org/search?q=${queryString}`}
                    rel="noreferrer"
                    target="_blank"
                  >
                    Knowledge Space
                  </a>
                </div>
              )}
            </div>
          </div>
          <Footer />
        </div>
        <DetailView />
      </>
    )}
    <SearchErrorPanel />
    <SearchInstanceErrorPanel />
  </div>
);

const calculateFacetList = facets => {
  return facets.reduce((acc, facet) => {
    switch (facet.type) {
    case "list":
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
    case "exists":
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
};

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

const SearchBase = ({
  initializeSearch,
  goBackToInstance,
  isActive,
  queryString,
  selectedType,
  facets,
  page,
  totalPages,
  group,
  defaultGroup,
  searchHasError,
  search,
  isUpToDate
}) => {
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    document.title = "EBRAINS - Knowledge Graph Search";
    const params = getUrlParmeters();
    const searchParam = { ...params };
    delete searchParam.group;
    initializeSearch(searchParam);
    window.onpopstate = () => {
      const reg = /^#(.+)$/;
      const [, id] = reg.test(window.location.hash)
        ? window.location.hash.match(reg)
        : [null, null];
      goBackToInstance(id);
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
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
    query = getUpdatedQuery(
      query,
      "q",
      queryString !== "",
      queryString,
      false
    );
    query = getUpdatedQuery(
      query,
      "category",
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
    query = getUpdatedQuery(query, "p", page !== 1, page, false);
    query = getUpdatedQuery(
      query,
      "group",
      group && group !== defaultGroup,
      group,
      false
    );
    const newLocationSearch = getLocationSearchFromQuery(query);
    if (newLocationSearch !== location.search) {
      navigate(`${location.pathname}${newLocationSearch}`, {
        replace: !window.location.search
      }); // replace no type at initialisation
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    queryString,
    selectedType,
    page,
    group,
    facets
  ]);

  useEffect(() => {
    if (!isUpToDate) {
      search();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isUpToDate]);

  const showKnowledgeSpaceLink =
    (!totalPages || page === totalPages) &&
    typeof queryString === "string" &&
    queryString.length > 0;
  return (
    <SearchComponent
      show={!searchHasError}
      showKnowledgeSpaceLink={showKnowledgeSpaceLink}
      queryString={queryString}
    />
  );
};

export const SearchWithTabKeyNavigation = withTabKeyNavigation(
  "isActive",
  ".kgs-search",
  ".kgs-instance"
)(SearchBase);

const Search = connect(
  state => ({
    isActive: !state.instances.currentInstance && !state.application.info,
    group: state.groups.group,
    defaultGroup: state.groups.defaultGroup,
    searchHasError: !!state.search.error,
    queryString: state.search.queryString,
    selectedType: state.search.selectedType?.type,
    facets: getActiveFacets(state.search.selectedType?.facets),
    page: state.search.page,
    totalPages: state.search.totalPages,
    isUpToDate: state.search.isUpToDate
  }),
  dispatch => ({
    initializeSearch: params => {
      dispatch(actionsSearch.initializeSearch(params));
    },
    search: () => {
      dispatch(actionsSearch.search());
    },
    goBackToInstance: id => {
      dispatch(actionsInstances.goBackToInstance(id));
    }
  })
)(SearchWithTabKeyNavigation);

export default Search;
