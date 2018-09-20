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
import { connect } from "../../../../store";
import * as actions from "../../../../actions";
import "./styles.css";

const LayoutModeSwitcherToggleItemComponent = ({label, value, isActive, onClick}) => {
  const handleClick = () => {
    onClick(value);
  };
  return (
    <button type="button" className={(isActive?"is-active":"")}  disabled={isActive} onClick={handleClick} >
      <label>{label}</label>
    </button>
  );
};

const LayoutModeSwitcherToggleComponent = ({show, gridLayoutMode, onClick}) => {
  if (!show) {
    return null;
  }

  return (
    <div className="kgs-layout-mode-switcher-toggle">
      <div>
        <LayoutModeSwitcherToggleItemComponent label="Grid" value="grid" onClick={onClick} isActive={gridLayoutMode} />
        <LayoutModeSwitcherToggleItemComponent label="List" value="list" onClick={onClick} isActive={!gridLayoutMode} />
      </div>
    </div>
  );
};

export const LayoutModeSwitcherToggle = connect(
  state => ({
    gridLayoutMode: state.application.gridLayoutMode,
    show: state.search.results && state.search.results.hits && state.search.results.hits.total > 0
  }),
  dispatch => ({
    onClick: layoutMode => dispatch(actions.setLayoutMode(layoutMode === "grid"))
  })
)(LayoutModeSwitcherToggleComponent);
