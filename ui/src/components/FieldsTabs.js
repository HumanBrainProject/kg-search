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
import { Field } from "./Field";
import { Tabs } from "./Tabs";

const getCount = field => {
  if (field.data && field.mapping && field.mapping.children) {
    let fieldToCount = null;
    Object.entries(field.mapping.children).some(([subField, mapping]) => {
      if (mapping && mapping.count) {
        fieldToCount = subField;
        return true;
      }
      return false;
    });
    if (fieldToCount) {
      const root = Array.isArray(field.data)?field.data:field.data?[field.data]:[];
      let counter = 0;
      root.forEach(item => {
        const subItems = item.children && item.children[fieldToCount];
        counter +=  Array.isArray(subItems)?subItems.length:1;
      });
      return counter;
    }
  }
  return Array.isArray(field.data)?field.data.length:field.data?1:0;
};

const getTabs = fields => {
  if (!Array.isArray(fields)) {
    return [];
  }
  return fields.map(field => {
    return {
      id: field.name,
      title: (field.mapping && field.mapping.value)?field.mapping.value:field.name,
      counter: getCount(field),
      hint: field.mapping?{
        show: !!field.mapping.hint,
        value: field.mapping.hint
      }:null,
      data: field
    };
  });
};

export const FieldsTabs = ({className, id, fields}) => {
  if (!fields || !fields.length) {
    return null;
  }
  const tabs = getTabs(fields);
  return (
    <Tabs className={className?className:""} id={id} tabs={tabs} viewComponent={Field} />
  );
};