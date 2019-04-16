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
import { connect } from "react-redux";
import API from "../services/API";
import { Field } from "./Field";
import { FieldsPanel } from "./FieldsPanel";
import { FieldsTabs } from "./FieldsTabs";
import "./Instance.css";

export const InstanceBase = ({className, type, hasNoData, hasUnknownData, header, main, summary, groups}) => {
  const classNames = ["kgs-instance", className].join(" ");
  return (
    <div className={classNames} data-type={type}>
      <div className="kgs-instance__content">
        <div className="kgs-instance__header">
          <div>
            <Field {...header.icon} />
            <Field {...header.type} />
          </div>
          <div>
            <Field {...header.title} />
          </div>
        </div>
        <div className="kgs-instance__body">
          <FieldsPanel className="kgs-instance__main" fields={main} fieldComponent={Field} />
          <FieldsPanel className="kgs-instance__summary" fields={summary} fieldComponent={Field} />
        </div>
        <FieldsTabs className="kgs-instance__groups" fields={groups} />
      </div>
      {hasNoData && (
        <div className="kgs-instance__no-data">This data is currently not available.</div>
      )}
      {hasUnknownData && (
        <div className="kgs-instance__no-data">This type of data is currently not supported.</div>
      )}
    </div>
  );
};

const getField = (index, type, name, data, mapping) => {
  switch (name) {
  case "type":
    return {
      name: "type",
      data: {value: type},
      mapping: {visible: true},
      index: index
    };
  default:
    return {
      name: name,
      data: data,
      mapping: mapping,
      index: index
    };
  }
};

const getFields = (index, type, data, mapping, filter) => {
  if (!data || !mapping) {
    return [];
  }

  const fields = Object.entries(mapping.fields || {})
    .filter(([name, mapping]) =>
      mapping
        && (mapping.showIfEmpty || (data && data[name]))
        && (!filter || (typeof filter === "function" && filter(type, name, data[name], mapping)))
    )
    .map(([name, mapping]) => getField(index, type, name, data[name], mapping));

  return fields;
};

export const Instance = connect(
  (state, {data}) => {

    const source = data && !(data.found === false) && data._type && data._source;
    const mapping = source && state.definition && state.definition.shapeMappings && state.definition.shapeMappings[data._type];
    const index = (data && !(data.found === false) && data._index)?data._index:API.defaultIndex;

    return {
      type: data && data._type,
      hasNoData: !source,
      hasUnknownData: !mapping,
      header: {
        icon:  getField(index, data && data._type, "icon", {value: data && data._type, image: {url: source && source.image && source.image.url}}, {visible: true, type: "icon", icon: mapping && mapping.icon}),
        type:  getField(index, data && data._type, "type"),
        title: getField(index, data && data._type, "title", source && source["title"], mapping && mapping.fields && mapping.fields["title"])
      },
      main: getFields(index, data && data._type, source, mapping, (type, name) => name !== "title"),
      summary: getFields(index, data && data._type, source, mapping, (type, name, data, mapping) => mapping.layout === "summary" && name !== "title"),
      groups: getFields(index, data && data._type, source, mapping, (type, name, data, mapping) => mapping.layout === "group" && name !== "title")
    };
  }
)(InstanceBase);