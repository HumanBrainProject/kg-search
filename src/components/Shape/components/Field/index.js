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

import React from 'react';
import { ListField } from '../ListField';
import { ObjectField } from '../ObjectField';
import { ValueField } from '../ValueField';
import { HintField} from '../HintField';

export function Field({name, data, mapping, showSmartContent}) {

  if (!mapping || !mapping.visible || !(mapping.showIfEmpty || data[name]))
    return null;
  let displayValue;
  if(name === 'component'){
    displayValue = "From the " + data[name].value + " project";
  }
  let labelTag = null;
  let valueTag = null;
  let objectTag = null;
  let listTag = null;
  let hintTag = null;
  let order = null;

  if(mapping.order && !showSmartContent){
    order = {
      "order": mapping.order
    };
  }

  if (mapping.value){
    if(mapping.label_hidden && !showSmartContent){
      labelTag = null;
    } else {
      labelTag = <div className="field-label">{mapping.value}</div>;
    }
  }

  if(mapping.hint)
    hintTag = <HintField value = {mapping.hint} label = {mapping.value} />;

  const value = data && name && data[name];
  if (value) {
    if (Array.isArray(value)) {
      listTag = <ListField items={value} mapping={mapping} showSmartContent={showSmartContent} />;
    } else {
      valueTag = <ValueField value={value} mapping={mapping} showSmartContent={showSmartContent} displayValue={displayValue}/>;
      if (mapping && mapping.children) {
        objectTag = <ObjectField data={value.children} mapping={mapping} showSmartContent={showSmartContent} />;
      }
    }
  };

  return (
    <span style={order} className={"kgs-shape__field" + (name?" kgs-shape__" + name:"")}>
      {labelTag}
      {hintTag}
      {valueTag}
      {objectTag}
      {listTag}
    </span>
  );
}
