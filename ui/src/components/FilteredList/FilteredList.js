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
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

import { List } from "../List/List";

import "./FilteredList.css";

export class FilteredList extends React.Component {

  constructor(props) {
    super(props);
    this.state = { filter: "", hasFocus: false, selectedItems: [], options: [], focusedOption: null };
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
  };

  handleInputKeyStrokes = e => {
    if (e.keyCode === 40) {
      e.preventDefault();
      const options = this.state.options;
      const index = options.indexOf(this.state.focusedOption);
      const next = (index !== -1 && index < options.length - 1) ? options[index + 1] : options[0];
      this.setState({focusedOption: next});
    } else if (e.keyCode === 38) {
      e.preventDefault();
      const options = this.state.options;
      const index = options.indexOf(this.state.focusedOption);
      const previous = index > 0 ? options[index - 1] : options[options.length - 1];
      this.setState({focusedOption: previous});
    } else if (e.keyCode === 27 || e.keyCode === 9) {
      //escape or tab key -> we want to close the dropdown menu
      this.closeDropdown();
    }
  };

  handleChangeUserInput = e => {
    this.setState({ filter: e.target.value });
  };

  handleFocus = () => {
    this.listenClickOutHandler();
    this.setState({ hasFocus: true });
  };

  handleItemClick = item => {
    this.props.onItemClick && this.props.onItemClick(item);
    this.closeDropdown();
  };

  closeDropdown() {
    this.unlistenClickOutHandler();
    this.wrapperRef = null;
    this.setState({ filter: "", hasFocus: false });
  }

  clickOutHandler = e => {
    if (!this.wrapperRef || !this.wrapperRef.contains(e.target)) {
      this.closeDropdown();
    }
  };

  listenClickOutHandler() {
    window.addEventListener("mouseup", this.clickOutHandler, false);
    window.addEventListener("touchend", this.clickOutHandler, false);
    window.addEventListener("keyup", this.clickOutHandler, false);
  }

  unlistenClickOutHandler() {
    window.removeEventListener("mouseup", this.clickOutHandler, false);
    window.removeEventListener("touchend", this.clickOutHandler, false);
    window.removeEventListener("keyup", this.clickOutHandler, false);
  }

  componentWillUnmount() {
    this.unlistenClickOutHandler();
  }

  static getDerivedStateFromProps(nextProps, prevState) {
    const { items } = nextProps;

    const selectedItems = [];
    const options = [];
    items.forEach(e => {
      if (e.checked) {
        selectedItems.push(e);
      } else {
        options.push(e);
      }
    });

    const filter = prevState.filter.toLowerCase().trim();
    const filteredOptions = filter?options.filter(e => e.value && e.value.toLowerCase().includes(filter)):options;
    return {
      filter: prevState.filter,
      hasFocus: prevState.hasFocus,
      selectedItems: selectedItems,
      options: filteredOptions,
      focusedOption: prevState.focusedOption
    };
  }

  render() {
    const { label, ItemComponent, itemUniqKeyAttribute, onItemClick } = this.props;
    const { filter, selectedItems, options, focusedOption } = this.state;

    const dropdownOpen = this.wrapperRef && this.wrapperRef.contains(document.activeElement);

    const dropdownStyle = this.inputRef ? { top: this.inputRef.offsetHeight + "px" } : {};

    return (
      <div className="kgs-filtered-list" ref={ref => this.wrapperRef = ref}>
        <div className="kgs-filtered-list_filter" >
          <input className="kgs-filtered-list_input"
            ref={ref => this.inputRef = ref} type="text"
            onKeyDown={this.handleInputKeyStrokes}
            onChange={this.handleChangeUserInput}
            onFocus={this.handleFocus}
            value={filter}
            placeholder={(!dropdownOpen) ? "add " + label.toLowerCase() + " filters" : ""} />
          <FontAwesomeIcon icon="filter" className="kgs-filtered-facet_filter_icon" />
          <FontAwesomeIcon icon="chevron-down" className="kgs-filtered-facet_filter_dropdown_icon"/>
          <input style={{ display: "none" }} type="text" ref={ref => this.hiddenInputRef = ref} />
        </div>
        {dropdownOpen && (options.length || filter) && (
          <div className={`kgs-filtered-list_dropdown ${dropdownOpen ? "is-open" : ""}`} style={dropdownStyle} onKeyDown={this.handleInputKeyStrokes} >
            <List items={options} currentItem={focusedOption} ItemComponent={ItemComponent} itemUniqKeyAttribute={itemUniqKeyAttribute} onItemClick={this.handleItemClick} />
          </div>
        )}
        <List items={selectedItems} ItemComponent={ItemComponent} itemUniqKeyAttribute={itemUniqKeyAttribute} onItemClick={onItemClick} />
      </div>
    );
  }
}

export default FilteredList;