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
import xssFilter from "showdown-xss-filter";

const converter = new showdown.Converter({extensions: [xssFilter]});

import "./ErrorPanel.css";

export const ErrorPanel = ({ message, onCancelClick, onRetryClick, cancelVariant, retryVariant, cancelLabel="Cancel", retryLabel="Retry" }) => {
  const html = converter.makeHtml(message);
  return (
    <div className="kgs-error-container">
      <div className="kgs-error-panel">
        <span className="kgs-error-message" dangerouslySetInnerHTML={{__html:html}} />
        {(typeof onCancelClick === "function" || typeof onRetryClick === "function") && (
          <div className="kgs-error-navigation">
            {typeof onCancelClick === "function" && (
              <button className={`${cancelVariant?cancelVariant:""}`} onClick={onCancelClick}>{cancelLabel}</button>
            )}
            {typeof onRetryClick === "function" && (
              <button className={`${retryVariant?retryVariant:""}`} onClick={onRetryClick}>{retryLabel}</button>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

ErrorPanel.propTypes = {
  show: PropTypes.bool,
  cancelLabel: PropTypes.string,
  onCancelClick: PropTypes.oneOfType([
    PropTypes.func,
    PropTypes.object
  ]),
  retryLabel: PropTypes.string,
  onRetryClick: PropTypes.oneOfType([
    PropTypes.func,
    PropTypes.object
  ]),
  onAction: PropTypes.func
};

export default ErrorPanel;