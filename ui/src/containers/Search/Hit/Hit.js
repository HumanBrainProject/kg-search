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

import { Badges } from "../../../components/Badges/Badges";
import { PrintViewField } from "../../../components/Field/Field";
import { HighlightsField } from "./HighlightsField";
import { formatHitForHighlight } from "../../../helpers/HitFormattingHelpers";
import { Title } from "../../../components/Field/Field";
import "./Hit.css";

export const HitBase = ({ type, hasNoData, hasUnknownData, title, fields, previewImage, badges, highlightsField }) => (
  <div className="kgs-hit" data-type={type}>
    <div className={`kgs-hit__body ${previewImage? "has-previewImage":""}`}>
      <div className={`kgs-hit__content ${badges? "has-badges":""}`}>
        <Badges badges={badges} />
        <Title key="title" text={title} />
        <HighlightsField key="highlights" {...highlightsField}></HighlightsField>
        {fields.map(({ name, data, mapping, group }) =>
          <PrintViewField key={name} name={name} data={data} mapping={mapping} group={group} />
        )}
      </div>
      {!!previewImage &&
        <div className="kgs-hit__preview">
          <img src={previewImage} alt={previewImage}/>
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

const markdownEscapedChars = {
  "&#92;": "\\",
  "&#x2F;": "/",
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

const getTitle = (text, highlight) => {
  if (highlight && highlight["title.value"] && highlight["title.value"].length > 0) {
    return formatHitForHighlight(highlight["title.value"][0]);
  }
  return text;
};

const getDescriptionField = (group, data, highlight, mapping) => {

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
    mapping: mapping,
    group: group
  };
};

const getField = (group, name, data, highlight, mapping) => {
  switch (name) {
  case "description":
    return getDescriptionField(group, data, highlight, mapping);
  default:
    return {
      name: name,
      data: data,
      mapping: mapping,
      group: group
    };
  }
};

const getFields = (group, data, highlight, parentMapping) => {
  if (!data || !parentMapping) {
    return [];
  }
  return Object.entries(parentMapping.fields || {})
    .filter(([name, mapping]) =>
      mapping
      && (data?.[name])
    )
    .map(([name, mapping]) => getField(group, name, data[name], highlight, mapping));
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

    const type = data?.type; // state.search.selectedType?.type
    const fields = data?.fields;
    const mapping = fields && state.settings && state.settings.typeMappings && state.settings.typeMappings[type];
    const group = state.groups.group;

    return {
      type: type,
      hasNoData: !fields,
      hasUnknownData: !mapping,
      title: getTitle(data?.title, data?.highlight),
      fields: getFields(group, fields, data && data.highlight, mapping),
      previewImage: data?.previewImage,
      badges: (data?.badges && Object.keys(data.badges).length)?data.badges:null,
      highlightsField: {
        fields: filterHighlightFields(data?.highlight, ["title.value", "description.value"]),
        mapping: mapping
      }
    };
  }
)(HitBase);