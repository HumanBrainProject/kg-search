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
export class BgError extends React.PureComponent {
  onRetry = () => {
    const { retryAction, onAction } = this.props;
    typeof onAction === "function" && onAction(retryAction);
  };

  onCancel = () => {
    const { cancelAction, onAction } = this.props;
    typeof onAction === "function" && onAction(cancelAction);
  };

  render() {
    const { show, message, retryLabel, retryStyle, cancelLabel, cancelStyle } = this.props;
    if (!show) {
      return null;
    }
    const converter = new showdown.Converter({extensions: [xssFilter]});
    const html = converter.makeHtml(message);
    return (
      <div className="kgs-bg-error">
        <FontAwesomeIcon icon="ban" className="kgs-bg-error-icon" size="5x"/><br/>
        <span className="kgs-bg-error-message" dangerouslySetInnerHTML={{__html:html}} />
        <div className="kgs-bg-error-navigation">
          {cancelLabel && (
            <button className={`${cancelStyle?cancelStyle:""}`} onClick={this.onCancel}>{cancelLabel}</button>
          )}
          {retryLabel && (
            <button className={`${retryStyle?retryStyle:""}`} onClick={this.onRetry}>{retryLabel}</button>
          )}
        </div>
      </div>
    );
  }
}

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