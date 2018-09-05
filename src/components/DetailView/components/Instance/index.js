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
import { store } from "../../../../store";
import { InstancePanel } from "./components/InstancePanel";

const removedTitleField = (name, data, mapping) => {

  // remove title
  const fieldMapping = mapping && Object.assign({}, mapping);
  fieldMapping && delete fieldMapping.value; // no deep cloning needed as only first level is modified

  return {
    name: name,
    data: data,
    mapping: fieldMapping
  };
};

const getField = (type, name, data, mapping) => {
  switch (name) {
  case "type":
    return {
      name: "type",
      data: {value: type},
      mapping: {visible: true}
    };
  case "title":
    return removedTitleField(name, data, mapping);
  case "description":
    return removedTitleField(name, data, mapping);
  default:
    return {
      name: name,
      data: data,
      mapping: mapping
    };
  }
};

const getFields = (type, data, mapping) => {
  if (!data || !mapping) {
    return [];
  }

  const primaryFiels = ["title", "description"];
  const fields = [
    ["type", {}],
    ...(primaryFiels
      .map(name => ([name, mapping.fields && mapping.fields[name]]))
      .filter(([,mapping]) => mapping)
    ),
    ...(Object.entries(mapping.fields || {})
      .filter(([name, mapping]) =>
        mapping
        && (mapping.showIfEmpty || (data && data[name]))
        && !primaryFiels.includes(name) // exclude above "manually" defined fields
      )
    )
  ].map(([name, mapping]) => getField(type, name, data[name], mapping));

  return fields;
};

export function Instance({data}) {

  const state = store.getState();
  const source = data && !(data.found === false) && data._type && data._source;
  const mapping = source && state.definition && state.definition.shapeMappings && state.definition.shapeMappings[data._type];

  const instanceProps = {
    type: data && data._type,
    hasNoData: !source,
    hasUnknownData: !mapping,
    icon: {
      title: data && data._type,
      url: source && source.image && source.image.url,
      inline: mapping && mapping.icon
    },
    fields: getFields(data && data._type, source, mapping)
  };
  return (
    <InstancePanel {...instanceProps} />
  );
}