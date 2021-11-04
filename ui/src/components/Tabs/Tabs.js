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
import "./Tabs.css";

const Tab = ({tab, active, onClick}) => {

  const handleClick = () => onClick(tab);

  const className = `kgs-tabs-button ${active?"is-active":""}`;
  return (
    <button type="button" className={className} onClick={handleClick}>{tab.name?tab.name:""}</button>
  );
};


export const TabsView = ({group}) => {
  if (!group || !Array.isArray(group.fields)) {
    return null;
  }

  if (group.name === "Overview") {

    const mainFields = group.fields.filter(f => !f.mapping.layout);
    const summaryFields = group.fields.filter(f => f.mapping.layout === "summary");

    return (
      // <div className={`kgs-instance-content kgs-instance__grid ${(buttons && buttons.length) ? "kgs-instance__with-buttons" : ""} ${(previews && previews.length) ? "kgs-instance__with-previews" : ""}`}>
      <div className={"kgs-instance-content kgs-instance__grid"}>
        <div className="kgs-instance__main">
          {mainFields.map(f => <Field key={f.name} {...f} />)}
        </div>
        <div className="kgs-instance__summary">
          {summaryFields.map(f => <Field key={f.name} {...f} />)}
        </div>
      </div>
    );
  }

  return (
    <>
      {group.fields.map(f => <Field key={f.name} {...f} />)}
    </>
  );
};

export const Tabs = ({id, className, groups }) => {
  const [group, setGroup] = useState();

  useEffect(() => {
    setGroup(Array.isArray(groups) && groups.length && groups[0]);
  }, [id]);

  const handleClick = g => setGroup(g);

  const classNames = ["kgs-tabs", className].join(" ");

  if (!Array.isArray(groups) || !groups.length) {
    return null;
  }

  return (
    <div className={classNames}>
      <div className="kgs-tabs-buttons">
        {groups.map(g => (
          <Tab key={g.name} tab={g} active={group && g.name === group.name} onClick={handleClick} />
        ))}
      </div>
      <div className="kgs-instance-scroll">
        <div className="kgs-instance-scoll-content">
          <TabsView group={group}/>
        </div>
      </div>
    </div>
  );
};

export default Tabs;