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

import Tags from "../../../components/Tags/Tags";
import { Field, Title } from "../../Field/Field";
import FieldsPanel from "../../../components/Field/FieldsPanel";
import { VersionSelector } from "../../../components/VersionSelector/VersionSelector";
import ShareButtons from "../../../features/ShareButtons";

import "./Header.css";

const DefaultNavigation = ({ tags }) => (
  <div className="kgs-instance__header_navigation">
    <div className="kgs-instance__header_navigation_left">
      <Tags tags={tags} />
    </div>
    <ShareButtons />
  </div>
);

const getDefaultNavigation = tags => {
  const Navigation = () => (
    <DefaultNavigation tags={tags} />
  );
  Navigation.displayName = "Navigation";
  return Navigation;
};

const Header = ({title, version, tags, fields, versions, customNavigationComponent, onVersionChange}) => {

  const Navigation = customNavigationComponent?customNavigationComponent:getDefaultNavigation(tags);

  return (
    <div className="kgs-instance__header">
      <Navigation />
      <div className="kgs-instance__header_fields">
        {customNavigationComponent && (
          <Tags tags={tags} />
        )}
        <div className="kgs-instance__header_title">
          <Title text={title} />
          <VersionSelector version={version} versions={versions} onChange={onVersionChange} />
        </div>
        <FieldsPanel fields={fields} fieldComponent={Field} />
      </div>
    </div>
  );
};

export default Header;