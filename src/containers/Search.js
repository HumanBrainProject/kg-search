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
import * as actions from "../actions";
import { withTabKeyNavigation } from "../helpers/withTabKeyNavigation";
import { SearchPanel } from "./Search/SearchPanel";
import { TypesFilterPanel } from "./Search/TypesFilterPanel";
import { FiltersPanel } from "./Search/FiltersPanel";
import { ResultsHeader } from "./Search/ResultsHeader";
import { HitsPanel } from "./Search/HitsPanel";
import { Footer } from "./Search/Footer";
import { TermsShortNotice } from "./TermsShortNotice";
import { DetailView } from "./Search/DetailView";

import "./Search.css";

class SearchBase extends React.Component {

  componentDidMount() {
    const { setInitialSearchParams, setInitialGroup } = this.props;
    const params = this.getUrlParmeters();
    const searchParam = {...params};
    delete searchParam.group;
    setInitialSearchParams(searchParam);
    if (params.group) {
      setInitialGroup(params.group);
    }
    this.search();
  }

  componentDidUpdate(previousProps) {
    const { definitionIsReady, isGroupsReady, location } = this.props;
    if (definitionIsReady !== previousProps.definitionIsReady || isGroupsReady !== previousProps.isGroupsReady || location.search !== previousProps.location.search) {
      this.search();
    }
  }

  getUrlParmeters() {
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
    const { definitionIsReady, definitionIsLoading, loadDefinition, isGroupsReady, isGroupLoading, shouldLoadGroups, loadGroups, search } = this.props;
    if (!definitionIsReady) {
      if (!definitionIsLoading) {
        loadDefinition();
      }
    } else if (shouldLoadGroups && !isGroupsReady) {
      if (!isGroupLoading) {
        loadGroups();
      }
    } else {
      search();
    }
  }

  render() {
    const { definitionIsReady } = this.props;

    if (!definitionIsReady) {
      return null;
    }

    return (
      <div className = "kgs-search-container" >
        <div className = "kgs-search" >
          <SearchPanel />
          <TermsShortNotice className = "kgs-search__terms-short-notice" />
          <TypesFilterPanel />
          <div className = "kgs-search__panel" >
            <FiltersPanel />
            <div className = "kgs-search__main" >
              <ResultsHeader />
              <HitsPanel />
            </div>
          </div>
          <Footer />
        </div>
        <DetailView />
      </div>
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
    isGroupsReady: state.groups.isReady,
    isGroupLoading: state.groups.isLoading,
    shouldLoadGroups: !!state.auth.accessToken,
    location: state.router.location
  }),
  dispatch => ({
    setInitialSearchParams: params => dispatch(actions.setInitialSearchParams(params)),
    setInitialGroup: group => dispatch(actions.setInitialGroup(group)),
    loadDefinition: () => dispatch(actions.loadDefinition()),
    loadGroups: () => dispatch(actions.loadGroups()),
    search: () => dispatch(actions.search())
  })
)(SearchWithTabKeyNavigation);