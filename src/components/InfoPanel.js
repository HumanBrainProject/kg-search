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
import showdown from "showdown";
/*import FilterXSS from 'xss';*/
import xssFilter from "showdown-xss-filter";
import "./InfoPanel.css";

const converter = new showdown.Converter({extensions: [xssFilter]});

export const InfoPanel = ({text, onClose}) => {
  if (!text) {
    return null;
  }
  const html = converter.makeHtml(text);
  return (
    <div className="kgs-info">
      <div className="kgs-info__panel">
        <div className="kgs-info__container">
          <span className="kgs-info__content" dangerouslySetInnerHTML={{__html:html}} />
          <button className="kgs-info__closeButton" onClick={onClose}>
            <i className="fa fa-close" />
          </button>
        </div>
      </div>
    </div>
  );
};

InfoPanel.propTypes = {
  text: PropTypes.string,
  onClose: PropTypes.func
};

export default InfoPanel;