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
import ReactPiwik from "react-piwik";
import { history } from "../../store";

import { BgError } from "../BgError/BgError";
import { Header } from "./Header/Header";
import Tabs from "../Tabs/Tabs";

import "./Instance.css";
import "./Fields.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";


export const Instance = ({ id, type, group, path, defaultGroup, hasNoData, hasUnknownData, header, groups, NavigationComponent, ImagePopupComponent, TermsShortNoticeComponent, searchPage, fetch, isOutdated, latestVersion, allVersions }) => {

  useEffect(() => {
    trackEvent(hasNoData);
  }, [id, hasNoData, group]);

  const trackEvent = hasNoData => {
    const relativeUrl = `${path}/${id}${(group && group !== defaultGroup)?("?group=" + group):""}`;
    ReactPiwik.push(["trackEvent", "Card", hasNoData?"NotFound":"Opened", relativeUrl]);
  };

  const onVersionChange = version => {
    if(searchPage) {
      fetch(group, version, true);
    } else {
      history.push(`${path}${version}${group && group !== "public"?("?group=" + group ):""}`);
    }
  };

  if (hasNoData) {
    return(
      <BgError show={true} message="This data is currently not available." />
    );
  }

  if (hasUnknownData) {
    return(
      <BgError show={true} message="This type of data is currently not supported." />
    );
  }

  return (
    <div className={`kgs-instance ${isOutdated?"kgs-outdated":""}`} data-type={type}>
      <Header header={header} group={group} path={path} fetch={fetch} NavigationComponent={NavigationComponent} searchPage={searchPage} onVersionChange={onVersionChange} />
      {isOutdated && allVersions ? (
        <div className="kgs-outdated-alert" >
          <div className="alert alert-secondary" role="alert">
            <FontAwesomeIcon icon="info-circle"/>&nbsp;This is not the newest version of this {type.toLowerCase()}.
            <button className="kgs-instance-link" onClick={() => onVersionChange(latestVersion.value)}>
              Visit {latestVersion.label}
            </button> for the latest version or
            <button className="kgs-instance-link" onClick={() => onVersionChange(allVersions.reference)}>
              get an overview of all available versions
            </button>.
          </div>
        </div>):null}
      <Tabs instanceId={id} groups={groups} />
      <strong className="kgs-instance-disclaimer">Disclaimer:
          Please alert us at <a href="mailto:curation-support@ebrains.eu">curation-support@ebrains.eu</a> for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible.</strong>
      <TermsShortNoticeComponent />
      <ImagePopupComponent className="kgs-instance__image_popup" />
    </div>
  );
};
