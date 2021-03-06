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
import { FieldLabel} from "./FieldLabel";
import { Hint} from "../Hint/Hint";
import { ListField, PrintViewListField } from "./ListField";
import { ObjectField, PrintViewObjectField } from "./ObjectField";
import { ValueField, PrintViewValueField } from "./ValueField";
import { TableField, PrintViewTableField } from "./TableField";
import HierarchicalFiles from "./Files/HierarchicalFiles";
import "./Field.css";
import { AsyncHierarchicalFiles } from "../../containers/Files/AsyncHierarchicalFiles";

const FieldBase = (renderUserInteractions = true) => {

  const ListFieldComponent = renderUserInteractions?ListField:PrintViewListField;
  const ObjectFieldComponent = renderUserInteractions?ObjectField:PrintViewObjectField;
  const ValueFieldComponent = renderUserInteractions?ValueField:PrintViewValueField;
  const TableFieldComponent = renderUserInteractions?TableField:PrintViewTableField;

  const Field = ({name, data, mapping, group}) => {
    if (!mapping || !mapping.visible || !(data || mapping.showIfEmpty)) {
      return null;
    }
    if(Array.isArray(data) && data.length === 1) {
      data = data[0];
    }
    const isList = Array.isArray(data);
    const isTable = mapping.isTable;
    const isHierarchicalFiles = mapping.isHierarchicalFiles;
    const asyncUrl = mapping.isAsync?data:null;
    const isButton = mapping.isButton;
    const style = (mapping.order && !renderUserInteractions)?{order: mapping.order}:null;
    const className = "kgs-field" + (name?" kgs-field__" + name:"") + (mapping.layout?" kgs-field__layout-" + mapping.layout:"") + (isTable?" kgs-field__table":"") + (isHierarchicalFiles?" kgs-field__hierarchical-files":"");

    const labelProps = {
      show: !!mapping.value && (!mapping.labelHidden || !renderUserInteractions) && !isButton,
      showAsBlock: mapping.tagIcon,
      value: mapping.value,
      counter: (mapping.layout === "group" && isList)?data.length:0
    };
    const hintProps = {
      show: renderUserInteractions && !!mapping.hint,
      value: mapping.hint
    };
    const listProps = {
      show: isList && !isHierarchicalFiles,
      items: data,
      mapping: mapping,
      group: group
    };
    const valueProps = {
      show: !isList && !isButton && !isHierarchicalFiles,
      data: data,
      mapping: mapping,
      group: group
    };
    const objectProps = {
      show: !isList && !!mapping.children,
      data: data && data.children,
      mapping: mapping,
      group: group
    };
    const tableProps = {
      show: isTable && !isHierarchicalFiles,
      items: data,
      mapping: mapping,
      group: group
    };
    const hierarchicalFileProps = {
      data: data,
      mapping: mapping,
      group: group
    };
    const asyncHierarchicalFileProps = {
      mapping: mapping,
      group: group,
      url: asyncUrl
    };

    return (
      <span style={style} className={className}>
        <FieldLabel {...labelProps} />
        <Hint {...hintProps} />
        <ValueFieldComponent {...valueProps} />
        <ListFieldComponent {...listProps} />
        <ObjectFieldComponent {...objectProps} />
        <TableFieldComponent {...tableProps} />
        {isHierarchicalFiles && (
          asyncUrl?
            <AsyncHierarchicalFiles  {...asyncHierarchicalFileProps} />
            :
            <HierarchicalFiles  {...hierarchicalFileProps} />
        )}
      </span>
    );
  };

  return Field;
};

export const Field = FieldBase(true);
export const PrintViewField = FieldBase(false);