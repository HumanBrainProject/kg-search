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

import React, { PureComponent } from "react";
import { store, dispatch } from "../../store";
import "./styles.css";

export class ErrorPanel extends PureComponent {
  constructor(props) {
    super(props);
    this.state = this.getState();
  }
  getState() {
    const globalState = store.getState();
    return {
      show: globalState.error && globalState.error.message,
      message: globalState.error && globalState.error.message,
      retryLabel: globalState.error && globalState.error.retry && globalState.error.retry.label,
      retryAction: globalState.error && globalState.error.retry && globalState.error.retry.action,
      cancelLabel: globalState.error && globalState.error.cancel && globalState.error.cancel.label,
      cancelAction: globalState.error && globalState.error.cancel && globalState.error.cancel.action,
    };
  }
  handleStateChange() {
    setTimeout(() => {
      const nextState = this.getState();
      this.setState(nextState);
    });
  }
  componentDidMount() {
    document.addEventListener("state", this.handleStateChange.bind(this), false);
    this.handleStateChange();
  }
  componentWillUnmount() {
    document.removeEventListener("state", this.handleStateChange);
  }
  render() {
    if (!this.state.show) {
      return null;
    }
    const onRetry = () => {
      dispatch({
        type: this.state.retryAction
      });
    };
    const onCancel = () => {
      dispatch({
        type: this.state.cancelAction
      });
    };
    //window.console.debug("ErrorPanel rendering...");
    return (
      <div className="kgs-error-container">
        <div className="kgs-error-panel">
          <span className="kgs-error-message">{this.state.message}</span>
          <div className="kgs-error-navigation">
            <button onClick={onRetry} data-show={!!this.state.retryLabel}>{this.state.retryLabel}</button>
            <button onClick={onCancel} data-show={!!this.state.cancelLabel}>{this.state.cancelLabel}</button>
          </div>
        </div>
      </div>
    );
  }
}