/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

import React from "react";
import PropTypes from "prop-types";
import showdown from "showdown";
/*import FilterXSS from 'xss';*/
import xssFilter from "showdown-xss-filter";
import "./Notice.css";

const converter = new showdown.Converter({extensions: [xssFilter]});

export const Notice = ({className, show, text, agreeLabel="I agree", onAgree}) => {
  if (!show || !text) {
    return null;
  }

  const html = converter.makeHtml(text);
  const classNames = ["kgs-notice", className].join(" ");
  return (
    <div className={classNames}>
      <div className="kgs-notice-panel">
        <span className="kgs-notice-content" dangerouslySetInnerHTML={{__html:html}} />
      </div>
      {onAgree && (
        <button className="btn btn-primary kgs-notice-agree-button" onClick={onAgree}>{agreeLabel}</button>
      )}
    </div>
  );
};

Notice.propTypes = {
  className: PropTypes.string,
  show: PropTypes.bool,
  text: PropTypes.string,
  agreeLabel: PropTypes.string,
  onAgree: PropTypes.func
};

export default Notice;