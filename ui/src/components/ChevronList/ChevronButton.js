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
import "./ChevronButton.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

export const ChevronButton = ({ reference, data, component, onClick }) => {
  const handleClick = (event) => {
    onClick(data, event.currentTarget);
  };

  const Component = component;
  return (
    <button role="link" className="kgs-chevron-button" onClick={handleClick} data-reference={reference}>
      <Component data={data} />
      <span className="kgs-chevron-button__chevron"><FontAwesomeIcon icon="chevron-right" /></span>
    </button>
  );
};

export default ChevronButton;