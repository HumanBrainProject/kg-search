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
import { FieldLabel} from "../FieldLabel";
import { FieldHint} from "../FieldHint";
import { ListField } from "../ListField";
import { ObjectField } from "../ObjectField";
import { ValueField } from "../ValueField";

export function Field({name, data, mapping, showSmartContent}) {
  const value = data && name && data[name];
  if (!mapping || !mapping.visible || !(value || mapping.showIfEmpty)) {
    return null;
  }

  const isList = Array.isArray(value);
  const style = (mapping.order && !showSmartContent)?{order: mapping.order}:null;
  const className = "kgs-shape__field" + (name?" kgs-shape__" + name:"");

  const labelProps = {
    show: mapping.value && (!mapping.label_hidden || showSmartContent),
    showAsBlock: mapping.tag_icon,
    value: mapping.value
  };
  const hintProps = {
    show: mapping.value && mapping.hint,
    value: mapping.hint,
    label: mapping.value
  };
  const listProps = {
    show: isList,
    items: value,
    mapping: mapping,
    showSmartContent: showSmartContent
  };
  const valueProps = {
    show: !isList,
    value: value,
    mapping: mapping,
    showSmartContent: showSmartContent
  };
  const objectProps = {
    show: !isList && mapping.children,
    value: value && value.children,
    mapping: mapping,
    showSmartContent: showSmartContent
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