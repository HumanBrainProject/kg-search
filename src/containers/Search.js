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
import { ShapesFilterPanel } from "./Search/ShapesFilterPanel";
import { FiltersPanel } from "./Search/FiltersPanel";
import { ResultsHeader } from "./Search/ResultsHeader";
import { HitsPanel } from "./Search/HitsPanel";
import { Footer } from "./Search/Footer";
import { TermsShortNotice } from "./TermsShortNotice";
import { DetailView } from "./Search/DetailView";

import "./Search.css";
import { ElasticSearchHelpers } from "../helpers/ElasticSearchHelpers";

const regParamWithBrackets = /^([^[]+)\[(\d+)\]$/;// name[number]

class SearchBase extends React.Component {

  componentDidMount() {
    const { definitionIsReady, definitionIsLoading, loadDefinition, location, setInitial } = this.props;
    if (!definitionIsReady && !definitionIsLoading) {
      loadDefinition();
    }
    const initial = Object.entries(location.query).reduce((acc, [key, value]) => {
      const [, name, count] = regParamWithBrackets.test(key)?key.match(regParamWithBrackets):[null, key, null];
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
    setInitial(initial);
  }

  componentDidUpdate(previousProps) {
    const { definitionIsReady, searchParams, group, search, location} = this.props;
    if (definitionIsReady  && (definitionIsReady !== previousProps.definitionIsReady || (location.search !== previousProps.location.search))) {
      search(searchParams, group);
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
          <ShapesFilterPanel />
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
    searchParams: ElasticSearchHelpers.getSearchParamsFromState(state),
    group: state.search.group,
    location: state.router.location
  }),
  dispatch => ({
    loadDefinition: () => dispatch(actions.loadDefinition()),
    search: (searchParams, group) => dispatch(actions.doSearch(searchParams, group)),
    setInitial: initial => dispatch(actions.setInitial(initial))
  })
)(SearchWithTabKeyNavigation);
