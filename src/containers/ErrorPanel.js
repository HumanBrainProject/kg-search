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
import { BgError } from "../components/BgError";
import * as actions from "../actions/actions";
import * as actionsSearch from "../actions/actions.search";
import * as actionsGroups from "../actions/actions.groups";
import * as actionsInstances from "../actions/actions.instances";
import * as actionsDefinition from "../actions/actions.definition";

export const DefinitionErrorPanel = connect(
  state => ({
    show: !!state.definition.error,
    message: state.definition.error,
    retryLabel: "Retry",
    retryAction: actionsDefinition.clearDefinitionError(),
    retryStyle: "primary"
  }),
  dispatch => ({
    onAction:  action => dispatch(action)
  })
)(BgError);

export const GroupErrorPanel = connect(
  state => ({
    show: !!state.groups.error,
    message: state.groups.error,
    cancelLabel: "Back to search",
    cancelAction: actionsInstances.goToSearch(),
    retryLabel: "Retry",
    retryAction: actionsGroups.clearGroupError(),
    retryStyle: "primary"
  }),
  dispatch => ({
    onAction:  action => dispatch(action)
  })
)(BgError);


export const InstanceErrorPanel = connect(
  state => ({
    show: !!state.instances.error,
    message: state.instances.error,
    cancelLabel: "Back to search",
    cancelAction: actionsInstances.goToSearch(state.groups.group, state.groups.defaultGroup),
    retryLabel: "Retry",
    retryAction: actionsInstances.clearInstanceError(),
    retryStyle: "primary"
  }),
  dispatch => ({
    onAction:  action => dispatch(action)
  })
)(BgError);


export const SearchInstanceErrorPanel = connect(
  state => ({
    show: !!state.instances.error,
    message: state.instances.error,
    retryLabel: "Ok",
    retryAction: actionsInstances.clearInstanceError(),
    retryStyle: "primary"
  }),
  dispatch => ({
    onAction:  action => dispatch(action)
  })
)(Component);

export const SearchErrorPanel = connect(
  state => ({
    show: !!state.search.error,
    message: state.search.error,
    retryLabel: "Retry",
    retryAction: actionsSearch.search(),
    retryStyle: "primary"
  }),
  dispatch => ({
    onAction:  action => dispatch(action)
  })
)(BgError);

export const SessionExpiredErrorPanel = connect(
  state => ({
    show: !!state.auth.error,
    message: state.auth.error,
    retryLabel: "Login",
    retryAction: actions.authenticate(),
    retryStyle: "primary"
  }),
  dispatch => ({
    onAction:  action => dispatch(action)
  })
)(BgError);