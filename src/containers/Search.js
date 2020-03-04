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

import React from "react";
import { connect } from "react-redux";
import * as actionsSearch from "../actions/actions.search";
import * as actionsGroups from "../actions/actions.groups";
import * as actionsDefinition from "../actions/actions.definition";
import * as actionsInstances from "../actions/actions.instances";
import { withTabKeyNavigation } from "../helpers/withTabKeyNavigation";
import { SearchPanel } from "./Search/SearchPanel";
import { TypesFilterPanel } from "./Search/TypesFilterPanel";
import { FiltersPanel } from "./Search/FiltersPanel";
import { ResultsHeader } from "./Search/ResultsHeader";
import { HitsPanel } from "./Search/HitsPanel";
import { Footer } from "./Search/Footer";
import { TermsShortNotice } from "./TermsShortNotice";
import { DetailView } from "./Search/DetailView";
import { DefinitionErrorPanel, GroupErrorPanel, SearchInstanceErrorPanel, SearchErrorPanel } from "./ErrorPanel";
import { getUpdatedQuery, getLocationFromQuery } from "../helpers/BrowserHelpers";

import "./Search.css";
import { history } from "../store";

const SearchComponent = ({show}) => (
  <div className = "kgs-search-container" >
    {show && (
      <React.Fragment>
        <div className = "kgs-search" >
          <SearchPanel />
          <TermsShortNotice className = "kgs-search__terms-short-notice" />
          <div className = "kgs-search__panel" >
            <TypesFilterPanel />
            <FiltersPanel />
            <div className = "kgs-search__main" >
              <ResultsHeader />
              <HitsPanel />
            </div>
          </div>
          <Footer />
        </div>
        <DetailView />
      </React.Fragment>
    )}
    <DefinitionErrorPanel />
    <GroupErrorPanel />
    <SearchErrorPanel />
    <SearchInstanceErrorPanel />
  </div>
);

class SearchBase extends React.Component {

  componentDidMount() {
    const { setInitialSearchParams, setInitialGroup } = this.props;
    document.title = "Knowledge Graph Search";
    const params = this.getUrlParmeters();
    const searchParam = {...params};
    delete searchParam.group;
    setInitialSearchParams(searchParam);
    if (params.group) {
      setInitialGroup(params.group);
    }
    this.unlisten = history.listen( location => {
      const reg = /^#(.+)\/(.+)$/;
      const [,type, id] = reg.test(location.hash) ? location.hash.match(reg) : [null, null, null];
      this.props.goBackToInstance(type, id);
    });
    //this.updateLocation({});
    this.search();
  }

  componentDidUpdate(previousProps) {
    const { definitionIsReady, definitionHasError, isGroupsReady, groupsHasError, group, location } = this.props;
    this.updateLocation(previousProps);
    if (definitionIsReady !== previousProps.definitionIsReady || definitionHasError !== previousProps.definitionHasError ||
      groupsHasError !== previousProps.groupsHasError || isGroupsReady !== previousProps.isGroupsReady || group !== previousProps.group ||
      location.search !== previousProps.location.search) {
      this.search();
    }
    this.updateScrolling();
  }

  componentWillUnmount() {
    this.unlisten();
  }

  calculateFacetList = facets => {
    return facets.reduce((acc, facet) => {
      switch (facet.filterType) {
      case "list":
        facet.keywords.forEach(keyword => {
          acc.push({
            name: facet.id,
            value: keyword.value,
            checked: Array.isArray(facet.value) ? facet.value.includes(keyword.value) : false,
            many: true
          });
        });
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
  }

  updateLocation = (previousProps) => {
    const { queryString, selectedType, facets, facetValues, sort, page, group, defaultGroup, location } = this.props;
    const shouldUpdateQueryString = queryString !== previousProps.queryString;
    const shouldUpdateType = selectedType !== previousProps.selectedType;
    const shouldUpdateFacets = facetValues !== previousProps.facetValues;
    const shouldUpdateSort = sort !== previousProps.sort;
    const shouldUpdatePage = page !== previousProps.page;
    const shouldUpdateGroup = group !== previousProps.group;

    if (shouldUpdateQueryString || shouldUpdateType || shouldUpdateFacets || shouldUpdateSort || shouldUpdatePage || shouldUpdateGroup) {
      let query = location.query;
      if (shouldUpdateQueryString) {
        query = getUpdatedQuery(query, "q", queryString !== "", queryString, false);
      }
      if (shouldUpdateType) {
        query = getUpdatedQuery(query, "facet_type[0]", !!selectedType, selectedType, false);
      }
      if (shouldUpdateFacets) {
        const list = this.calculateFacetList(facets);
        query = list.reduce((acc, item) => getUpdatedQuery(acc, item.name, item.checked, item.value, item.many), query);
      }
      if (shouldUpdateSort) {
        query = getUpdatedQuery(query, "sort", sort && sort !== "newestFirst", sort, false);
      }
      if (shouldUpdatePage) {
        query = getUpdatedQuery(query, "p", page !== 1, page, false);
      }
      if (shouldUpdateGroup) {
        query = getUpdatedQuery(query, "group", group && group !== defaultGroup, group, false);
      }
      const url = getLocationFromQuery(query, location);
      history.push(url);
    }
  }

  updateScrolling = () => {
    const { isActive } = this.props;
    if (isActive) {
      document.documentElement.style.overflow = "";
      document.body.style.overflow = "";
    } else {
      document.documentElement.style.overflow = "hidden";
      document.body.style.overflow = "hidden";
    }
  }

  getUrlParmeters = () => {
    const { location } = this.props;
    const regParamWithBrackets = /^([^[]+)\[(\d+)\]$/; // name[number]
    return Object.entries(location.query).reduce((acc, [key, value]) => {
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
  }

  search() {
    const {
      definitionIsReady, definitionHasError, definitionIsLoading,
      isGroupsReady, isGroupLoading, shouldLoadGroups, groupsHasError,
      loadDefinition, loadGroups, search
    } = this.props;
    if (!definitionIsReady) {
      if (!definitionIsLoading && !definitionHasError) {
        loadDefinition();
      }
    } else if (shouldLoadGroups && !isGroupsReady) {
      if (!isGroupLoading && !groupsHasError) {
        loadGroups();
      }
    } else {
      search();
    }
  }

  render() {
    const { definitionIsReady, definitionHasError, searchHasError, groupsHasError } = this.props;
    return (
      <SearchComponent show={definitionIsReady && !definitionHasError && !groupsHasError && !searchHasError} />
    );
  }
}

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
    location: state.router.location,
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
    sort: state.search.sort?state.search.sort.param:null,
    page: state.search.page
  }),
  dispatch => ({
    setInitialSearchParams: params => dispatch(actionsSearch.setInitialSearchParams(params)),
    setInitialGroup: group => dispatch(actionsGroups.setInitialGroup(group)),
    loadDefinition: () => dispatch(actionsDefinition.loadDefinition()),
    loadGroups: () => dispatch(actionsGroups.loadGroups()),
    search: () => dispatch(actionsSearch.search()),
    goBackToInstance: (type, id) => dispatch(actionsInstances.goBackToInstance(type, id))
  })
)(SearchWithTabKeyNavigation);