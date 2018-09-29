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
import "./Toggle.css";

const ToggleItem = ({label, value, isActive, onClick}) => {
  const handleClick = () => {
    onClick(value);
  };
  return (
    <button type="button" className={(isActive?"is-active":"")}  disabled={isActive} onClick={handleClick} >
      <label>{label}</label>
    </button>
  );
};

export const Toggle = ({className, show, value, items, onClick}) => {
  if (!show) {
    return null;
  }
  const classNames = ["kgs-toggle", className].join(" ");
  return (
    <div className={classNames}>
      <div>
        {items.map(item =>
          <ToggleItem key={item.value} label={item.label} value={item.value} isActive={item.value === value} onClick={onClick} />
        )}
      </div>
    </div>
  );
};