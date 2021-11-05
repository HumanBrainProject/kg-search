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

import { getTags } from "../../helpers/InstanceHelper";
import { Tags } from "../Tags/Tags";
import { Field } from "../Field/Field";
import { FieldsPanel } from "../Field/FieldsPanel";
import { VersionSelector } from "../VersionSelector/VersionSelector";
import { history } from "../../store";
import { BgError } from "../BgError/BgError";

import "./Instance.css";
import Tabs from "../Tabs/Tabs";

// eslint-disable-next-line no-unused-vars
export const Instance = ({ id, type, group, path, defaultGroup, hasNoData, hasUnknownData, header, groups, NavigationComponent, ImagePopupComponent, TermsShortNoticeComponent, searchPage, fetch }) => {

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

  const tags = getTags(header);

  return (
    <div className="kgs-instance" data-type={type}>
      <div className="kgs-instance__header">
        <NavigationComponent />
        <div className="kgs-instance__header_fields">
          <Tags tags={tags} />
          <div className="kgs-instance__header_title">
            <Field {...header.title} />
            <VersionSelector version={header.version} versions={header.versions} onChange={onVersionChange} />
          </div>
          <FieldsPanel fields={header.fields} fieldComponent={Field} />
        </div>
      </div>
      <div className="kgs-instance-content">
        <Tabs instanceId={id} groups={groups} />
      </div>
      <TermsShortNoticeComponent />
      <ImagePopupComponent className="kgs-instance__image_popup" />
    </div>
  );
};