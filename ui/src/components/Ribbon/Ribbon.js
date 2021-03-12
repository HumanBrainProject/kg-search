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
import "./Ribbon.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

export const Ribbon = ({className, icon, text, counter, suffix}) => {
  return (
    <div className={`ribbon-container ${icon?"ribbon-with-icon":""} ${text?"ribbon-with-text":""}  ${className?className:""}`}>
      <div className="ribbon-inner-container">
        <div className="ribbon-inner">
          <div className="ribbon-inner-content">
            {!!icon && (
              <FontAwesomeIcon icon={icon} />
            )}
            {!!text && (
              <div>{text}</div>
            )}
            <div className="ribbon-inner-content-framed">{counter} {suffix}</div>
          </div>
        </div>
        <div className="skew r"></div>
      </div>
    </div>
  );
};

Ribbon.propTypes = {
  className: PropTypes.string,
  icon: PropTypes.string,
  text: PropTypes.string,
  counter: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number
  ]),
  suffix: PropTypes.string
};

export default Ribbon;