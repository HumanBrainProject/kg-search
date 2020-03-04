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
import { getTags, getTitle } from "../helpers/InstanceHelper";
import { ShareButtons } from "./ShareButtons";
import { Instance } from "../components/Instance";
import { Tags } from "../components/Tags";
import { DefinitionErrorPanel, GroupErrorPanel, InstanceErrorPanel } from "./ErrorPanel";

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

const NavigationComponent = ({tags}) => (
  <div className="kgs-instance-container__header">
    <div className="kgs-instance-container__left">
      <Tags tags={tags} />
    </div>
    <ShareButtons/>
  </div>
);

const getNavigation = header => {
  const tags = getTags(header);

  const Navigation = () => (
    <NavigationComponent tags={tags} />
  );
  Navigation.displayName = "Navigation";
  return Navigation;
};

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
    const { definitionIsReady, definitionHasError, isGroupsReady, groupsHasError, group, instanceHasError, type, id } = this.props;
    this.setTitle();
    if (definitionIsReady !== previousProps.definitionIsReady || definitionHasError !== previousProps.definitionHasError ||
       groupsHasError !== previousProps.groupsHasError || isGroupsReady !== previousProps.isGroupsReady || previousProps.group !== group ||
       previousProps.instanceHasError !== instanceHasError ||
       previousProps.type !== type || previousProps.id !== id) {
      this.initialize();
    }
  }

  setTitle() {
    const { type, id, currentInstance } = this.props;
    document.title = getTitle(currentInstance, type, id);
  }

  initialize() {
    const {
      definitionIsReady, definitionHasError, definitionIsLoading,
      isGroupsReady, isGroupLoading, shouldLoadGroups, groupsHasError,
      instanceIsLoading, shouldLoadInstance, instanceHasError,
      type, id, group, previousInstance, setPreviousInstance,
      loadDefinition, loadGroups, fetch
    } = this.props;

    if (!definitionIsReady) {
      if (!definitionIsLoading && !definitionHasError) {
        loadDefinition();
      }
    } else if (shouldLoadGroups && !isGroupsReady) {
      if (!isGroupLoading && !groupsHasError) {
        loadGroups();
      }
    } else {
      if (shouldLoadInstance && !instanceIsLoading && !instanceHasError) {
        if (previousInstance && previousInstance._type === type && previousInstance._id === id) {
          setPreviousInstance();
        } else {
          fetch(type, id, group);
        }
      }
    }
  }

  render() {
    const { showInstance, instanceProps } = this.props;
    const NavigationComponent = getNavigation(instanceProps && instanceProps.header);
    return (
      <React.Fragment>
        <div className="kgs-instance-container" >
          {showInstance && (
            <React.Fragment>
              <BackLinkButton />
              <Instance {...this.props.instanceProps} NavigationComponent={NavigationComponent} />
            </React.Fragment>
          )}
        </div>
        <DefinitionErrorPanel />
        <GroupErrorPanel />
        <InstanceErrorPanel />
      </React.Fragment>
    );
  }

}
