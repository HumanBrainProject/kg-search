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
import { FieldLabel} from "./Field/FieldLabel";
import { Hint} from "./Hint";
import { ListField, PrintViewListField } from "./Field/ListField";
import { ObjectField, PrintViewObjectField } from "./Field/ObjectField";
import { ValueField, PrintViewValueField } from "./Field/ValueField";
import { TableField, PrintViewTableField } from "./Field/TableField";
import { ButtonField, PrintViewButtonField } from "./Field/ButtonField";
import "./Field.css";

const FieldBase = (renderUserInteractions = true) => {

  const ListFieldComponent = renderUserInteractions?ListField:PrintViewListField;
  const ObjectFieldComponent = renderUserInteractions?ObjectField:PrintViewObjectField;
  const ValueFieldComponent = renderUserInteractions?ValueField:PrintViewValueField;
  const TableFieldComponent = renderUserInteractions?TableField:PrintViewTableField;
  const ButtonFieldComponent = renderUserInteractions?ButtonField:PrintViewButtonField;

  const Field = ({name, data, mapping, group}) => {
    if (!mapping || !mapping.visible || !(data || mapping.showIfEmpty)) {
      return null;
    }

    const isList = Array.isArray(data);
    const isTable = mapping.isTable;
    const isButton = mapping.isButton;
    const style = (mapping.order && !renderUserInteractions)?{order: mapping.order}:null;
    const className = "kgs-field" + (name?" kgs-field__" + name:"") + (mapping.layout?" kgs-field__layout-" + mapping.layout:"") + (isTable?" kgs-field__table":"");

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
      show: isList,
      items: data,
      mapping: mapping,
      group: group
    };
    const valueProps = {
      show: !isList && !isButton,
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
      show: isTable,
      items: data,
      mapping: mapping,
      group: group
    };
    const buttonProps = {
      show: isButton,
      items: data,
      mapping: mapping,
      group: group
    };

    return (
      <span style={style} className={className}>
        <FieldLabel {...labelProps} />
        <Hint {...hintProps} />
        <ValueFieldComponent {...valueProps} />
        <ListFieldComponent {...listProps} />
        <ObjectFieldComponent {...objectProps} />
        <TableFieldComponent {...tableProps} />
        <ButtonFieldComponent {...buttonProps} />
      </span>
    );
  };

  return Field;
};

export const Field = FieldBase(true);
export const PrintViewField = FieldBase(false);