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
import { store } from "../../store";
import { Summary } from "./components/Summary";
import { Field } from "./components/Field";
import { HighlightsField} from "./components/HighlightsField";
import "./styles.css";

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

const getTypeField = (type, data) => {

  // type is not present in the data
  const fieldData = Object.assign({}, data);
  fieldData.type = {
    value: type
  };

  return {
    name: "type",
    data: fieldData,
    mapping: {visible: true},
    showSmartContent: false
  };
};

const getTitleField = (data, highlight, mapping, showSmartContent) => {

  // remove title
  const fieldMapping = mapping && Object.assign({}, mapping);
  fieldMapping && delete fieldMapping.value; // no deep cloning needed as only first level is modified

  let fieldData = data;

  let value = data && data.title && data.title.value;

  if (highlight && highlight["title.value"] && highlight["title.value"].length > 0) {
    value = replaceMarkdownEscapedChars(highlight["title.value"][0]);

    fieldData = Object.assign({}, data);
    fieldData.title = Object.assign({}, data.title); // assuming data.title children are values
    fieldData.title.value = value;
  }

  return {
    name: "title",
    data: fieldData,
    mapping: fieldMapping,
    showSmartContent: showSmartContent
  };
};

const getDescriptionField = (data, highlight, mapping, showSmartContent) => {

  // remove title
  const fieldMapping = mapping && Object.assign({}, mapping, {collapsible: !!showSmartContent});
  fieldMapping && delete fieldMapping.value; // no deep cloning needed as only first level is modified

  let fieldData = data;

  if(!showSmartContent){

    const value = data && data.description && data.description.value;
    let modifiedValue = value;

    if (highlight && highlight["description.value"] && highlight["description.value"].length > 0) {
      modifiedValue = replaceMarkdownEscapedChars(highlight["description.value"][0]);
      modifiedValue += "...";
    } else if(value && value.length > 220) {
      modifiedValue = value.substring(0, 217) + "...";
    }

    if (modifiedValue !== value) {
      fieldData = Object.assign({}, data);
      fieldData.description = Object.assign({}, data.description); // assuming data.description children are values
      fieldData.description.value = modifiedValue;
    }
  }

  return {
    name: "description",
    data: fieldData,
    mapping: fieldMapping,
    showSmartContent: showSmartContent
  };
};

const getComponentField = (data, mapping, showSmartContent) => {
  let fieldData = data;
  let fieldMapping = mapping;
  if (!showSmartContent && data && data.component && data.component.value) {
    fieldData = Object.assign({}, data);
    fieldData.component = Object.assign({}, data.component); // assuming data.component children are values
    fieldData.component.value = "From the " + data.component.value + " project";

    // remove title
    fieldMapping = mapping && Object.assign({}, mapping);
    fieldMapping && delete fieldMapping.value; // no deep cloning needed as only first level is modified
  }

  return {
    name: "component",
    data: fieldData,
    mapping: fieldMapping,
    showSmartContent: showSmartContent
  };
};

const getField = (type, name, data, highlight, mapping, showSmartContent) => {
  switch (name) {
  case "type":
    return getTypeField(type, data);
  case "title":
    return getTitleField(data, highlight, mapping, showSmartContent);
  case "description":
    return getDescriptionField(data, highlight, mapping, showSmartContent);
  case "component":
    return getComponentField(data, mapping, showSmartContent);
  default:
    return {
      name: name,
      data: data,
      mapping: mapping,
      showSmartContent: showSmartContent
    };
  }
};

const getFields = (type, data, highlight, mapping, showSmartContent) => {
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
        && (showSmartContent || mapping.overview)
        && (mapping.showIfEmpty || (data && data[name]))
        && !primaryFiels.includes(name) // exclude above "manually" defined fields
      )
    )
  ].map(([name, mapping]) => getField(type, name, data, highlight, mapping, showSmartContent));

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

const Icon = ({title, url, inline}) => {
  if (url) {
    return (
      <img src={url} alt={title} width="100%" height="100%" />
    );
  }
  if (inline) {
    return (
      <div dangerouslySetInnerHTML={{__html: inline}} width="100%" height="100%" />
    );
  }
  return (
    <i className="fa fa-tag" />
  );
};

const NoData = ({show}) => {
  if (!show) {
    return null;
  }
  return (
    <div className="kgs-shape__no-data">This data is currently not available.</div>
  );
};

const UnknownData = ({show}) => {
  if (!show) {
    return null;
  }
  return (
    <div className="kgs-shape__no-data">This type of data is currently not supported.</div>
  );
};

const ShapeComponent = ({type, hasNoData, hasUnknownData, icon, summary, fields, highlightsField}) => {
  return (
    <div className="kgs-shape" data-type={type}>
      <div className="kgs-shape__field kgs-shape__header">
        <div className="kgs-shape__field kgs-shape__icon">
          <Icon {...icon}/>
        </div>
        {fields.map(({name, data, mapping, showSmartContent}) => (
          <Field key={name} name={name} data={data} mapping={mapping} showSmartContent={showSmartContent} />
        ))}
        <Summary {...summary} />
        <HighlightsField {...highlightsField} />
      </div>
      <NoData show={hasNoData} />
      <UnknownData show={hasUnknownData} />
    </div>
  );
};

export function Shape({data, detailViewMode}) {

  const state = store.getState();
  const source = data && !(data.found === false) && data._type && data._source;
  const mapping = source && state.definition.shapeMappings[data._type];

  const shapeProps = {
    type: data && data._type,
    hasNoData: !source,
    hasUnknownData: !mapping,
    icon: {
      title: data && data._type,
      url: source && source.image && source.image.url,
      inline: mapping && mapping.icon
    },
    summary: {
      show: !!detailViewMode,
      data: source,
      mapping: mapping
    },
    fields: getFields(data && data._type, source, data && data.highlight, mapping, !!detailViewMode),
    highlightsField: {
      fields: filterHighlightFields(data && data.highlight, ["title.value","description.value"]),
      mapping: mapping
    }
  };
  return (
    <ShapeComponent {...shapeProps} />
  );
}