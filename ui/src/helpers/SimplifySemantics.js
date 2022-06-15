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

const GRAPHQUERY_NAMESPACE = "https://schema.hbp.eu/graphQuery/";
const SEARCHUI_NAMESPACE = "https://schema.hbp.eu/searchUi/";
const SCHEMA_ORG = "http://schema.org/"; //NOSONAR it's a schema only

const getFieldPropertyValue = (field, key, defaultValue) => {
  let valueFromField = field[key];
  if (valueFromField !== undefined && valueFromField !== null) {
    return valueFromField;
  }
  return defaultValue;
};

const simplifySemanticKeysForField = definition => {
  const mapping = {
    label: getFieldPropertyValue(definition, GRAPHQUERY_NAMESPACE + "label", null),
    sort: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "sort", false),
    markdown: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "markdown", false),
    labelHidden: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "label_hidden", false),
    tagIcon: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "tag_icon", null),
    icon: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "icon", null),
    linkIcon: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "link_icon", null),
    visible: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "visible", true),
    isTable: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "isTable", false),
    isHierarchicalFiles: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "isHierarchicalFiles", false),
    isHierarchical:  getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "isHierarchical", false),
    isAsync: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "isAsync", false),
    isCitation: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "isCitation", false),
    isFilePreview: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "isFilePreview", false),
    isGroupedLinks: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "isGroupedLinks", false),
    showIfEmpty: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "showIfEmpty", false),
    layout: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "layout", null),
    hint: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "hint", null),
    overviewMaxDisplay: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "overviewMaxDisplay", null),
    termsOfUse: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "termsOfUse", false),
    separator: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "separator", null),
    overview: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "overview", false),
    order: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "order", null),
    aggregate: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "aggregate", null),
    type: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "type", null),
    detail_label: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "detail_label", null),
    count: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "count", false),
    collapsible: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "collapsible", false),
    ignoreForSearch: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "ignoreForSearch", false),
    highlight: getFieldPropertyValue(definition, SEARCHUI_NAMESPACE + "highlight", false)
  };
  if (definition.children instanceof Object && !Array.isArray(definition.children)) {
    mapping.children = Object.entries(definition.children).reduce((acc, [field, fieldDefinition]) => {
      acc[field] = simplifySemanticKeysForField(fieldDefinition);
      return acc;
    }, {});
  }
  return mapping;
};

const simplifySemanticKeyForType = definition => {
  const mapping = {};
  if (definition[SCHEMA_ORG + "name"]) {
    mapping.name = definition[SCHEMA_ORG + "name"];
  }
  if (definition[SEARCHUI_NAMESPACE + "order"]) {
    mapping.order = definition[SEARCHUI_NAMESPACE + "order"];
  }
  if (definition[SEARCHUI_NAMESPACE + "ribbon"]) {
    const ribbon = definition[SEARCHUI_NAMESPACE + "ribbon"];
    const framed = ribbon[SEARCHUI_NAMESPACE + "framed"];
    const suffix = framed[SEARCHUI_NAMESPACE + "suffix"];
    const datafield = framed[SEARCHUI_NAMESPACE + "dataField"].split(":");
    mapping.ribbon = {
      framed: {
        dataField: datafield.length ? datafield[1]: null,
        aggregation: framed[SEARCHUI_NAMESPACE + "aggregation"],
        suffix: {
          singular: suffix[SEARCHUI_NAMESPACE + "singular"],
          plural: suffix[SEARCHUI_NAMESPACE + "plural"]
        }
      }
    };
  }
  if (definition[SEARCHUI_NAMESPACE + "searchable"]) {
    mapping.searchable = definition[SEARCHUI_NAMESPACE + "searchable"];
  }
  if (definition[SEARCHUI_NAMESPACE + "defaultSelection"]) {
    mapping.defaultSelection = definition[SEARCHUI_NAMESPACE + "defaultSelection"];
  }
  if (definition.fields instanceof Object && !Array.isArray(definition.fields)) {
    mapping.fields = Object.entries(definition.fields).reduce((acc, [field, fieldDefinition]) => {
      acc[field] = simplifySemanticKeysForField(fieldDefinition);
      return acc;
    }, {});
  }
  if (definition.children instanceof Object && !Array.isArray(definition.children)) {
    mapping.children = Object.entries(definition.children).reduce((acc, [field, fieldDefinition]) => {
      acc[field] = simplifySemanticKeysForField(fieldDefinition);
      return acc;
    }, {});
  }
  if (definition.facets) {
    mapping.facets = [...definition.facets];
  }
  return mapping;
};

export const simplifySemantics = typesDefinition => {
  if (!(typesDefinition instanceof Object) || Array.isArray(typesDefinition)) {
    return {};
  }
  return Object.entries(typesDefinition).reduce((acc, [type, typeDefinition]) => {
    acc[type] = simplifySemanticKeyForType(typeDefinition);
    return acc;
  }, {});
};
