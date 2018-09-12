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
import { HitPanel} from "./components/HitPanel";

const markdownEscapedChars = {
  "&#x2F;": "\\",
  "&#x60;": "`",
  "&#x2a;": "*",
  "&#x5f;": "_",
  "&#x7b;": "{",
  "&#x7d;": "}",
  "&#x5b;": "[",
  "&#x5d;": "]",
  "&#x28;": "(",
  "&#x29;": ")",
  "&#x23;": "#",
  "&#x2b;": "+",
  "&#x2d;": "-",
  "&#x2e;": ".",
  "&#x21;": "!"
};

const replaceMarkdownEscapedChars = (str) => {
  Object.entries(markdownEscapedChars).forEach(([key, val]) => {
    str = str.replace(new RegExp(key, "g"), val);
  });
  return str.replace(/<\/?em>/gi,"");
};

const getTitleField = (data, highlight, mapping) => {
  let fieldData = data;
  if (highlight && highlight["title.value"] && highlight["title.value"].length > 0) {
    const value = replaceMarkdownEscapedChars(highlight["title.value"][0]);
    fieldData = Object.assign({}, data);
    fieldData.value = value;
  }

  return {
    name: "title",
    data: fieldData,
    mapping: mapping
  };
};

const getDescriptionField = (data, highlight, mapping) => {

  const fieldMapping = mapping && Object.assign({}, mapping, {collapsible: false});

  let fieldData = data;

  const value = data && data.value;
  let modifiedValue = value;

  if (highlight && highlight["description.value"] && highlight["description.value"].length > 0) {
    modifiedValue = replaceMarkdownEscapedChars(highlight["description.value"][0]);
    modifiedValue += "...";
  } else if (value && value.length > 220) {
    modifiedValue = value.substring(0, 217) + "...";
  }

  if (modifiedValue !== value) {
    fieldData = Object.assign({}, data);
    fieldData.value = modifiedValue;
  }

  return {
    name: "description",
    data: fieldData,
    mapping: fieldMapping
  };
};

const getComponentField = (data, mapping) => {
  let fieldData = data;
  let fieldMapping = mapping;
  if (data && data.value) {
    fieldData = Object.assign({}, data); // assuming value children are values
    fieldData.value = "From the " + data.value + " project";

    // remove title
    fieldMapping = mapping && Object.assign({}, mapping);
    fieldMapping && delete fieldMapping.value; // no deep cloning needed as only first level is modified
  }

  return {
    name: "component",
    data: fieldData,
    mapping: fieldMapping
  };
};

const getField = (type, name, data, highlight, mapping) => {
  switch (name) {
  case "title":
    return getTitleField(data, highlight, mapping);
  case "description":
    return getDescriptionField(data, highlight, mapping);
  case "component":
    return getComponentField(data, mapping);
  default:
    return {
      name: name,
      data: data,
      mapping: mapping
    };
  }
};

const getFields = (type, data, highlight, mapping) => {
  if (!data || !mapping) {
    return [];
  }
  const primaryFiels = ["title", "description"];
  const fields = Object.entries(mapping.fields || {})
    .filter(([name, mapping]) =>
      mapping
      && (mapping.overview || primaryFiels.includes(name))
      && (mapping.showIfEmpty || (data && data[name]))
    )
    .map(([name, mapping]) => getField(type, name, data[name], highlight, mapping));

  return fields;
};

const filterHighlightFields = (data, excludeFieldNames) => {
  if (!data) {
    return null;
  }
  if (!Array.isArray(excludeFieldNames) || excludeFieldNames.length === 0) {
    return data;
  }
  let hasFields = false;
  const fields = Object.entries(data)
    .filter(([name,]) => {
      return !(excludeFieldNames.includes(name));
    })
    .reduce((result, [name, field]) => {
      hasFields = true;
      result[name] = field;
      return result;
    }, {});
  return hasFields?fields:null;
};

export function Hit({data}) {

  const state = store.getState();
  const source = data && !(data.found === false) && data._type && data._source;
  const mapping = source && state.definition && state.definition.shapeMappings && state.definition.shapeMappings[data._type];

  const ribbonData = mapping && mapping.ribbon && mapping.ribbon.framed && mapping.ribbon.framed.data_field && source[mapping.ribbon.framed.data_field];
  const iconData = {
    value: data && data._type,
    image: {
      url: source && source.image && source.image.url
    }
  };
  const iconMapping = {
    visible: true,
    type: "icon",
    icon: mapping && mapping.icon
  };

  const hitProps = {
    type: data && data._type,
    hasNoData: !source,
    hasUnknownData: !mapping,
    ribbon: getField(data && data._type, "ribbon", ribbonData, null, mapping && mapping.ribbon),
    icon:  getField(data && data._type, "icon", iconData, null, iconMapping),
    fields: getFields(data && data._type, source, data && data.highlight, mapping, false),
    highlightsField: {
      fields: filterHighlightFields(data && data.highlight, ["title.value","description.value"]),
      mapping: mapping
    }
  };
  return (
    <HitPanel {...hitProps} />
  );
}