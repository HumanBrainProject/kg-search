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
import { Text } from "../Text/Text";
import "./Details.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
export class Details extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      collapsed: true
    };
  }

  handleToggle = () => this.setState(state => ({ collapsed: !state.collapsed }));

  handleClose = () => this.setState({ collapsed: true });

  render() {
    const { toggleLabel, content, asPopup } = this.props;

    if (!content) {
      return null;
    }

    const className = `toggle ${this.state.collapsed ? "" : "in"}`;
    return (
      <span className="field-details">
        <button className={className} onClick={this.handleToggle}>
          <FontAwesomeIcon icon="exclamation-circle" />
          {toggleLabel && (
            <span>{toggleLabel}</span>
          )}
        </button>
        {!asPopup ?
          <div className="collapsible">
            {!this.state.collapsed && (
              <div className="field-details__panel">
                <Text content={content} isMarkdown={true} />
                <button className="field-details__close-button" onClick={this.handleClose} title="close">
                  <FontAwesomeIcon icon="times" size="2x" />
                </button>
              </div>
            )}
          </div>
          :
          <div className={`popup ${this.state.collapsed ? "" : "show"}`}>
            <div className="field-details__panel">
              <Text content={content} isMarkdown={true} />
              <button className="field-details__close-button" onClick={this.handleClose} title="close">
                <FontAwesomeIcon icon="times" size="2x" />
              </button>
            </div>
          </div>
        }
      </span>
    );
  }
}

export default Details;