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

import React from "react";
import { connect } from "react-redux";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {faChevronRight} from "@fortawesome/free-solid-svg-icons/faChevronRight";
import * as actionsSearch from "../../../actions/actions.search";
import { Facet } from "./Facet";

import "./FiltersPanel.css";

class FiltersPanelBase extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      collapsed: true
    };
  }

  toggleFilters = () => {
    this.setState(state => ({ collapsed: !state.collapsed }));
  };

  render() {
    const { show, facets, onChange, onViewChange, onReset } = this.props;

    if (!show) {
      return null;
    }

    const hasFilters = facets.length > 0;

    return (
      <div className="kgs-filters collapsible">
        <div className="kgs-filters__header" >
          <div className="kgs-filters__title" >
            <button type="button" className={`kgs-filters__toggle ${this.state.collapsed?"":"in"} ${hasFilters?"hasFilters":""}`} onClick={this.toggleFilters}>
              <FontAwesomeIcon icon={faChevronRight} />
            </button> Filters </div>
          <div className= "kgs-filters__reset" >
            <button type="button" className="kgs-filters__reset-button" onClick={onReset}>Reset</button>
          </div>
        </div>
        <div className={`kgs-filters__body collapse ${this.state.collapsed?"":"in"}`}>
          {
            facets.map(facet => (
              <Facet
                key={facet.name}
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
    const facets = (state.settings.isReady && Array.isArray(state.search.selectedType?.facets))?state.search.selectedType.facets:[];
    return {
      show: state.settings.isReady && facets.length > 0,
      facets: facets
    };
  },
  dispatch => ({
    onChange: (name, active, keyword) => {
      dispatch(actionsSearch.setFacet(name, active, keyword));
    },
    onViewChange: (name, size) => {
      dispatch(actionsSearch.setFacetSize(name, size));
    },
    onReset: () => {
      dispatch(actionsSearch.resetFacets());
    }
  })
)(FiltersPanelBase);