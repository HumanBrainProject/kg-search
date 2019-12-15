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
import * as actions from "../../actions";
import { Facet } from "./Facet";

import "./FiltersPanel.css";
import { getUpdatedUrlForList } from "../../helpers/BrowserHelpers";
import { history } from "../../store";

class FiltersPanelBase extends React.Component {

  componentDidUpdate(previousProps) {
    const { facets, values, location } = this.props;
    if (values !== previousProps.values) {
      const list = this.calculateFacetList(facets);
      const url = getUpdatedUrlForList(list, location);
      history.push(url);
    }
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

  render(){
    const  { className, show, facets, location, onChange, onReset } = this.props;
    if (!show) {
      return null;
    }

    const hasFilters = facets.length > 0;

    return (
      <div className = { `kgs-filters ${className ? className : ""}` } >
        <span>
          <div className = "kgs-filters__header" >
            <div className = "kgs-filters__title" > Filters </div>
            <div className = "kgs-filters__reset" >
              <button type = "button"
                className = "kgs-filters__reset-button"
                onClick = { onReset } > Reset </button>
            </div >
          </div>
          <span>
            {
              facets.map(facet => (
                <Facet
                  key = { facet.id }
                  facet = { facet }
                  onChange = { onChange }
                  location = { location }
                />
              ))
            }
          </span>
          {!hasFilters && ( <span className = "kgs-filters__no-filters" > No filters available
          for your current search. </span>
          )}
        </span>
      </div>
    );
  }
}

export const FiltersPanel = connect(
  state => {
    const facets = state.search.facets.filter(f =>
      state.search.selectedType === f.type &&
      f.count > 0 &&
      (f.filterType !== "list" || f.keywords.length)
    );
    return {
      show: state.definition.isReady && state.search.facets.length > 0,
      facets: facets,
      values: state.search.facets.reduce((acc, facet) => {
        acc += Array.isArray(facet.value) ? facet.value.toString() : facet.value;
        return acc;
      }, ""),
      location: state.router.location
    };
  },
  dispatch => ({
    onChange: (id, active, keyword) => dispatch(actions.setFacet(id, active, keyword)),
    onReset: () => dispatch(actions.resetFacets())
  })
)(FiltersPanelBase);