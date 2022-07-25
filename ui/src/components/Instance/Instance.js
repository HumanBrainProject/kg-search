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

import React, { useEffect, useMemo } from "react";
import ReactPiwik from "react-piwik";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {faInfoCircle} from "@fortawesome/free-solid-svg-icons/faInfoCircle";
import { useNavigate } from "react-router-dom";

import { BgError } from "../BgError/BgError";
import { Header } from "./Header/Header";
import { Tabs } from "../../containers/Instance/Tabs";
import { Disclaimer } from "../Disclaimer/Disclaimer";

import "./Instance.css";
import "./Fields.css";


export const Instance = ({ id, type, group, path, defaultGroup, hasNoData, hasUnknownData, header, tabs, NavigationComponent, ImagePopupComponent, TermsShortNoticeComponent, searchPage, fetch, isOutdated, latestVersion, allVersions, disclaimer }) => {
  const navigate = useNavigate();

  useEffect(() => {
    const relativeUrl = `${path}/${id}${(group && group !== defaultGroup)?("?group=" + group):""}`;
    ReactPiwik.push(["trackEvent", "Card", hasNoData?"NotFound":"Opened", relativeUrl]);
  }, [id, hasNoData, group, defaultGroup, path]);

  const onVersionChange = useMemo(() => version => {
    if(searchPage) {
      fetch(group, version, navigate);
    } else {
      navigate(`${path}${version}${(group && group !== defaultGroup)?("?group=" + group ):""}`);
    }
  }, [fetch, path, group, defaultGroup, navigate, searchPage]);

  if (hasNoData) {
    return(
      <BgError message="This data is currently not available." />
    );
  }

  if (hasUnknownData) {
    return(
      <BgError message="This type of data is currently not supported." />
    );
  }

  return (
    <div className={`kgs-instance ${isOutdated?"kgs-outdated":""}`} data-type={type}>
      <Header header={header} NavigationComponent={NavigationComponent} onVersionChange={onVersionChange} />
      {isOutdated && allVersions ? (
        <div className="kgs-outdated-alert" >
          <div className="alert alert-secondary" role="alert">
            <FontAwesomeIcon icon={faInfoCircle} />&nbsp;This is not the newest version of this {type.toLowerCase()}.
            <button className="kgs-instance-link" onClick={() => onVersionChange(latestVersion.value)}>
            &nbsp;Visit {latestVersion.label}
            </button> for the latest version or
            <button className="kgs-instance-link" onClick={() => onVersionChange(allVersions.reference)}>
            &nbsp;get an overview of all available versions
            </button>.
          </div>
        </div>):null}
      <Tabs instanceId={id} tabs={tabs} />
      <Disclaimer content={disclaimer} />
      <TermsShortNoticeComponent />
      <ImagePopupComponent className="kgs-instance__image_popup" />
    </div>
  );
};
