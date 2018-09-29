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
import { connect } from "../../helpers/react-redux-like";
import { PrintViewField } from "../Field";
import { HitRibbon } from "./HitRibbon";
import { HighlightsField} from "./HighlightsField";
import "./Hit.css";

export const HitBase = ({type, hasNoData, hasUnknownData, ribbon, icon, fields, highlightsField}) => (
  <div className="kgs-hit" data-type={type}>
    <HitRibbon className="kgs-hit__ribbon" {...ribbon} />
    <div className="kgs-hit__content">
      <PrintViewField key={icon && icon.name} {...icon}/>
      {fields.map(({name, data, mapping, index}) => (
        <PrintViewField key={name} name={name} data={data} mapping={mapping} index={index} />
      ))}
      <HighlightsField {...highlightsField} />
    </div>
    {hasNoData && (
      <div className="kgs-hit__no-data">This data is currently not available.</div>
    )}
    {hasUnknownData && (
      <div className="kgs-hit__no-data">This type of data is currently not supported.</div>
    )}
  </div>
);

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

const getTitleField = (index, data, highlight, mapping) => {
  let fieldData = data;
  if (highlight && highlight["title.value"] && highlight["title.value"].length > 0) {
    const value = replaceMarkdownEscapedChars(highlight["title.value"][0]);
    fieldData = Object.assign({}, data);
    fieldData.value = value;
  }

  return {
    name: "title",
    data: fieldData,
    mapping: mapping,
    index: index
  };
};

const getDescriptionField = (index, data, highlight, mapping) => {

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
    mapping: fieldMapping,
    index: index
  };
};

const getComponentField = (index, data, mapping) => {
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
    mapping: fieldMapping,
    index: index
  };
};

const getField = (index, type, name, data, highlight, mapping) => {
  switch (name) {
  case "title":
    return getTitleField(index, data, highlight, mapping);
  case "description":
    return getDescriptionField(index, data, highlight, mapping);
  case "component":
    return getComponentField(index, data, mapping);
  default:
    return {
      name: name,
      data: data,
      mapping: mapping,
      index: index
    };
  }
};

const getFields = (index, type, data, highlight, mapping) => {
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
    .map(([name, mapping]) => getField(index, type, name, data[name], highlight, mapping));

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

export const Hit = connect(
  (state, {data}) => {
    const source = data && !(data.found === false) && data._type && data._source;
    const mapping = source && state.definition && state.definition.shapeMappings && state.definition.shapeMappings[data._type];
    const index = (data && !(data.found === false) && data._index)?data._index:"public";

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

    return {
      type: data && data._type,
      hasNoData: !source,
      hasUnknownData: !mapping,
      ribbon: getField(index, data && data._type, "ribbon", ribbonData, null, mapping && mapping.ribbon),
      icon:  getField(index, data && data._type, "icon", iconData, null, iconMapping),
      fields: getFields(index, data && data._type, source, data && data.highlight, mapping, false),
      highlightsField: {
        fields: filterHighlightFields(data && data.highlight, ["title.value","description.value"]),
        mapping: mapping
      }
    };
  }
)(HitBase);