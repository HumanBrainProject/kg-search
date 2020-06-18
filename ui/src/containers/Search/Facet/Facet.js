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

  handleKeyDown = e => {
    if(e.keyCode === 13) {
      this.handleClick(e);
    } else {
      this.props.onKeyDown && this.props.onKeyDown(e);
    }
  }

  render() {
    const { label, checked, count, hidden } = this.props;
    const inputProps = {};

    if (hidden) {
      inputProps.tabIndex = -1;
    }

    return (
      <div className={`kgs-facet-checkbox ${checked?"is-active":""}`} tabIndex="-1" onClick={this.handleClick}  onKeyDown={this.handleKeyDown} >
        <input type="checkbox" defaultChecked={checked} {...inputProps} />
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
        label = { item.label }
        checked = { item.checked }
        count = { item.count }
        value = { item.value }
        many = { true }
        onClick = { this.onClick }
        onKeyDown = { this.props.onKeyDown }
        hidden  = { this.props.hidden }
      />
    );
  }
}

const FacetList = ({ list, onChange, onKeyDown, onViewChange, viewText, hidden }) => (
  <div className="kgs-facet-list">
    {list.map(item => (
      <FacetListItem
        key = { item.value }
        item = { item }
        onChange = { onChange }
        onKeyDown = { onKeyDown }
        hidden  = { hidden }
      />
    ))}
    {onViewChange && (
      <button className="kgs-facet-viewMore-button" onClick={onViewChange}>{viewText}</button>
    )}
  </div>
);

class FilteredFacetList extends React.PureComponent {

  constructor(props) {
    super(props);
    this.state = { filter: "", hasFocus: false };
  }

  //The only way to trigger an onChange event in React is to do the following
  //Basically changing the field value, bypassing the react setter and dispatching an "input"
  // event on a proper html input node
  //See for example the discussion here : https://stackoverflow.com/a/46012210/9429503
  triggerOnChange = () => {
    Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value").set
      .call(this.hiddenInputRef, JSON.stringify(this.state.filter));
    const event = new Event("input", { bubbles: true });
    this.hiddenInputRef.dispatchEvent(event);
  }

  handleInputKeyStrokes = e => {
    if(e.keyCode === 40){
      e.preventDefault();
      const allOptions = this.optionsRef.querySelectorAll(".kgs-facet-checkbox");
      if(allOptions.length > 0){
        allOptions[0].focus();
      }
    } else if(e.keyCode === 38){
      e.preventDefault();
      const allOptions = this.optionsRef.querySelectorAll(".kgs-facet-checkbox");
      if(allOptions.length > 0){
        allOptions[allOptions.length-1].focus();
      }
    } else if(e.keyCode === 27 || e.keyCode === 9) {
      //escape or tab key -> we want to close the dropdown menu
      this.closeDropdown();
    }
  }

  handleOptionKeyStrokes = e => {
    if (e.keyCode === 40){
      e.preventDefault();
      if (e.target.nextSibling) {
        e.target.nextSibling.focus();
      } else {
        const allOptions = this.optionsRef.querySelectorAll(".kgs-facet-checkbox");
        if (allOptions.length > 0) {
          allOptions[0].focus();
        }
      }
    } else if(e.keyCode === 38) {
      e.preventDefault();
      if (e.target.previousSibling) {
        e.target.previousSibling.focus();
      } else {
        const allOptions = this.optionsRef.querySelectorAll(".kgs-facet-checkbox");
        if (allOptions.length > 0) {
          allOptions[allOptions.length-1].focus();
        }
      }
    } else if (e.keyCode === 27 || e.keyCode === 9) {
      //escape or tab key -> we want to close the dropdown menu
      this.closeDropdown();
    }
  }

  handleChangeUserInput = e => {
    this.setState({ filter: e.target.value });
  }

  handleFocus = () => {
    this.listenClickOutHandler();
    this.setState({ hasFocus: true});
  }

  onSelect = (keyword, active) => {
    this.props.onChange && this.props.onChange(keyword, active);
    this.closeDropdown();
  }

  closeDropdown(){
    this.unlistenClickOutHandler();
    this.wrapperRef = null;
    this.setState({ filter: "", hasFocus: false});
  }

  clickOutHandler = e => {
    if(!this.wrapperRef || !this.wrapperRef.contains(e.target)){
      this.closeDropdown();
    }
  }

  listenClickOutHandler(){
    window.addEventListener("mouseup", this.clickOutHandler, false);
    window.addEventListener("touchend", this.clickOutHandler, false);
    window.addEventListener("keyup", this.clickOutHandler, false);
  }

  unlistenClickOutHandler(){
    window.removeEventListener("mouseup", this.clickOutHandler, false);
    window.removeEventListener("touchend", this.clickOutHandler, false);
    window.removeEventListener("keyup", this.clickOutHandler, false);
  }

  componentWillUnmount(){
    this.unlistenClickOutHandler();
  }

