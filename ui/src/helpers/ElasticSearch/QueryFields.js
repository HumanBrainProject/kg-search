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

const addQueryFieldsFromFields = (queryFields, fields, parentPath) => {
  Object.entries(fields).forEach(([field, mapping]) => {
    if (!mapping.ignoreForSearch) {
      const path = `${parentPath}${field}`;
      const name = `${path}.value`;
      queryFields[name] = {
        boost: mapping.boost ?? 1,
        highlight: [
          "title.value",
          "description.value",
          "contributors.value",
          "custodians.value",
          "owners.value",
          "component.value",
          "created_at.value",
          "releasedate.value",
          "activities.value"
        ].includes(name) // temporary mapping.highlight
      };
      if (mapping.children !== undefined) {
        addQueryFieldsFromFields(queryFields, mapping.children, path + ".children.");
      }
    }
  });
};

const getQueryFieldsFromFields = fields => {
  const queryFields = {};
  addQueryFieldsFromFields(queryFields, fields, "");
  return queryFields;
};

export const getQueryFieldsByType = types => {
  if (!(types instanceof Object) || Array.isArray(types)) {
    return {};
  }

  return Object.entries(types).reduce((acc, [type, mapping]) => {
    acc[type] = getQueryFieldsFromFields(mapping.fields);
    return acc;
  }, {});
};

export const getQueryFields = (queryFieldsByType, selectedType) => {
  if (!(queryFieldsByType instanceof Object) || Array.isArray(queryFieldsByType)) {
    return [];
  }
  let queryFields = {};
  Object.entries(queryFieldsByType).forEach(([type, fields]) => {
    if (type !== selectedType) {
      queryFields = {
        ...queryFields,
        ...fields
      };
    }
  });
  // selected type fields override others
  if (selectedType && typeof selectedType === "string" && queryFieldsByType[selectedType]) {
    queryFields = {
      ...queryFields,
      ...queryFieldsByType[selectedType]
    };
  }
  return Object.entries(queryFields).map(([name, field]) => `${name}^${field.boost}`).sort();
};