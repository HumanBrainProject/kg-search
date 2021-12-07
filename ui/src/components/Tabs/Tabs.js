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
import React, { useState, useEffect } from "react";
import { Field } from "../Field/Field";
import { FieldsPanel } from "../Field/FieldsPanel";
import { ImagePreviews } from "../../containers/Image/ImagePreviews";
import "./Tabs.css";
import "./Overview.css";
import ReactPiwik from "react-piwik";

const Tab = ({group, active, onClick}) => {

  const handleClick = () => onClick(group);

  const className = `kgs-tabs__button ${active?"is-active":""}`;
  return (
    <button type="button" className={className} onClick={handleClick}>{group.name?group.name:""}</button>
  );
};


const TabsView = ({group}) => {
  if (!group || !Array.isArray(group.fields)) {
    return null;
  }

  if (group.name === "Overview") {
    const previews = group.previews;
    const summaryFields = group.fields.filter(f => f.mapping.layout === "summary");

    return (
      <div className={`kgs-tabs__view kgs-tabs__overview ${(previews && previews.length) ? "kgs-tabs__overview__with-previews" : ""}  ${(summaryFields && summaryFields.length) ? "kgs-tabs__overview__with-summary" : ""}`}>
        <ImagePreviews className={`kgs-tabs__overview__previews ${(previews && previews.length > 1) ? "has-many" : ""}`} width="300px" images={previews} />
        <FieldsPanel className="kgs-tabs__overview__summary" fields={summaryFields} fieldComponent={Field} />
        <FieldsPanel className="kgs-tabs__overview__main" fields={group.fields} fieldComponent={Field} />
      </div>
    );
  }

  return (
    <FieldsPanel className="kgs-tabs__view" fields={group.fields} fieldComponent={Field} />
  );
};

export const Tabs = ({instanceId, groups }) => {
  const [group, setGroup] = useState();

  useEffect(() => {
    if (!window.instanceTabSelection) {
      window.instanceTabSelection = {};
    }
    let selectedGroup = null;
    if (Array.isArray(groups) && groups.length) {
      if (window.instanceTabSelection[instanceId]) {
        selectedGroup = groups.find(g => g.name === window.instanceTabSelection[instanceId]);
      }
      if (!selectedGroup && Array.isArray(groups) && groups.length) {
        selectedGroup = groups[0];
      }
    }
    setGroup(selectedGroup);
  }, [instanceId]);

  const handleClick = g => {
    setGroup(g);
    if (!window.instanceTabSelection) {
      window.instanceTabSelection = {};
    }
    window.instanceTabSelection[instanceId] = g.name;
    ReactPiwik.push(["trackEvent", "Tab", `${g.name} clicked`, instanceId]);
  };

  if (!Array.isArray(groups) || !groups.length) {
    return null;
  }

  return (
    <>
      <div className="kgs-tabs__buttons">
        {groups.map(g => (
          <Tab key={g.name} group={g} active={group && g.name === group.name} onClick={handleClick} />
        ))}
      </div>
      <div className="kgs-tabs__content">
        <TabsView group={group}/>
      </div>
    </>
  );
};

export default Tabs;