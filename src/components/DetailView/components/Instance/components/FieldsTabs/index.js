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
import React, { PureComponent } from "react";
import { Field } from "../../../../../Field";
import { FieldHint } from "../../../../../Field/components/FieldHint";
import "./styles.css";

const Tab = ({name, title, counter, hint, active, onClick}) => {
  const handleClick = () => {
    typeof onClick === "function" && onClick(name);
  };
  const className = `kgs-field__tab ${active?"is-active":""}`;
  return (
    <button type="button" className={className} onClick={handleClick}>{title} {counter?`(${counter})`:""} <FieldHint {...hint} /></button>
  );
};

const FieldsTabsComponent = ({className, tabs, field, onClick}) => {
  if (!Array.isArray(tabs) || !field) {
    return null;
  }

  return (
    <div className={className}>
      <div className="kgs-fields__tabs">
        {tabs.map((tab, index) => (
          <Tab key={tab.name?tab.name:index} {...tab} active={tab.name === field.name} onClick={onClick} />
        ))}
      </div>
      <div className="kgs-fields__tab__content">
        <Field key={field.name} {...field}  renderUserInteractions={true} />
      </div>
    </div>
  );
};


export class FieldsTabs extends PureComponent {
  constructor(props) {
    super(props);
    const {fields} = props;
    const name = (Array.isArray(fields) && fields.length && fields[0])?fields[0].name:null;
    this.state = {
      tabs: this.getTabs(fields),
      field: this.getField(name, fields)
    };
    this.handleClick = this.handleClick.bind(this);
  }
  getTabs(fields) {
    if (!Array.isArray(fields)) {
      return [];
    }
    return fields.map(field => {
      return {
        name: field.name,
        title: (field.mapping && field.mapping.value)?field.mapping.value:field.name,
        counter: Array.isArray(field.data)?field.data.length:0,
        hint: field.mapping?{
          show: !!field.mapping.value && !!field.mapping.hint,
          value: field.mapping.hint,
          label: field.mapping.value
        }:null
      };
    });
  }
  getField(name, fields) {
    let field = null;
    Array.isArray(fields) && fields.some(f => {
      if (f.name === name) {
        field = f;
        return true;
      }
      return false;
    });
    return field;
  }
  handleClick(name) {
    this.setState({
      field: this.getField(name, this.props.fields)
    });
  }
  render() {
    const {className, fields} = this.props;
    if (!fields || !fields.length) {
      return null;
    }

    return (
      <FieldsTabsComponent className={className} tabs={this.state.tabs} field={this.state.field} onClick={this.handleClick} />
    );
  }
}

