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
import {faChevronLeft} from "@fortawesome/free-solid-svg-icons/faChevronLeft";

import { getTags, getTitle } from "../../helpers/InstanceHelper";
import { ShareButtons } from "../Share/ShareButtons";
import { Instance } from "../../components/Instance/Instance";
import { Tags } from "../../components/Tags/Tags";
import { InstanceErrorPanel } from "../Error/ErrorPanel";
import { getUpdatedQuery, getLocationSearchFromQuery, searchToObj } from "../../helpers/BrowserHelpers";

import "./InstanceContainer.css";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import Notification from "../../components/Notification/Notification";

const BackLinkButton = ({instance}) => {

  const navigate = useNavigate();

  if (!instance) {
    return null;
  }

  const title = getTitle(instance);

  const onClick = () => navigate(-1);

  return (
    <button className="kgs-container__backButton" onClick={onClick}><FontAwesomeIcon icon={faChevronLeft} />&nbsp;{title}</button>
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

export const InstanceContainer = ({
  path,
  group,
  instanceHasError,
  currentInstance,
  showInstance,
  instanceProps,
  watermark,
  warning,
  fetch,
  defaultGroup,
  instanceIsLoading,
  previousInstance,
  clearAllInstances,
  getId,
  goBackToInstance
}) => {
  const location = useLocation();
  const navigate = useNavigate();

  const id = getId(useParams());

  useEffect(() => {
    if (!instanceIsLoading && !instanceHasError) {
      if (!currentInstance || currentInstance.id !== id || (currentInstance.group && currentInstance.group !== group)) { // live has not currentInstance.group
        if (currentInstance?.group !== group) {
          clearAllInstances();
        }
        fetch(group, id);
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, group, instanceIsLoading, instanceHasError]);

  useEffect(() => {
    window.onpopstate = () => {
      const reg = new RegExp(`^${path}(.+)$`);
      let previousGroup = searchToObj()["group"];
      if (!previousGroup) {
        previousGroup = defaultGroup;
      }
      const [, previousId] = reg.test(window.location.pathname) ? window.location.pathname.match(reg) : [null, null];
      goBackToInstance(previousGroup, previousId);
    };
    return () => {
      clearAllInstances();
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    document.title = `EBRAINS - ${getTitle(currentInstance, id)}`;
  }, [id, currentInstance]);

  useEffect(() => {
    const query = getUpdatedQuery(searchToObj(), "group", group && group !== defaultGroup, group, false);
    const newLocationSearch = getLocationSearchFromQuery(query);
    if (newLocationSearch !== location.search) {
      navigate(`${location.pathname}${newLocationSearch}`);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [group]);


  const NavigationComponent = getNavigation(instanceProps && instanceProps.header);

  return (
    <>
      <div className="kgs-instance-container" >
        {warning && (
          <Notification text={warning} show={true} />
        )}
        {showInstance && (
          <>
            <BackLinkButton instance={previousInstance} />
            <Instance {...instanceProps} NavigationComponent={NavigationComponent} fetch={fetch} />
          </>
        )}
        {watermark && (
          <div className="kgs-instance-editor__watermark">
            <p>{watermark}</p>
          </div>
        )}
      </div>
      <InstanceErrorPanel />
    </>
  );
};
