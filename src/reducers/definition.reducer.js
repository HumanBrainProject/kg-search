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

import * as types from "../actions.types";

const initialState = {
  isReady: false,
  hasRequest: false,
  isLoading: false,
  hasError: false,
  shapeMappings: {},
  queryFields: ["title", "description"],
  facetFields: [],
  sortFields: []
};

const loadDefinition = state => {
  return Object.assign({}, state, {
    hasRequest: true,
    isLoading: false,
    hasError: false
  });
};

const loadDefinitionRequest = state => {
  return Object.assign({}, state, {
    hasRequest: false,
    isLoading: true,
    hasError: false
  });
};

const loadDefinitionSuccess = (state, action) => {

  const source = action.definition && action.definition._source;
  simplifySemantics(source);
  const shapeMappings = source;

  const initQueryFieldsRec = (queryFields, shapeFields, parent) => {
    Object.keys(shapeFields).forEach(fieldName => {
      const field = shapeFields[fieldName];
      const fullFieldName = parent + fieldName;
      let queryField = queryFields[fullFieldName];
      if (!queryField) {
        queryField = {boost: 1};
        queryFields[fullFieldName] = queryField;
      }

      if (field && field.boost && field.boost > queryField.boost) {
        queryField.boost = field.boost;
      }

      if (field["children"] !== undefined) {
        initQueryFieldsRec(queryFields, field["children"], fullFieldName + ".children.");
      }
    });
  };

  let queryFields = {};
  if (source) {
    Object.keys(source).forEach(shape => {
      const shapeFields = source[shape] && source[shape].fields;
      initQueryFieldsRec(queryFields, shapeFields, "");
    });
  }
  queryFields = Object.keys(queryFields).map(fieldName => {
    const boost = queryFields[fieldName].boost;
    if (boost) {
      return fieldName + ".value^" + boost;
    }
    return fieldName + ".value";
  });
  let facetFields = {};
  let sortFields = {_score: {label: "Relevance", field: "_score", order: "desc", defaultOption: true}};
  if (source) {
    Object.keys(source).forEach(type => {
      facetFields[type] = {};
      Object.keys(source[type].fields).forEach(fieldName => {
        const field = source[type].fields[fieldName];
        if (field.facet) {
          facetFields[type][fieldName] = {
            filterType: field.facet,
            filterOrder: field.facetOrder,
            exclusiveSelection: field.facetExclusiveSelection,
            fieldType: field.type,
            fieldLabel: field.value,
            isChild: false
          };
        }
        if (field.children) {
          Object.keys(field.children).forEach(childName => {
            const child = source[type].fields[fieldName].children[childName];
            if (child.facet) {
              facetFields[type][fieldName + ".children." + childName] = {
                filterType: child.facet,
                filterOrder: child.facetOrder,
                exclusiveSelection: field.facetExclusiveSelection,
                fieldType: child.type,
                fieldLabel: child.value,
                isChild: true,
                path: fieldName + ".children"
              };
            }
          });
        }
        if (field.sort && sortFields[fieldName] === undefined) {
          sortFields[fieldName] = {label: field.value, field: fieldName + ".value.keyword", order: "asc"};
        }
      });
    });
  }

  sortFields = Object.values(sortFields);

  return Object.assign({}, state, {
    isReady: true,
    hasRequest: false,
    isLoading: false,
    shapeMappings: shapeMappings,
    queryFields: queryFields,
    facetFields: facetFields,
    sortFields: sortFields
  });
};

const loadDefinitionFailure = (state) => {
  return Object.assign({}, state, {
    isReady: false,
    hasRequest: false,
    isLoading: false,
    hasError: true
  });
};

const GRAPHQUERY_NAMESPACE = "https://schema.hbp.eu/graphQuery/";
const SEARCHUI_NAMESPACE = "https://schema.hbp.eu/searchUi/";

function getFieldAndRemove(field, key, defaultValue) {
  let valueFromField = field[key];
  if (valueFromField !== undefined && valueFromField !== null) {
    delete field[key];
    return valueFromField;
  }
  return defaultValue;
}

function simplifySemantics(source) {
  if (source instanceof Object) {
    Object.keys(source).forEach(key => {
      simplifySemantics(source[key]);
    });
    if (source[SEARCHUI_NAMESPACE + "icon"]) {
      source.icon = source[SEARCHUI_NAMESPACE + "icon"];
      delete source[SEARCHUI_NAMESPACE + "icon"];
    }
    if (source.fields) {
      Object.keys(source.fields).forEach(field => {
        simplifySemanticKeysForField(source.fields[field]);
      });
    }
    if (source.children) {
      Object.keys(source.children).forEach(field => {
        simplifySemanticKeysForField(source.children[field]);
      });
    }

  }
}


function simplifySemanticKeysForField(field) {

  field.value = getFieldAndRemove(field, GRAPHQUERY_NAMESPACE + "label", null);
  field.sort = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "sort", false);
  field.markdown = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "markdown", false);
  field.labelHidden = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "labelHidden", false);
  field.tagIcon = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "tag_icon", null);
  field.visible = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "visible", true);
  field.showIfEmpty = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "showIfEmpty", false);
  field.layout = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "layout", null);
  field.hint = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "hint", null);
  field.overviewMaxDisplay = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "overviewMaxDisplay", null);
  field.termsOfUse = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "termsOfUse", false);
  field.separator = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "separator", null);
  field.overview = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "overview", false);
  field.boost = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "boost", 1);
  field.order = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "order", null);
  field.facet = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "facet", null);
  field.facetOrder = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "facet_order", "bycount");
  field.aggregate = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "aggregate", null);
  field.type = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "type", null);
  field.detail_label = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "detail_label", null);
  field.facetExclusiveSelection = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "facetExclusiveSelection", false);

}


export function reducer(state = initialState, action = {}) {
  switch (action.type) {
  case types.LOAD_DEFINITION:
    return loadDefinition(state, action);
  case types.LOAD_DEFINITION_REQUEST:
    return loadDefinitionRequest(state, action);
  case types.LOAD_DEFINITION_SUCCESS:
    return loadDefinitionSuccess(state, action);
  case types.LOAD_DEFINITION_FAILURE:
    return loadDefinitionFailure(state, action);
  default:
    return state;
  }
}