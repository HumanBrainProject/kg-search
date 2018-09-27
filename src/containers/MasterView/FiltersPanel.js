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
import { SearchkitComponent } from "searchkit";
import { connect } from "../../store";
import { Facet } from "./Facet";
import { FiltersPanel as Component } from "../../components/FiltersPanel";

const FiltersPanelContainer = connect(
  (state, props) => {
    const { searchkit } = props;
    const facetFields = state.definition.facetFields;
    const facets = [];
    const selectedType = searchkit && searchkit.state && searchkit.state.facet_type && searchkit.state.facet_type.length > 0 ? searchkit.state.facet_type[0]: "";
    Object.entries(facetFields).forEach(([type, typedFacets]) => {
      const isMatchingSelectedType = selectedType === type;
      Object.entries(typedFacets).forEach(([name, facet])=>{
        facets.push({
          id: "facet_" + type + "_" + name,
          name: name,
          facet: facet,
          isVisible: isMatchingSelectedType
        });
      });
    });
    const hasFilters = facets.some(f => f.isVisible);
    return {
      type: selectedType,
      show: state.definition.isReady && facets.length > 0,
      hasFilters: hasFilters,
      facets: facets,
      facetComponent: Facet,
      onReset: props.onReset
    };
  }
)(Component);

export class FiltersPanel extends SearchkitComponent {
  onReset = () => {
    const allFilters = this.searchkit.query && this.searchkit.query.getSelectedFilters();
    const filters = allFilters?
      allFilters.filter(filter => filter.id !== "facet_type")
      :
      [];
    const accessorsIds = Object.values(filters.reduce((res, filter) => {
      res[filter.id] = filter.id;
      return res;
    }, {}));
    accessorsIds
      .forEach(id => {
        const accessor = this.searchkit.accessors.statefulAccessors[id];
        accessor && accessor.resetState();
      });
    filters.forEach(filter => filter.remove());
    this.searchkit.reloadSearch();
  }
  render() {
    return (
      <FiltersPanelContainer searchkit={this.searchkit} onReset={this.onReset} {...this.props} />
    );
  }
}