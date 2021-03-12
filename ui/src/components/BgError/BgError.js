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
import "./BgError.css";
import showdown from "showdown";
import xssFilter from "showdown-xss-filter";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

const converter = new showdown.Converter({extensions: [xssFilter]});

export const BgError = ({ cancelAction, retryAction, onAction, show, message, retryLabel, retryStyle, cancelLabel, cancelStyle }) => {
  const onRetry = () => typeof onAction === "function" && onAction(retryAction);

  const onCancel = () => typeof onAction === "function" && onAction(cancelAction);

  if (!show) {
    return null;
  }

  const html = converter.makeHtml(message);
  return (
    <div className="kgs-bg-error">
      <FontAwesomeIcon icon="ban" className="kgs-bg-error-icon" size="5x"/><br/>
      <span className="kgs-bg-error-message" dangerouslySetInnerHTML={{__html:html}} />
      <div className="kgs-bg-error-navigation">
        {cancelLabel && (
          <button className={`${cancelStyle?cancelStyle:""}`} onClick={onCancel}>{cancelLabel}</button>
        )}
        {retryLabel && (
          <button className={`${retryStyle?retryStyle:""}`} onClick={onRetry}>{retryLabel}</button>
        )}
      </div>
    </div>
  );
};

BgError.propTypes = {
  show: PropTypes.bool,
  cancelLabel: PropTypes.string,
  cancelAction: PropTypes.oneOfType([
    PropTypes.func,
    PropTypes.object
  ]),
  retryLabel: PropTypes.string,
  retryAction: PropTypes.oneOfType([
    PropTypes.func,
    PropTypes.object
  ]),
  onAction: PropTypes.func
};

export default BgError;