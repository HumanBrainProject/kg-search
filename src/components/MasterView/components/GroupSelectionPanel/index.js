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
import { store, dispatch } from "../../../../store";
import * as actions from "../../../../actions";
import { Select } from "../../../Select";
import "./styles.css";

export class GroupSelectionPanel extends PureComponent {
  constructor(props) {
    super(props);
    this.state = this.getState();
  }
  getState() {
    const globalState = store.getState();
    return {
      group: globalState.search.index,
      groups: globalState.indexes.indexes?globalState.indexes.indexes:[]
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
    const handleGroupChange = group => {
      //window.console.debug("new group: " + group);
      dispatch(actions.setIndex(group));
    };
    if (this.state.groups.length <= 1) {
      return null;
    }
    return (
      <div className="kgs-group-selection">
        <Select label="Group" value={this.state.group} list={this.state.groups} onChange={handleGroupChange} />
      </div>
    );
  }
}