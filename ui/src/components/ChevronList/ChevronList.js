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
import "./ChevronList.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

const ListItem = ({ reference, data, itemComponent, onClick }) => {
  const handleClick = (event) => {
    onClick(data, event.currentTarget);
  };

  const Component = itemComponent;
  return (
    <li>
      <button role="link" onClick={handleClick} data-reference={reference}>
        <Component data={data} />
        <span className="kgs-chevron-list__chevron"><FontAwesomeIcon icon="chevron-right" /></span>
      </button>
    </li>
  );
};

export const ChevronList = ({ className, title, items, itemComponent, getKey, onClick }) => {
  if (!Array.isArray(items) || items.length === 0 || typeof getKey !== "function") {
    return null;
  }
  const classNames = ["kgs-chevron-list", className].join(" ");
  return (
    <div className={classNames}>
      {title && (
        <div>{title}</div>
      )}
      <ul>
        {items.map(item => {
          const key = getKey(item);
          return (
            <ListItem key={key} reference={key} data={item} itemComponent={itemComponent} onClick={onClick} />
          );
        })}
      </ul>
    </div>
  );
};

ChevronList.propTypes = {
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

export default ChevronList;