  render() {
    const { list, onChange } = this.props;

    const dropdownOpen = this.wrapperRef && this.wrapperRef.contains(document.activeElement);

    let activeList = [];
    let optionsList = [];
    list.forEach(e => {
      if (e.checked) {
        activeList.push(e);
      } else if (dropdownOpen) {
        optionsList.push(e);
      }
    });

    let filteredOptionsList = [];
    if (dropdownOpen) {
      if (this.state.filter) {
        const filter = this.state.filter.toLowerCase().trim();
        filteredOptionsList = optionsList.filter(e => e.value && e.value.toLowerCase().includes(filter));
      } else {
        filteredOptionsList = optionsList;
      }
    }

    const dropdownStyle = this.inputRef?{top: this.inputRef.offsetHeight + "px"}:{};

    return (
      <div className="kgs-filtered-facet" ref={ref=>this.wrapperRef = ref}>
        <div className="kgs-filtered-facet_filter" >
          <input className="kgs-filtered-facet_input"
            ref={ref=>this.inputRef = ref} type="text"
            onKeyDown={this.handleInputKeyStrokes}
            onChange={this.handleChangeUserInput}
            onFocus={this.handleFocus}
            value={this.state.filter } />
          <input style={{display:"none"}} type="text" ref={ref=>this.hiddenInputRef = ref}/>
        </div>
        {dropdownOpen && (filteredOptionsList.length || this.state.filter) && (
          <div className={`kgs-filtered-facet_dropdown ${dropdownOpen?"is-open":""}`} style={dropdownStyle} ref={ref=>{this.optionsRef = ref;}} >
            <div className="kgs-facet-list">
              {filteredOptionsList.map(item => (
                <FacetListItem
                  key = { item.value }
                  item= { item }
                  onChange = { this.onSelect }
                  onKeyDown = { this.handleOptionKeyStrokes }
                  hidden = { true }
                />
              ))}
            </div>
          </div>
        )}
        <div className="kgs-facet-list">
          {activeList.map(item => (
            <FacetListItem
              key = { item.value }
              item = { item }
              onChange = { onChange }
            />
          ))}
        </div>
      </div>
    );
  }
}

class HierarchicalFacetListItem extends React.Component {

  constructor(props) {
    super(props);
    this.childrenRef = React.createRef();
    this.state = {isCollapsed: true};
  }

  onClick = active => {
    this.props.onChange(this.props.item.value, active);
  };

  onCollapseToggle = () => this.setState(state => ({ isCollapsed: !state.isCollapsed }));

  render() {
    const { item } = this.props;

    const maxHeight = (!this.state.isCollapsed && this.childrenRef && this.childrenRef.current)?this.childrenRef.current.scrollHeight + "px":null;

    return (
      <div className={`kgs-collapsible-facet ${this.state.isCollapsed?"is-collapsed":""} ${this.props.item.hasAnyChildChecked?"has-any-child-active":""}`}>
        <div className="kgs-collapsible-facet__header">
          <button className="kgs-collapsible-facet__button" onClick={this.onCollapseToggle} title={`${this.state.isCollapsed?"expand":"collapse"}`}><i className="fa fa-chevron-down"></i></button>
          <FacetCheckbox
            name = { item.name }
            label = { item.label }
            checked = { item.checked }
            count = { item.count }
            value = { item.value }
            many = { true }
            onClick = { this.onClick }
          />
        </div>
        <div className="kgs-collapsible-facet__children" ref={this.childrenRef} style={{maxHeight: maxHeight}}>
          <FacetList list={item.children} onChange={this.props.onChange} hidden={this.state.isCollapsed} />
        </div>
      </div>
    );
  }
}

const HierarchicalFacetList = ({ list, onChange }) => (
  <div className="kgs-facet-list">
    {list.map(item => (
      <HierarchicalFacetListItem
        key = { item.value }
        item = { item }
        onChange = { onChange }
      />
    ))}
  </div>
);

const viewMoreIncrement = 50;

export const Facet = ({ facet, onChange, onViewChange }) => {
  let Component = null;
  let parameters = null;
  switch (facet.filterType) {
  case "list":
  {
    if (facet.isHierarchical) {
      const list = facet.keywords.map(keyword => {
        let value = [];
        let checked = true;
        let hasAnyChildChecked = false;
        let children = (keyword.children && keyword.children.keywords)?keyword.children.keywords.map(child => {
          value.push(child.value);
          const childChecked = Array.isArray(facet.value) ? facet.value.includes(child.value) : false;
          if (!childChecked) {
            checked = false;
          } else {
            hasAnyChildChecked = true;
          }
          return {
            name: facet.id,
            label: child.value,
            value: child.value,
            count: child.count,
            checked: childChecked
          };
        }):[];
        return {
          name: facet.id,
          label: keyword.value,
          value: value,
          count: keyword.count,
          checked: checked,
          hasAnyChildChecked: hasAnyChildChecked,
          children: children
        };
      });
      const nullValueIdx = list.findIndex(e => e.label === facet.nullValuesLabel);
      if (nullValueIdx !== -1) {
        const removedItems = list.splice(nullValueIdx, 1);
        list.push(removedItems[0]);
      }
      Component = HierarchicalFacetList;
      parameters = {
        list: list,
        onChange: (keyword, active) => onChange(facet.id, active, keyword)
      };
    } else {
      const list = facet.keywords.map(keyword => ({
        name: facet.id,
        label: keyword.value,
        value: keyword.value,
        count: keyword.count,
        checked: Array.isArray(facet.value) ? facet.value.includes(keyword.value) : false,
      }));
      if (facet.isFilterable) {
        Component = FilteredFacetList;
        parameters = {
          list: list,
          onChange: (keyword, active) => onChange(facet.id, active, keyword)
        };
      } else {
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
          list: list,
          onChange: (keyword, active) => onChange(facet.id, active, keyword),
          onViewChange: onView,
          viewText: viewText
        };
      }
    }
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