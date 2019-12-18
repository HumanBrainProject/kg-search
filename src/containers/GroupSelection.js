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
import { connect } from "react-redux";

import * as actionsGroups from "../actions/actions.groups";
import { getUpdatedUrl } from "../helpers/BrowserHelpers";
import { history } from "../store";
import { Select } from "../components/Select";

class GroupSelectionBase extends React.Component {

  componentDidUpdate(prevProps) {
    const { value, defaultValue, location } = this.props;
    if (value !== prevProps.value) {
      const url = getUpdatedUrl("group", value !== defaultValue, value, false, location);
      history.push(url);
    }
  }

  render() {
    return (
      <Select {...this.props} />
    );
  }
}

export const GroupSelection = connect(
  (state, props) => ({
    className: props.className,
    label: "group",
    value: state.groups.group,
    list: state.groups.groups?state.groups.groups:[],
    defaultValue: state.groups.defaultGroup,
    location: state.router.location
  }),
  dispatch => ({
    onChange: value => {
      dispatch(actionsGroups.setGroup(value));
      dispatch(actionsGroups.resetTypeForGroup(value));
    }
  })
)(GroupSelectionBase);