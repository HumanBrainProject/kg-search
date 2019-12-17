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

import { history } from "../store";
import { getTitle } from "../helpers/InstanceHelper";
import { ShareButtons } from "./ShareButtons";
import { Instance } from "../components/Instance";

import "./InstanceContainer.css";

class BackLinkButton extends React.Component {

  onClick = () => history.goBack();

  render() {
    const title = history.location.state && history.location.state.title;
    if (!title) {
      return null;
    }
    return (
      <button className="kgs-container__backButton" onClick={this.onClick}><i className="fa fa-chevron-left"></i>&nbsp;{history.location.state.title}</button>
    );
  }
}
export class InstanceContainer extends React.Component {
  componentDidMount() {
    const { setInitialGroup, location } = this.props;
    this.setTitle();
    const group = location.query.group;
    if (group) {
      setInitialGroup(group);
    }
    this.initialize();
  }

  componentDidUpdate(previousProps) {
    const { definitionIsReady, isGroupsReady, group, type, id } = this.props;
    this.setTitle();
    if (definitionIsReady !== previousProps.definitionIsReady || isGroupsReady !== previousProps.isGroupsReady || previousProps.group !== group || previousProps.type !== type || previousProps.id !== id) {
      this.initialize();
    }
  }

  setTitle() {
    const { type, id, currentInstance } = this.props;
    document.title = getTitle(currentInstance, type, id);
  }

  handleGoHome = () => {
    const { group, defaultGroup, onGoHome} = this.props;
    onGoHome(`/${(group && group !== defaultGroup)?("?group=" + group):""}`);
  }

  initialize() {
    const { definitionIsReady, definitionIsLoading, loadDefinition, isGroupsReady, isGroupLoading, shouldLoadGroups, loadGroups, instanceIsLoading, shouldLoadInstance, type, id, group, previousInstance, fetch, setPreviousInstance } = this.props;
    if (!definitionIsReady) {
      if (!definitionIsLoading) {
        loadDefinition();
      }
    } else if (shouldLoadGroups && !isGroupsReady) {
      if (!isGroupLoading) {
        loadGroups();
      }
    } else {
      if (shouldLoadInstance && !instanceIsLoading) {
        if (previousInstance && previousInstance._type === type && previousInstance._id === id) {
          setPreviousInstance();
        } else {
          fetch(type, id, group);
        }
      }
    }
  }

  render() {
    const { showInstance } = this.props;
    return (
      <div className="kgs-instance-container" >
        {showInstance && (
          <React.Fragment>
            <div className="kgs-instance-container__header">
              <div className="kgs-instance-container__left">
                <button className="kgs-container__backButton" onClick={this.handleGoHome}><i className="fa fa-chevron-left"></i><i className="fa fa-chevron-left"></i>&nbsp;Search</button>&nbsp;<BackLinkButton />
              </div>
              <ShareButtons/>
            </div>
            <Instance {...this.props.instanceProps} />
          </React.Fragment>
        )}
      </div>
    );
  }

}
