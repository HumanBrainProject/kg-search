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

import React, { PureComponent } from "react";
import { store, dispatch } from "../../../../store";
import * as actions from "../../../../actions";
import "./styles.css";

const LayoutModeSwitcherToggleItemComponent = props => {
  const handleClick = () => {
    props.onClick(props.value);
  };
  return (
    <button className={"sk-toggle-option sk-toggle__item" + (props.isActive? " is-active":"")}  disabled={props.isActive} onClick={handleClick} >
      <label className="sk-toggle-option__text">{props.label}</label>
    </button>
  );
};

const LayoutModeSwitcherToggleComponent = props => {
  const handleClick = (layoutMode) => {
    props.onToggle(layoutMode === "grid");
  };
  return (
    <div className="kgs-layout-mode-switcher-toggle">
      <div className="sk-toggle">
        <LayoutModeSwitcherToggleItemComponent label="Grid" value="grid" onClick={handleClick} isActive={props.gridLayoutMode} />
        <LayoutModeSwitcherToggleItemComponent label="List" value="list" onClick={handleClick} isActive={!props.gridLayoutMode} />
      </div>
    </div>
  );
};

export class LayoutModeSwitcherToggle extends PureComponent {
  constructor(props) {
    super(props);
    this.state = this.getState();
  }
  getState() {
    const globalState = store.getState();
    return {
      gridLayoutMode: globalState.application.gridLayoutMode,
      show: globalState.search.results && globalState.search.results.hits && globalState.search.results.hits.total > 0
    };
  }
  handleStateChange() {
    setTimeout(() => {
      const nextState = this.getState();
      this.setState(nextState);
    });
  }
  componentDidMount() {
    document.addEventListener("state", this.handleStateChange.bind(this), false);
    this.handleStateChange();
  }
  componentWillUnmount() {
    document.removeEventListener("state", this.handleStateChange);
  }
  render() {
    const onToggle = gridLayoutMode => {
      dispatch(actions.setLayoutMode(gridLayoutMode));
    };
    if (!this.state.show) {
      return null;
    }
    //window.console.debug("LayoutModeSwitcherToggle rendering...");
    return (
      <LayoutModeSwitcherToggleComponent gridLayoutMode={this.state.gridLayoutMode} onToggle={onToggle} />
    );
  }
}