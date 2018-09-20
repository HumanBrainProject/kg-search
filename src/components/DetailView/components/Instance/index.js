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

import { connect } from "../../../../store";
import { InstancePanel } from "./components/InstancePanel";

const getField = (type, name, data, mapping) => {
  switch (name) {
  case "type":
    return {
      name: "type",
      data: {value: type},
      mapping: {visible: true}
    };
  default:
    return {
      name: name,
      data: data,
      mapping: mapping
    };
  }
};

const getFields = (type, data, mapping, filter) => {
  if (!data || !mapping) {
    return [];
  }

  const fields = Object.entries(mapping.fields || {})
    .filter(([name, mapping]) =>
      mapping
        && (mapping.showIfEmpty || (data && data[name]))
        && (!filter || (typeof filter === "function" && filter(type, name, data[name], mapping)))
    )
    .map(([name, mapping]) => getField(type, name, data[name], mapping));

  return fields;
};

export const Instance = connect(
  (state, {data}) => {

    const source = data && !(data.found === false) && data._type && data._source;
    const mapping = source && state.definition && state.definition.shapeMappings && state.definition.shapeMappings[data._type];

    return {
      type: data && data._type,
      hasNoData: !source,
      hasUnknownData: !mapping,
      header: {
        icon:  getField(data && data._type, "icon", {value: data && data._type, image: {url: source && source.image && source.image.url}}, {visible: true, type: "icon", icon: mapping && mapping.icon}),
        type:  getField(data && data._type, "type"),
        title: getField(data && data._type, "title", source["title"], mapping && mapping.fields && mapping.fields["title"])
      },
      main: getFields(data && data._type, source, mapping, (type, name) => name !== "title"),
      summary: getFields(data && data._type, source, mapping, (type, name, data, mapping) => mapping.layout === "summary" && name !== "title"),
      groups: getFields(data && data._type, source, mapping, (type, name, data, mapping) => mapping.layout === "group" && name !== "title")
    };
  }
)(InstancePanel);