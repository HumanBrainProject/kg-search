/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

import { history } from "../../store";
import { getTags, getTitle } from "../../helpers/InstanceHelper";
import { ShareButtons } from "../Share/ShareButtons";
import { Instance } from "../../components/Instance/Instance";
import { Tags } from "../../components/Tags/Tags";
import { DefinitionErrorPanel, GroupErrorPanel, InstanceErrorPanel } from "../Error/ErrorPanel";
import { getUpdatedQuery, getLocationFromQuery } from "../../helpers/BrowserHelpers";

import "./InstanceContainer.css";

const BackLinkButton = () => {

  const onClick = () => history.goBack();

  const title = history.location.state && history.location.state.title;
  if (!title) {
    return null;
  }
  return (
    <button className="kgs-container__backButton" onClick={onClick}><FontAwesomeIcon icon="chevron-left" />&nbsp;{history.location.state.title}</button>
  );

};

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
    this.initialize(group);
  }

  componentDidUpdate(previousProps) {
    const { definitionIsReady, definitionHasError, isGroupsReady, groupsHasError, group, instanceHasError, id } = this.props;
    this.setTitle();
    this.updateLocation(previousProps);
    if (definitionIsReady !== previousProps.definitionIsReady || definitionHasError !== previousProps.definitionHasError ||
       groupsHasError !== previousProps.groupsHasError || isGroupsReady !== previousProps.isGroupsReady || previousProps.group !== group ||
       previousProps.instanceHasError !== instanceHasError ||
       previousProps.id !== id) {
      this.initialize(previousProps.group);
    }
  }

  setTitle() {
    const { id, currentInstance } = this.props;
    document.title = `EBRAINS - ${getTitle(currentInstance, id)}`;
  }

  updateLocation = (previousProps) => {
    const { group, defaultGroup, location } = this.props;
    const shouldUpdateGroup = group !== previousProps.group;

    if (shouldUpdateGroup) {
      const query = getUpdatedQuery(location.query, "group", group && group !== defaultGroup, group, false);
      const url = getLocationFromQuery(query, location);
      history.push(url);
    }
  }

  initialize(previousGroup) {
    const {
      definitionIsReady, definitionHasError, definitionIsLoading,
      isGroupsReady, isGroupLoading, shouldLoadGroups, groupsHasError,
      instanceIsLoading, id, group, previousInstance, setPreviousInstance,
      loadDefinition, loadGroups, fetch, instanceHasError
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
      if(!instanceIsLoading) {
        if (previousInstance &&
            previousInstance._id === id &&
            previousGroup === group) {
          setPreviousInstance();
        } else {
          if(!instanceHasError) {
            fetch(group, id);
          }
        }
      }
    }
  }

  render() {
    const { showInstance, instanceProps, watermark, fetch } = this.props;
    const NavigationComponent = getNavigation(instanceProps && instanceProps.header);
    return (
      <React.Fragment>
        <div className="kgs-instance-container" >
          {showInstance && (
            <React.Fragment>
              <BackLinkButton />
              <Instance {...this.props.instanceProps} NavigationComponent={NavigationComponent} fetch={fetch} />
            </React.Fragment>
          )}
          {watermark && (
            <div className="kgs-instance-editor__watermark">
              <p>{watermark}</p>
            </div>
          )}
        </div>
        <DefinitionErrorPanel />
        <GroupErrorPanel />
        <InstanceErrorPanel />
      </React.Fragment>
    );
  }

}
