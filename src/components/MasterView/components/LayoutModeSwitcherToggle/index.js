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
import "./styles.css";

function LayoutModeSwitcherToggleItem(props) {
  const handleClick = () => {
    props.onClick(props.value);
  }
  return (
    <button className={"sk-toggle-option sk-toggle__item" + (props.isActive? " is-active":"")}  disabled={props.isActive} onClick={handleClick} >
      <label className="sk-toggle-option__text">{props.label}</label>
    </button>
  );
}

export function LayoutModeSwitcherToggle(props) {
  const handleClick = (layoutMode) => {
    props.onToggle(layoutMode === "grid");
  };

  return (
    <div className="kgs-layout-mode-switcher-toggle">
      <div className="sk-toggle">
        <LayoutModeSwitcherToggleItem label="Grid" value="grid" onClick={handleClick} isActive={props.gridLayoutMode} />
        <LayoutModeSwitcherToggleItem label="List" value="list" onClick={handleClick} isActive={!props.gridLayoutMode} />
      </div>
    </div>
  );
}