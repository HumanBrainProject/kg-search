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
import { history } from "../../store";

import "./Facet.css";

class FacetCheckbox extends React.Component {

  componentDidUpdate(previousProps) {
    const { name, checked, value, many, loc } = this.props;
    const val = encodeURIComponent(value);
    if (checked !== previousProps.checked) {
      let found = false;
      let counts = 0;
      const queryString = Object.entries(loc.query).reduce((acc, [key, v]) => {
        const m = key.match(/^([^[]+)\[(\d+)\]$/); // name[number]
        const [, n, k] = m?m:[null, key, null];
        const current = n === name && (!many || v === val );
        found = found || current;
        if (!current || checked) {
          const appendix = many?(n === name?`[${counts}]`:`[${k}]`):"";
          acc += `${(acc.length > 1)?"&":""}${n}${appendix}=${v}`;
          if (many && n === name) {
            counts++;
          }
        }
        return acc;
      }, "?");

      let addParam = "";
      if (!found && checked) {
        const appendix = many?`[${counts}]`:"";
        addParam = `${(queryString.length > 1)?"&":""}${name}${appendix}=${val}`;
      }
      const url = `${loc.pathname}${queryString}${addParam}`;
      history.push(url);
    }
  }

  handleClick = e => {
    e.stopPropagation();
    this.props.onClick(!this.props.checked);
  };

  render() {
    const { label, checked, count } = this.props;
    //window.console.log(label, checked, count);
    return ( <div className = { `kgs-facet-checkbox ${checked ? "is-active" : ""}` }
      onClick = { this.handleClick } >
      <input type = "checkbox" checked = { checked } />
      <div className = "kgs-facet-checkbox__text" > { label } </div>
      <div className = "kgs-facet-checkbox__count" > { count } </div>
    </div>
    );
  }
}

const FacetListItem = ({ item, loc, onChange }) => {

  const onClick = active => onChange(item.value, active);


  return (
    <FacetCheckbox
      name = { item.name }
      label = { item.value }
      checked = { item.checked }
      count = { item.count }
      value = { item.value }
      many = { true }
      loc = { loc }
      onClick = { onClick }
    />
  );
};

const FacetList = ({ list, loc, onChange }) => (
  <div className = "kgs-facet-list" > {
    list.map(item => (
      <FacetListItem
        key = { item.value }
        item = { item }
        onChange = { onChange }
        loc = { loc }
      />
    ))
  } </div>
);

export const Facet = ({ facet, loc, onChange }) => {
  let Component = null;
  let parameters = null;
  switch (facet.filterType) {
  case "list":
    Component = FacetList;
    parameters = {
      list: facet.keywords.map(keyword => ({
        name: facet.id,
        value: keyword.value,
        count: keyword.count,
        checked: Array.isArray(facet.value) ? facet.value.includes(keyword.value) : false
      })),
      loc: loc,
      onChange: (keyword, active) => onChange(facet.name, active, keyword)
    };
    break;
  case "exists":
    Component = FacetCheckbox;
    parameters = {
      name: facet.id,
      label: facet.label,
      count: facet.count,
      value: !!facet.value,
      checked: !!facet.value,
      many: false,
      loc: loc,
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