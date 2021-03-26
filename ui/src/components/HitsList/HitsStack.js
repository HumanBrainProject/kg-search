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

import React, { useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

import "./HitsStack.css";
import { HitButton } from "./HitButton";

export const HitsStack = ({ items, itemComponent, getKey, onClick }) => {

  const [expanded, setExpanded] = useState(false);

  if (!Array.isArray(items) || !items.length) {
    return null;
  }

  const handleExpandToggle = () => setExpanded(true);

  const item = items[0];
  const key = getKey(item);

  if (expanded) {
    return (
      <div className="kgs-hits-stack">
        <ul>
          {items.map(item => {
            const key = getKey(item);
            return (
              <li key={key}>
                <HitButton reference={key} data={item} component={itemComponent} onClick={onClick} />
              </li>
            );
          })}
        </ul>
      </div>
    );
  }

  return (
    <div className="kgs-hits-stack" size={items.length>5?5:items.length}>
      <div className="kgs-hits-stack__panel">
        <HitButton reference={key} data={item} component={itemComponent} onClick={onClick} />
      </div>
      <button className="kgs-hits-stack__expand-button" onClick={handleExpandToggle} title={items.length>2?`show the ${items.length-1} other versions `:"show the other version"}>
        <FontAwesomeIcon icon="angle-double-down" />
      </button>
    </div>
  );
};

export default HitsStack;