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
import { ShapeIcon } from "./ShapeIcon";
import { connect } from "react-redux";
import { SearchkitComponent, MenuFilter } from "searchkit";
import "./ShapesFilterPanel.css";

// {itemKey, label, count, rawCount, listDocCount, active, disabled, showCount, bemBlocks, onClick}
const ShapeFilter = ({itemKey, label, count, active, disabled, onClick}) => {
  if (itemKey === "$all") {
    return null;
  }
  return (
    <div className={`kgs-fieldsFilter__shape${active?" is-active":""}${disabled?" is-disabled":""}`}>
      <button key={itemKey} onClick={onClick} className="kgs-fieldsFilter__button" disabled={disabled || active}>
        <div>
          <div className="kgs-fieldsFilter__icon">
            <ShapeIcon label={label} shape={itemKey} active={active} />
          </div>
          <div className="kgs-fieldsFilter__label">{label}</div>
          <div className="kgs-fieldsFilter__count">{`${count?count:0} ${count && count > 1?"Results":"Result"}`}</div>
        </div>
      </button>
    </div>
  );
};
class ShapesFilterPanelBase extends SearchkitComponent {
  constructor(props) {
    super(props);
    this.initalized =  false;
  }
  componentDidUpdate() {
    if (!this.initalized) {
      this.initalized =  true;
      if (window.location.search === "") {
        const accessor = this.searchkit.accessors.statefulAccessors["facet_type"];
        if (accessor && this.props.defaultShape) {
          accessor.state = accessor.state.setValue([this.props.defaultShape]);
          this.searchkit.reloadSearch();
          //window.console.debug("set default type to Dataset");
        }
      }
    }
  }
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
      <div className="kgs-fieldsFilter">
        <MenuFilter field={"_type"} id="facet_type" itemComponent={ShapeFilter}/>
      </div>
    );
  }
}

export const ShapesFilterPanel = connect(
  state => {
    let defaultShape = null;
    if (state && state.search && state.search.group && state.groups && state.groups.groups && state.groups.groups.length && state.groups.groupSettings && state.groups.groupSettings[state.search.group]) {
      defaultShape = state.groups.groupSettings[state.search.group].facetDefaultSelectedType;
    } else {
      defaultShape = state && state.definition && state.definition.facetDefaultSelectedType;
    }
    return {
      defaultShape: defaultShape
    };
  }
)(ShapesFilterPanelBase);