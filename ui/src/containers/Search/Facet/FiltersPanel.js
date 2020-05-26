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
import * as actionsSearch from "../../../actions/actions.search";
import { Facet } from "./Facet";

import "./FiltersPanel.css";

class FiltersPanelBase extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      collapsed: true,
    };
  }

  toggleFilters = () => {
    this.setState(state => ({ collapsed: !state.collapsed }));
  }

  render() {
    const { show, facets, onChange, onViewChange, onReset } = this.props;

    if (!show) {
      return null;
    }

    const hasFilters = facets.length > 0;

    return (
      <div className="kgs-filters collapsible">
        <div className="kgs-filters__header" >
          <div className="kgs-filters__title" ><button type="button" className={`kgs-filters__toggle ${this.state.collapsed?"":"in"} ${hasFilters?"hasFilters":""}`}><i className="fa fa-chevron-right" onClick={this.toggleFilters}></i></button> Filters </div>
          <div className= "kgs-filters__reset" >
            <button type="button" className="kgs-filters__reset-button" onClick={onReset}>Reset</button>
          </div>
        </div>
        <div className={`kgs-filters__body collapse ${this.state.collapsed?"":"in"}`}>
          {
            facets.map(facet => (
              <Facet
                key={facet.id}
                facet={facet}
                onChange={onChange}
                onViewChange={onViewChange}
              />
            ))
          }
        </div>
        {!hasFilters && ( <span className="kgs-filters__no-filters" > No filters available
          for your current search. </span>
        )}
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
      facets: facets
    };
  },
  dispatch => ({
    onChange: (id, active, keyword) => dispatch(actionsSearch.setFacet(id, active, keyword)),
    onViewChange: (id, size) => dispatch(actionsSearch.setFacetSize(id, size)),
    onReset: () => dispatch(actionsSearch.resetFacets())
  })
)(FiltersPanelBase);