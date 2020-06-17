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

import * as types from "./actions.types";
import API from "../services/API";
import { sessionFailure } from "./actions";
import * as Sentry from "@sentry/browser";

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
    field.value = getFieldAndRemove(field, GRAPHQUERY_NAMESPACE + "label", null);
    field.sort = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "sort", false);
    field.markdown = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "markdown", false);
    field.labelHidden = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "label_hidden", false);
    field.tagIcon = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "tag_icon", null);
    field.linkIcon = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "link_icon", null);
    field.visible = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "visible", true);
    field.isTable = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "isTable", false);
    field.isButton = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "isButton", false);
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
    field.nullValuesLabel = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "nullValuesLabel", false);
    field.facetOrder = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "facet_order", "bycount");
    field.aggregate = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "aggregate", null);
    field.type = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "type", null);
    field.detail_label = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "detail_label", null);
    field.facetExclusiveSelection = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "facetExclusiveSelection", false);
    field.count = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "count", false);
    field.collapsible = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "collapsible", false);
    field.ignoreForSearch = getFieldAndRemove(field, SEARCHUI_NAMESPACE + "ignoreForSearch", false);
  };

  const simplifySemantics = source => {
    if (source instanceof Object) {
      Object.keys(source).forEach(key => {
        simplifySemantics(source[key]);
      });
      if (source[SCHEMA_ORG + "identifier"]) {
        source.identifier = source[SCHEMA_ORG + "identifier"];
        delete source[SCHEMA_ORG + "identifier"];
      }
      if (source[SCHEMA_ORG + "name"]) {
        source.name = source[SCHEMA_ORG + "name"];
        delete source[SCHEMA_ORG + "name"];
      }
      if (source[SEARCHUI_NAMESPACE + "order"]) {
        source.order = source[SEARCHUI_NAMESPACE + "order"];
        delete source[SEARCHUI_NAMESPACE + "order"];
      }
      if (source[SEARCHUI_NAMESPACE + "ribbon"]) {
        const ribbon = source[SEARCHUI_NAMESPACE + "ribbon"];
        const framed = ribbon[SEARCHUI_NAMESPACE + "framed"];
        const suffix = framed[SEARCHUI_NAMESPACE + "suffix"];
        delete source[SEARCHUI_NAMESPACE + "ribbon"];
        const datafield = framed[SEARCHUI_NAMESPACE + "dataField"].split(":");
        source.ribbon = {
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
      if (source[SEARCHUI_NAMESPACE + "boost"]) {
        source.boost = source[SEARCHUI_NAMESPACE + "boost"];
        delete source[SEARCHUI_NAMESPACE + "boost"];
      }
      if (source[SEARCHUI_NAMESPACE + "defaultSelection"]) {
        source.defaultSelection = source[SEARCHUI_NAMESPACE + "defaultSelection"];
        delete source[SEARCHUI_NAMESPACE + "defaultSelection"];
      }
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
  };

  return dispatch => {
    dispatch(loadDefinitionRequest());
    API.axios
      .get(API.endpoints.definition())
      .then(({ data }) => {
        const definition = data && data._source;
        if (definition && definition.serviceUrl) {
          delete definition.serviceUrl;
        }
        simplifySemantics(definition);
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
        case 500:
        {
          Sentry.captureException(e);
          break;
        }
        case 404:
        default:
        {
          const error = `The service is temporary unavailable. Please retry in a moment. (${e.message?e.message:e})`;
          dispatch(loadDefinitionFailure(error));
        }
        }
      });
  };
};