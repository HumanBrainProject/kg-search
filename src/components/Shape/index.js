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
import { store } from "../../store";
import { Summary } from './components/Summary';
import { Field } from './components/Field';
import { HighlightsField} from './components/HighlightsField';
import './styles.css';

const markdownEscapedChars = {
  "&#x2F;": "\\",
  "&#x60;": "`",
  "&#x2a;": "*",
  "&#x5f;": "_",
  "&#x7b;": "{",
  "&#x7d;": "}",
  "&#x5b;": "[",
  "&#x5d;": "]",
  "&#x28;": "(",
  "&#x29;": ")",
  "&#x23;": "#",
  "&#x2b;": "+",
  "&#x2d;": "-",
  "&#x2e;": ".",
  "&#x21;": "!"
};

const replaceMarkdownEscapedChars = (str) => {
  Object.entries(markdownEscapedChars).forEach(([key, val]) => {
    str = str.replace(new RegExp(key, "g"), val);
  });
  return str.replace(/<\/?em>/gi,"");
};

export function Shape({detailViewMode, data}) {
  
  const state = store.getState();

  let iconTag = <i className="fa fa-tag" />;
  let noDataTag = <div className="kgs-shape__no-data">This data is currently not available.</div>;
  let fieldsTag = null;

  const source = data && !(data.found === false) && data._type && data._source;
  if (source) {

    const mapping = state.configuration.shapeMappings[data._type];
    if (!mapping) {

      if (source.image && source.image.url)
        iconTag = <img src={source.image.url} alt={data._type} width="100%" height="100%" />;
    
      noDataTag = <div className="kgs-shape__no-data">This type of data is currently not supported.</div>;

      fieldsTag = <Field name="type" data={{type: {value: data._type}}} mapping={{visible: true}} showSmartContent={false} />;

    } else {

      noDataTag = null;

      if (source.image && source.image.url) {
        iconTag = <img src={source.image.url} alt={data._type} width="100%" height="100%" />;
      } else if (mapping.icon) {
        iconTag = <div dangerouslySetInnerHTML={{__html: mapping.icon}} width="100%" height="100%" />;
      }

      let description  = "";
      if (source.description && source.description.value) {
        description = source.description.value;
        if(!detailViewMode){
          if(data.highlight && data.highlight["description.value"] && data.highlight["description.value"].length > 0){
            description = replaceMarkdownEscapedChars(data.highlight["description.value"][0]);
            description += "...";
          } else if(description.length > 220){
            description = description.substring(0, 217) + "...";
          }
        } else {
          mapping.fields["description"].collapsible = true;
        }
      }
      if (mapping && mapping.fields["description"]) 
        delete  mapping.fields["description"].value;

      let title = source.title && source.title.value;
      if(data.highlight && data.highlight["title.value"] && data.highlight["title.value"].length > 0)
        title = replaceMarkdownEscapedChars(data.highlight["title.value"][0]);
      if (mapping && mapping.fields["title"]) 
        delete  mapping.fields["title"].value;

      fieldsTag = [
        {
          name: "type", 
          data: {
            type: {
              value: mapping.label?mapping.label:data._type
            }
          }, 
          mapping: {
            visible: false
          } 
        },
        {
          name: "title", 
          data: title?{
            title: {
              value: title
            }
          }:{}, 
          mapping: mapping && mapping.fields["title"] 
        }
      ].map(e => <Field name={e.name} data={e.data} mapping={e.mapping} key={e.name} showSmartContent={detailViewMode} />);
      
      if (detailViewMode) {
        fieldsTag.push(<Summary data={source} mapping={mapping} key="summary" />);
      }

      fieldsTag.push(...[
        {
          name: "description", 
          data: (description !== "")?{
            description: {
              value: description
            }
          }:{}, 
          mapping: mapping && mapping.fields["description"]
        }
      ].map(e => <Field name={e.name} data={e.data} mapping={e.mapping} key={e.name} showSmartContent={detailViewMode} />));
    
      if (mapping.fields) {
        fieldsTag.push(...(Object.entries(mapping.fields)
          .map(([name, mapping]) => ({
            name: name,
            mapping: mapping
          }))
          .filter(e => 
                e.mapping
              && (detailViewMode || e.mapping.overview)
              && (e.mapping.showIfEmpty || source[e.name])
              && ["image", "title", "description"].indexOf(e.name) === -1
          )
          .map(e => <Field name={e.name} data={source} mapping={e.mapping} showSmartContent={detailViewMode} key={e.name} />)));
      }

      if(!detailViewMode){
        let highlightFields;
        if(data.highlight){
          Object.keys(data.highlight).forEach(field => {
            if(["title.value","description.value"].indexOf(field) !== -1){
              return;
            }
            if(!highlightFields){
              highlightFields = {}
            }
            highlightFields[field] = data.highlight[field];
          });
        }
        
        if(highlightFields){
          fieldsTag.push(
            <HighlightsField mapping={mapping} highlights={highlightFields} key={"highlightFields"}/>//[{name: "description", data: description}].map(e => <Field name={e.name} data={e.data} key={e.name} />)
          );
        }
      }
    }
  }

  return (
    <div className={"kgs-shape__panel"} >
      <div className={"kgs-shape__field kgs-shape__header"}>
        <div className={"kgs-shape__field kgs-shape__icon"}>
          {iconTag}
        </div>
        {fieldsTag}
      </div>
      {noDataTag}
    </div>
  );
}
