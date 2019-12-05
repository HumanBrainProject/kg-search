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
import { RefinementListFilter, InputFilter, CheckboxFilter, RangeFilter } from "searchkit";
/* No date range filter: package removed for now */
/* import { DateRangeFilter, DateRangeCalendar } from "searchkit-datefilter"; */
import "./Facet.css";

class FacetCheckbox extends React.Component {

  handleClick = () => this.props.onClick(!this.props.checked);

  render() {
    const {label, checked, count } = this.props;
    return (
      <div className={`kgs-facet-checkbox ${checked?"is-active":""}`} onClick={this.handleClick}>
        <input type="checkbox" checked={checked}  />
        <div className="kgs-facet-checkbox__text sk-item-list-option__text">{label}</div>
        <div className="kgs-facet-checkbox__count sk-item-list-option__count">{count}</div>
      </div>
    );
  }
}

const FacetListItem = ({item, onClick}) => {

  const _onClick = active => onClick(item.value, active);

  return (
    <FacetCheckbox label={item.value} count={item.count} onClick={_onClick} />
  );
};

const FacetList = ({list, onClick}) => (
  <div className="kgs-facet-list">
    {list.map(item => (
      <FacetListItem key={item.id} item={item} onClick={onClick} />
    ))}
  </div>
);

const FacetPanel = ({title, component: Component, parameters }) => (
  <div className="kgs-facet-panel">
    <div className="kgs-facet-title">{title}</div>
    <Component {...parameters} />
  </div>
);

const FacetCheckboxPanel = ({name, title, label, count, onClick }) => {
  const parameters = {
    label: label,
    count: count,
    onClick: active => onClick(name, active)
  };
  return (
    <FacetPanel title={title} component={FacetCheckbox} parameters={parameters} />
  );
};

const FacetListPanel = ({name, title, list, onClick }) => {
  const parameters = {
    list: list,
    onClick: (keyword, active) => onClick(name, keyword, active)
  };
  return (
    <FacetPanel title={title} component={FacetList} parameters={parameters} />
  );
};


const SearchkitFacet = ({id, name, facet}) => {
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
    /*
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
    */
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

export const Facet = ({className, id, name, facet, visible}) => {
  const classNames = ["kgs-facet", className, visible?"":"hidden"].join(" ");
  return (
    <div className={classNames}>
      <SearchkitFacet id={id} name={name} facet={facet} />
    </div>
  );
};