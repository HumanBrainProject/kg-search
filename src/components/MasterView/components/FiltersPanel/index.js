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
import { SearchkitComponent, RefinementListFilter, InputFilter, CheckboxFilter, RangeFilter } from "searchkit";
import { DateRangeFilter, DateRangeCalendar } from "searchkit-datefilter";
import { connect } from "../../../../store";
import "./styles.css";

const Facet = ({id, name, facet}) => {
  if (facet.filterType === "list") {
    const orderKey = facet.filterOrder && facet.filterOrder === "byvalue"? "_term": "_count";
    const operator = facet.exclusiveSelection === false?"OR":"AND";
    const orderDirection = orderKey === "_term"? "asc": "desc";
    if (facet.isChild) {
      return (
        <RefinementListFilter
          id={id}
          field={name+".value.keyword"}
          title={facet.fieldLabel}
          operator={operator}
          size={10}
          orderKey={orderKey}
          orderDirection={orderDirection}
          fieldOptions={{type:"nested", options:{path:facet.path}}} />
      );
    }
    return (
      <RefinementListFilter
        id={id}
        field={name+".value.keyword"}
        title={facet.fieldLabel}
        operator={operator}
        size={10}
        orderKey={orderKey}
        orderDirection={orderDirection}/>
    );
  }
  if (facet.filterType === "input") {
    return (
      <InputFilter
        id={id}
        title={facet.fieldLabel}
        placeholder={"Search "+facet.fieldLabel}
        searchOnChange={true}
        queryFields={[name+".value.keyword"]} />
    );
  }
  if (facet.filterType === "exists"){
    return (
      <CheckboxFilter
        id={id}
        title={facet.fieldLabel}
        label={"Has "+facet.fieldLabel.toLowerCase()}
        filter={{exists:{field:name+".value.keyword"}}} />
    );
  }
  if(facet.filterType === "range"){
    if(facet.fieldType === "date") {
      return (
        <DateRangeFilter
          id={id}
          fromDateField={name+".value"}
          toDateField={name+".value"}
          calendarComponent={DateRangeCalendar}
          title={facet.fieldLabel} />
      );
    }
    return (
      <RangeFilter
        id={id}
        field={name+".value"}
        min={0}
        max={200}
        showHistogram={true}
        title={facet.fieldLabel} />
    );
  }
  return null;
};

class ResetFiltersButton extends SearchkitComponent {
  render() {
    const handleClick = () => {
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
    };
    return (
      <button type="button" className="kgs-filters__reset-button" onClick={handleClick}>Reset</button>
    );
  }
}

const FiltersPanelComponent = ({show, hasFilters, facets}) => {
  if (!show) {
    return null;
  }
  return (
    <div className="kgs-filters">
      <span>
        <div className="kgs-filters__header">
          <div className="kgs-filters__title">Filters</div>
          {hasFilters && (
            <div className="kgs-filters__reset"><ResetFiltersButton/></div>
          )}
        </div>
        <span>
          {facets.map(f => (
            <div className={f.isVisible?null:"hidden"} key={f.id}>
              <Facet id={f.id} name={f.name} facet={f.facet} />
            </div>
          ))}
        </span>
        {!hasFilters && (
          <span className="kgs-filters__no-filters">No filters available for your current search.</span>
        )}
      </span>
    </div>
  );
};

const mapStateToProps = (state, props) => {
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
    facets: facets
  };
};

const FiltersPanelContainer = connect(
  mapStateToProps
)(FiltersPanelComponent);

export class FiltersPanel extends SearchkitComponent {
  render() {
    return (
      <FiltersPanelContainer searchkit={this.searchkit} {...this.props} />
    );
  }
}