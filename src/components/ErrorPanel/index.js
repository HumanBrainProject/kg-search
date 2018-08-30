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
import { dispatch } from "../../store";
import { withStoreStateSubscription} from "../withStoreStateSubscription";
import "./styles.css";

const ErrorPanelComponent = ({show, message, retryLabel, retryAction, cancelLabel, cancelAction}) => {
  if (!show) {
    return null;
  }
  const onRetry = () => {
    dispatch({
      type: retryAction
    });
  };
  const onCancel = () => {
    dispatch({
      type: cancelAction
    });
  };
  //window.console.debug("ErrorPanel rendering...");
  return (
    <div className="kgs-error-container">
      <div className="kgs-error-panel">
        <span className="kgs-error-message">{message}</span>
        <div className="kgs-error-navigation">
          <button onClick={onRetry} data-show={!!retryLabel}>{retryLabel}</button>
          <button onClick={onCancel} data-show={!!cancelLabel}>{cancelLabel}</button>
        </div>
      </div>
    </div>
  );
};

export const ErrorPanel = withStoreStateSubscription(
  ErrorPanelComponent,
  data => ({
    show: data.error && data.error.message,
    message: data.error && data.error.message,
    retryLabel: data.error && data.error.retry && data.error.retry.label,
    retryAction: data.error && data.error.retry && data.error.retry.action,
    cancelLabel: data.error && data.error.cancel && data.error.cancel.label,
    cancelAction: data.error && data.error.cancel && data.error.cancel.action,
  })
);
