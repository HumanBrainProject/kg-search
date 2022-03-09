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
import { getPreviews } from "../../../helpers/InstanceHelper";
import { PrintViewField } from "../../../components/Field/Field";
import { HitRibbon } from "./HitRibbon";
import { HighlightsField } from "./HighlightsField";
import { formatHitForHighlight } from "../../../helpers/HitFormattingHelpers";
import "./Hit.css";

export const HitBase = ({ type, hasNoData, hasUnknownData, ribbon, fields, preview, highlightsField }) => (
  <div className="kgs-hit" data-type={type}>
    <HitRibbon className="kgs-hit__ribbon" {...ribbon} />
    <div className={`kgs-hit__body ${preview? "has-preview":""}`}>
      <div className="kgs-hit__content">
        {insertSearchHightLights(fields, highlightsField)}
      </div>
      {!!preview &&
        <div className="kgs-hit__preview">
          <img src={preview} alt={preview}/>
        </div>
      }
    </div>
    {hasNoData && (
      <div className="kgs-hit__no-data">This data is currently not available.</div>
    )}
    {hasUnknownData && (
      <div className="kgs-hit__no-data">This type of data is currently not supported.</div>
    )}
  </div>
);

const insertSearchHightLights = (fields, highlightsField) => {
  // Removing the project field in the card if there is a Search hit on the project
  const hasProjectHit = highlightsField && highlightsField["fields"] && highlightsField["fields"] instanceof Object && Object.keys(highlightsField["fields"]).includes("component.value");
  const fieldsComponents = fields.filter(({ name }) => !hasProjectHit || name !== "component").map(({ name, data, mapping, group }) =>
    <PrintViewField key={name} name={name} data={data} mapping={mapping} group={group} />
  );
  fieldsComponents.splice(1, 0, <HighlightsField key="highlights" {...highlightsField}></HighlightsField>);
  return fieldsComponents;
};

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
  return formatHitForHighlight(str);
};

const getTitleField = (group, data, highlight, mapping) => {
  let fieldData = data;
  if (highlight && highlight["title.value"] && highlight["title.value"].length > 0) {
    const value = replaceMarkdownEscapedChars(highlight["title.value"][0]);
    fieldData = {
      ...data,
      value: value
    };
  }

  return {
    name: "title",
    data: fieldData,
    mapping: mapping,
    group: group
  };
};

const getDescriptionField = (group, data, highlight, mapping) => {

  const fieldMapping = mapping && {
    ...mapping,
    collapsible: false
  };

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
    fieldData = {
      ...data,
      value: modifiedValue
    };
  }

  return {
    name: "description",
    data: fieldData,
    mapping: fieldMapping,
    group: group
  };
};

const getComponentField = (group, data, mapping) => {
  let fieldData = data;
  let fieldMapping = mapping;
  if (data && data.value) {
    fieldData = {
      ...data, // assuming value children are values
      value: "From the " + data.value + " project"
    };

  }
  // remove title
  fieldMapping = mapping && { ...mapping };
  fieldMapping && delete fieldMapping.value; // no deep cloning needed as only first level is modified

  return {
    name: "component",
    data: fieldData,
    mapping: fieldMapping,
    group: group
  };
};

const getField = (group, type, name, data, highlight, mapping) => {
  switch (name) {
  case "title":
    return getTitleField(group, data, highlight, mapping);
  case "description":
    return getDescriptionField(group, data, highlight, mapping);
  case "component":
    return getComponentField(group, data, mapping, highlight);
  default:
    return {
      name: name,
      data: data,
      mapping: mapping,
      group: group
    };
  }
};

const getFields = (group, type, data, highlight, mapping) => {
  if (!data || !mapping) {
    return [];
  }
  const primaryFields = ["title", "description"];
  const fields = Object.entries(mapping.fields || {})
    .filter(([name, mapping]) =>
      mapping
      && (mapping.overview || primaryFields.includes(name))
      && (mapping.showIfEmpty || (data && data[name]))
    )
    .map(([name, mapping]) => getField(group, type, name, data[name], highlight, mapping));

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
  return hasFields ? fields : null;
};

export const Hit = connect(
  (state, { data }) => {

    const type = data?._source?.type?.value; // state.search.selectedType
    const source = data && data._source;
    const mapping = source && state.definition && state.definition.typeMappings && state.definition.typeMappings[type];
    const group = state.groups.group;

    const getPreview = () => {
      const previews = getPreviews(source);
      if(previews.length && previews[0].staticImageUrl) {
        return previews[0].staticImageUrl;
      }
      return null;
    };

    const ribbonData = mapping && mapping.ribbon && mapping.ribbon.framed && mapping.ribbon.framed.dataField && source[mapping.ribbon.framed.dataField];
    return {
      type: type,
      hasNoData: !source,
      hasUnknownData: !mapping,
      ribbon: getField(group, type, "ribbon", ribbonData, null, mapping && mapping.ribbon),
      fields: getFields(group, type, source, data && data.highlight, mapping, false),
      preview: getPreview(),
      highlightsField: {
        fields: filterHighlightFields(data && data.highlight, ["title.value", "description.value"]),
        mapping: mapping
      }
    };
  }
)(HitBase);