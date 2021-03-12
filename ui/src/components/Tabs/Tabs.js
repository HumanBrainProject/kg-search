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
import React, { useState, useEffect } from "react";
import PropTypes from "prop-types";
import { Hint } from "../Hint/Hint";
import "./Tabs.css";

const Tab = ({tab, active, onClick}) => {
  if (!tab) {
    return null;
  }
  const handleClick = () => {
    typeof onClick === "function" && onClick(tab);
  };
  const {title, counter, hint} = tab;
  const className = `kgs-tabs-button ${active?"is-active":""}`;
  return (
    <button type="button" className={className} onClick={handleClick}>{title?title:""} {counter !== null?`(${counter})`:""} <Hint {...hint} /></button>
  );
};

export const Tabs = ({id, className, tabs, viewComponent }) => {
  const [value, setValue] = useState();

  useEffect(() => {
    setValue(Array.isArray(tabs) && tabs.length && tabs[0]);
  }, [id]);

  const handleClick = value => setValue(value);

  const classNames = ["kgs-tabs", className].join(" ");
  const Component = viewComponent;

  if (!Array.isArray(tabs) || !tabs.length) {
    return null;
  }

  return (
    <div className={classNames}>
      <div className="kgs-tabs-buttons">
        {tabs.map(tab => (
          <Tab key={tab.id} tab={tab} active={value && tab.id === value.id} onClick={handleClick} />
        ))}
      </div>
      <div className="kgs-tabs-content">
        {value && value.data && Component && (
          <Component key={value.id} {...value.data} />
        )}
      </div>
    </div>
  );
};

Tabs.propTypes = {
  className: PropTypes.string,
  id: PropTypes.string,
  tabs:  PropTypes.arrayOf(PropTypes.any),
  viewComponent: PropTypes.oneOfType([
    PropTypes.element,
    PropTypes.func
  ]).isRequired
};

export default Tabs;