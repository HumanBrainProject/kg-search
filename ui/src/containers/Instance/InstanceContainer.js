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

import React, { useEffect } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

import { getTags, getTitle } from "../../helpers/InstanceHelper";
import { ShareButtons } from "../Share/ShareButtons";
import { Instance } from "../../components/Instance/Instance";
import { Tags } from "../../components/Tags/Tags";
import { DefinitionErrorPanel, GroupErrorPanel, InstanceErrorPanel } from "../Error/ErrorPanel";
import { getUpdatedQuery, getLocationFromQuery, searchToObj } from "../../helpers/BrowserHelpers";

import "./InstanceContainer.css";
import { useLocation, useNavigate, useParams } from "react-router-dom";

const BackLinkButton = ({title}) => {
  const navigate = useNavigate();

  const onClick = () => navigate(-1);

  if (!title) {
    return null;
  }
  return (
    <button className="kgs-container__backButton" onClick={onClick}><FontAwesomeIcon icon="chevron-left" />&nbsp;{title}</button>
  );

};

const NavigationComponent = ({ tags }) => (
  <div className="kgs-instance-container__header">
    <div className="kgs-instance-container__left">
      <Tags tags={tags} />
    </div>
    <ShareButtons />
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

export const InstanceContainer = ({ definitionIsReady, definitionHasError, isGroupsReady, groupsHasError, group, instanceHasError, currentInstance, showInstance, instanceProps, watermark, fetch, defaultGroup, definitionIsLoading, loadDefinition, shouldLoadGroups, isGroupLoading, loadGroups, instanceIsLoading, previousInstance, setPreviousInstance, clearAllInstances, getId}) => {
  const location = useLocation();
  const navigate = useNavigate();

  const id = getId(useParams());

  useEffect(() => {
    if (!definitionIsReady) {
      if (!definitionIsLoading && !definitionHasError) {
        loadDefinition();
      }
    } else if (shouldLoadGroups && !isGroupsReady) {
      if (!isGroupLoading && !groupsHasError) {
        loadGroups();
      }
    } else {
      if (!instanceIsLoading) {
        if (previousInstance && previousInstance._id === id ) {
          setPreviousInstance();
        } else {
          if (!instanceHasError) {
            fetch(group, id, navigate);
          }
        }
      }
    }
  }, [definitionIsReady, definitionHasError, groupsHasError, isGroupsReady, group, instanceHasError, id]);


  useEffect(() => {
    document.title = `EBRAINS - ${getTitle(currentInstance, id)}`;
  }, [id, currentInstance]);

  useEffect(() => {
    return () => {
      clearAllInstances();
    };
  }, []);

  useEffect(() => {
    const query = getUpdatedQuery(searchToObj(), "group", group && group !== defaultGroup, group, false);
    const newUrl = getLocationFromQuery(query, location);
    if (newUrl) {
      navigate(newUrl);
    }
  }, [group]);

  const NavigationComponent = getNavigation(instanceProps && instanceProps.header);

  const backTitle = previousInstance?getTitle(previousInstance):null;

  return (
    <>
      <div className="kgs-instance-container" >
        {showInstance && (
          <React.Fragment>
            <BackLinkButton title={backTitle} />
            <Instance {...instanceProps} NavigationComponent={NavigationComponent} fetch={fetch} />
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
    </>
  );
};
