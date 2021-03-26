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
import PropTypes from "prop-types";
import "./HitsList.css";
import { HitButton } from "./HitButton";
import { HitsStack } from "./HitsStack";


const HitsListItem = ({item, itemComponent, getKey, onClick }) => {
  if (Array.isArray(item)) {
    const key = getKey(item[0]);
    return (
      <HitsStack reference={key} items={item} itemComponent={itemComponent} getKey={getKey} onClick={onClick} />
    );
  }
  const key = getKey(item);
  return (
    <HitButton reference={key} data={item} component={itemComponent} onClick={onClick} />
  );
};

export const HitsList = ({ className, title, items, itemComponent, getKey, onClick }) => {
  if (!Array.isArray(items) || items.length === 0 || typeof getKey !== "function") {
    return null;
  }
  const classNames = ["kgs-hits-list", className].join(" ");
  return (
    <div className={classNames}>
      {title && (
        <div>{title}</div>
      )}
      <ul>
        {items.map(item => {
          const key = getKey(Array.isArray(item)?items[0]:item);
          return (
            <li key={key}>
              <HitsListItem item={item} itemComponent={itemComponent} getKey={getKey} onClick={onClick} />
            </li>
          );
        })}
      </ul>
    </div>
  );
};

HitsList.propTypes = {
  className: PropTypes.string,
  title: PropTypes.string,
  items: PropTypes.arrayOf(PropTypes.any),
  itemComponent: PropTypes.oneOfType([
    PropTypes.element,
    PropTypes.func,
    PropTypes.object
  ]).isRequired,
  getKey: PropTypes.func.isRequired,
  onClick: PropTypes.func
};

export default HitsList;