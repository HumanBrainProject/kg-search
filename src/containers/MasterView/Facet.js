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
import "./Facet.css";

class FacetCheckbox extends React.Component {

    handleClick = e => {
      e.stopPropagation();
      this.props.onClick(!this.props.checked);
    };

    render() {
      const { label, checked, count } = this.props;
      window.console.log(label, checked, count);
      return ( <div className = { `kgs-facet-checkbox ${checked ? "is-active" : ""}` }
        onClick = { this.handleClick } >
        <input type = "checkbox" checked = { checked } />
        <div className = "kgs-facet-checkbox__text" > { label } </div>
        <div className = "kgs-facet-checkbox__count" > { count } </div>
      </div>
      );
    }
}

const FacetListItem = ({ item, onChange }) => {

  const onClick = active => onChange(item.value, active);


  return (
    <FacetCheckbox label = { item.value }
      checked = { item.checked }
      count = { item.count }
      onClick = { onClick }
    />
  );
};

const FacetList = ({ list, onChange }) => (
  <div className = "kgs-facet-list" > {
    list.map(item => ( <FacetListItem key = { item.id }
      item = { item }
      onChange = { onChange }
    />
    ))
  } </div>
);

export const Facet = ({ facet, onChange }) => {
  let Component = null;
  let parameters = null;
  switch (facet.filterType) {
  case "list":
    Component = FacetList;
    parameters = {
      list: facet.keywords.map(keyword => ({
        value: keyword.value,
        count: keyword.count,
        checked: Array.isArray(facet.value) ? facet.value.includes(keyword.value) : false
      })),
      onChange: (keyword, active) => onChange(facet.name, active, keyword)
    };
    break;
  case "exists":
    Component = FacetCheckbox;
    parameters = {
      label: facet.name,
      count: facet.count,
      checked: !!facet.value,
      onClick: active => onChange(facet.name, active)
    };
    break;
  default:
    break;
  }
  if (Component) {
    return ( <div className = "kgs-facet" >
      <div className = "kgs-facet-title" > { facet.fieldLabel } </div>
      <Component {...parameters }/>
    </div>
    );
  }
  return null;
};