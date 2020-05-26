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
  }

  render() {
    const { label, checked, count } = this.props;
    return (
      <div className={`kgs-facet-checkbox ${checked?"is-active":""}`} onClick = { this.handleClick } >
        <input type="checkbox" defaultChecked={checked} />
        <div className="kgs-facet-checkbox__text">{label}</div>
        <div className="kgs-facet-checkbox__count">{count}</div>
      </div>
    );
  }
}

class FacetListItem extends React.PureComponent {

  onClick = active => this.props.onChange(this.props.item.value, active);

  render() {
    const { item } = this.props;
    return (
      <FacetCheckbox
        name = { item.name }
        label = { item.value }
        checked = { item.checked }
        count = { item.count }
        value = { item.value }
        many = { true }
        onClick = { this.onClick }
      />
    );
  }
}

const FacetList = ({ list, onChange, onViewChange, viewText }) => (
  <div className="kgs-facet-list">
    {list.map(item => (
      <FacetListItem
        key = { item.value }
        item = { item }
        onChange = { onChange }
      />
    ))}
    {onViewChange && (
      <button className="kgs-facet-viewMore-button" onClick={onViewChange}>{viewText}</button>
    )}
  </div>
);

const viewMoreIncrement = 50;

export const Facet = ({ facet, onChange, onViewChange }) => {
  let Component = null;
  let parameters = null;
  switch (facet.filterType) {
  case "list":
  {
    let onView = null;
    let viewText = null;
    if (facet.others > 0) {
      onView = () => onViewChange(facet.id, facet.size === facet.defaultSize?viewMoreIncrement:facet.size + viewMoreIncrement);
      if (viewMoreIncrement >= facet.others) {
        viewText = "View all";
      } else {
        viewText = "View more";
      }
    } else if (facet.keywords.length > facet.defaultSize) {
      onView = () => onViewChange(facet.id, facet.defaultSize);
      viewText = "View less";
    }
    Component = FacetList;
    parameters = {
      list: facet.keywords.map(keyword => ({
        name: facet.id,
        value: keyword.value,
        count: keyword.count,
        checked: Array.isArray(facet.value) ? facet.value.includes(keyword.value) : false
      })),
      onChange: (keyword, active) => onChange(facet.id, active, keyword),
      onViewChange: onView,
      viewText: viewText
    };
    break;
  }
  case "exists":
    Component = FacetCheckbox;
    parameters = {
      name: facet.id,
      label: `Has ${facet.fieldLabel}`,
      count: facet.count,
      value: !!facet.value,
      checked: !!facet.value,
      many: false,
      onClick: active => onChange(facet.id, active)
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