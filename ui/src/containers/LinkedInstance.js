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
import { connect } from "react-redux";

import { Field } from "../components/Field/Field";
import { FieldsPanel } from "../components/Field/FieldsPanel";

import "./LinkedInstance.css";

const LinkedInstanceComponent = ({data, mapping, group, type}) => {
  if (!data || typeof data !== "object") {
    return null;
  }
  // BEGIN v1 file
  if (type === "File" && (!mapping || typeof mapping !== "object")) {
    if (!data || !data.fileSize) {
      return null;
    }
    mapping = {
      size: {
        label: "Size",
        order: 13
      }
    };
    data.size = {
      value: data.fileSize
    };
  }
  // END v1 file
  const fields = Object.entries(mapping)
    .filter(([name, fieldsMapping]) => fieldsMapping && data?.[name])
    .map(([name, fieldsMapping]) => ({
      name: name,
      data: data[name],
      mapping: fieldsMapping,
      group: group,
      type: type
    }));
  return (
    <FieldsPanel className="kgs-linked-instance" fields={fields} fieldComponent={Field} />
  );
};


export const LinkedInstance = connect(
  (state, props) => {
    const mapping = Object.entries((props.type && state.instances.typeMappings[props.type] && state.instances.typeMappings[props.type].fields)?state.instances.typeMappings[props.type].fields:{}).reduce((acc, [name, fieldsMapping]) => {
      if (
        name !== "title" && // filter title as we only want to show the details of the linked instance
        name !== "label" && // filter label as we only want to show the details of the linked instance
        !fieldsMapping.isAsync && // filter async data in linked instance
        !(props.type === "File" && name === "iri") // filter iri in file linked instance
      ) {
        acc[name] = fieldsMapping;
      }
      return acc;
    }, {});
    return {
      data: props.data,
      mapping: mapping,
      group: props.group,
      type: props.type
    };
  }
)(LinkedInstanceComponent);

export default LinkedInstance;