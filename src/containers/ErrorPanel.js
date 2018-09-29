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

import { connect } from "react-redux";
import { ErrorPanel as  Component } from "../components/ErrorPanel";

export const ErrorPanel = connect(
  state => ({
    show: state.error && state.error.message,
    message: state.error && state.error.message,
    retryLabel: state.error && state.error.retry && state.error.retry.label,
    retryAction: state.error && state.error.retry && state.error.retry.action,
    cancelLabel: state.error && state.error.cancel && state.error.cancel.label,
    cancelAction: state.error && state.error.cancel && state.error.cancel.action,
  }),
  dispatch => ({
    onAction:  action => dispatch({type: action})
  })
)(Component);