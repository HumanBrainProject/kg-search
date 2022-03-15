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

import * as types from "./actions.types";
import API from "../services/API";
import { sessionFailure } from "./actions";


export const setAuthEndpoint = authEndpoint => {
  return {
    type: types.SET_AUTH_ENDPOINT,
    authEndpoint: authEndpoint
  };
};

export const setCommit = commit => {
  return {
    type: types.SET_COMMIT,
    commit: commit
  };
};

export const loadDefinitionRequest = () => {
  return {
    type: types.LOAD_DEFINITION_REQUEST
  };
};

export const loadDefinitionSuccess = definition => {
  return {
    type: types.LOAD_DEFINITION_SUCCESS,
    definition: definition
  };
};

export const loadDefinitionFailure = error => {
  return {
    type: types.LOAD_DEFINITION_FAILURE,
    error: error
  };
};

export const clearDefinitionError = () => {
  return {
    type: types.CLEAR_DEFINITION_ERROR
  };
};

export const loadDefinition = () => {

  const GRAPHQUERY_NAMESPACE = "https://schema.hbp.eu/graphQuery/";
  const SEARCHUI_NAMESPACE = "https://schema.hbp.eu/searchUi/";
  const SCHEMA_ORG = "http://schema.org/";

  const getFieldAndRemove = (field, key, defaultValue) => {
    let valueFromField = field[key];
    if (valueFromField !== undefined && valueFromField !== null) {
      delete field[key];
      return valueFromField;
    }
    return defaultValue;
  };

  const simplifySemanticKeysForField = field => {
    field.label = getFieldAndRemove(field, GRAPHQUERY_NAMESPACE + "label", null);
    field.sort = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "sort", false);
    field.markdown = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "markdown", false);
    field.labelHidden = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "label_hidden", false);
    field.tagIcon = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "tag_icon", null);
    field.icon = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "icon", null);
    field.linkIcon = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "link_icon", null);
    field.visible = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "visible", true);
    field.isTable = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "isTable", false);
    field.isHierarchicalFiles = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "isHierarchicalFiles", false);
    field.isAsync = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "isAsync", false);
    field.isFilePreview = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "isFilePreview", false);
    field.isGroupedLinks = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "isGroupedLinks", false);
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
    field.isHierarchicalFacet = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "isHierarchicalFacet", false);
    field.isFilterableFacet = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "isFilterableFacet", false);
    field.facetMissingTerm = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "facetMissingTerm", false);
    field.facetOrder = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "facet_order", "bycount");
    field.aggregate = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "aggregate", null);
    field.type = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "type", null);
    field.detail_label = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "detail_label", null);
    field.facetExclusiveSelection = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "facetExclusiveSelection", false);
    field.count = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "count", false);
    field.collapsible = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "collapsible", false);
    field.ignoreForSearch = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "ignoreForSearch", false);
    field.highlight = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "highlight", false);
    if (field.children) {
      Object.values(field.children).forEach(childField => {
        simplifySemanticKeysForField(childField);
      });
    }
  };

  const simplifySemanticKeyForType = mapping => {
    if (mapping[SCHEMA_ORG + "name"]) {
      mapping.name = mapping[SCHEMA_ORG + "name"];
      delete mapping[SCHEMA_ORG + "name"];
    }
    if (mapping[SEARCHUI_NAMESPACE + "order"]) {
      mapping.order = mapping[SEARCHUI_NAMESPACE + "order"];
      delete mapping[SEARCHUI_NAMESPACE + "order"];
    }
    if (mapping[SEARCHUI_NAMESPACE + "ribbon"]) {
      const ribbon = mapping[SEARCHUI_NAMESPACE + "ribbon"];
      const framed = ribbon[SEARCHUI_NAMESPACE + "framed"];
      const suffix = framed[SEARCHUI_NAMESPACE + "suffix"];
      delete mapping[SEARCHUI_NAMESPACE + "ribbon"];
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
    if (mapping[SEARCHUI_NAMESPACE + "searchable"]) {
      mapping.searchable = mapping[SEARCHUI_NAMESPACE + "searchable"];
      delete mapping[SEARCHUI_NAMESPACE + "searchable"];
    }
    if (mapping[SEARCHUI_NAMESPACE + "defaultSelection"]) {
      mapping.defaultSelection = mapping[SEARCHUI_NAMESPACE + "defaultSelection"];
      delete mapping[SEARCHUI_NAMESPACE + "defaultSelection"];
    }
    if (mapping.fields) {
      Object.values(mapping.fields).forEach(field => {
        simplifySemanticKeysForField(field);
      });
    }
    if (mapping.children) {
      Object.values(mapping.children).forEach(field => {
        simplifySemanticKeysForField(field);
      });
    }
  };

  const simplifySemantics = types => types instanceof Object && Object.values(types).forEach(mapping => simplifySemanticKeyForType(mapping));

  return dispatch => {
    dispatch(loadDefinitionRequest());
    API.axios
      .get(API.endpoints.definition())
      .then(({ data }) => {
        const definition = data && data._source;
        simplifySemantics(definition);
        data.authEndpoint && dispatch(setAuthEndpoint(data.authEndpoint));
        data.commit && dispatch(setCommit(data.commit));
        dispatch(loadDefinitionSuccess(definition));
      })
      .catch(e => {
        const { response } = e;
        const { status } = response;
        switch (status) {
        case 401: // Unauthorized
        case 403: // Forbidden
        case 511: // Network Authentication Required
        {
          const error = "Your session has expired. Please login again.";
          dispatch(sessionFailure(error));
          break;
        }
        default:
        {
          const error = `The service is temporary unavailable. Please retry in a moment. (${e.message?e.message:e})`;
          dispatch(loadDefinitionFailure(error));
        }
        }
      });
  };
};