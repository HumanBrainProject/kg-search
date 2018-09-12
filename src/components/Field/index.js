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
import { FieldLabel} from "./components/FieldLabel";
import { FieldHint} from "./components/FieldHint";
import { ListField } from "./components/ListField";
import { ObjectField } from "./components/ObjectField";
import { ValueField } from "./components/ValueField";
import "./styles.css";

export function Field({name, data, mapping, renderUserInteractions}) {
  if (!mapping || !mapping.visible || !(data || mapping.showIfEmpty)) {
    return null;
  }

  const isList = Array.isArray(data);
  const style = (mapping.order && !renderUserInteractions)?{order: mapping.order}:null;
  const className = "kgs-field" + (name?" kgs-field__" + name:"") + (mapping.layout?" kgs-field__layout-" + mapping.layout:"");

  const labelProps = {
    show: !!mapping.value && (!mapping.label_hidden || !!renderUserInteractions),
    showAsBlock: mapping.tag_icon,
    value: mapping.value
  };
  const hintProps = {
    show: renderUserInteractions && !!mapping.value && !!mapping.hint,
    value: mapping.hint,
    label: mapping.value
  };
  const listProps = {
    show: isList,
    items: data,
    mapping: mapping,
    renderUserInteractions: !!renderUserInteractions
  };
  const valueProps = {
    show: !isList,
    data: data,
    mapping: mapping,
    renderUserInteractions: !!renderUserInteractions
  };
  const objectProps = {
    show: !isList && !!mapping.children,
    data: data && data.children,
    mapping: mapping,
    renderUserInteractions: !!renderUserInteractions
  };

  return (
    <span style={style} className={className}>
      <FieldLabel {...labelProps} />
      <FieldHint {...hintProps} />
      <ValueField {...valueProps} />
      <ListField {...listProps} />
      <ObjectField {...objectProps} />
    </span>
  );
}