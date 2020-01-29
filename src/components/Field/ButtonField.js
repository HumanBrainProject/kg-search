/*
*   Copyright (c) 2020, EPFL/Human Brain Project PCO
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
import { ValueField } from "./ValueField";
import "./ButtonField.css";

const ButtonFieldBase = (renderUserInteractions = true) => {
  class ButtonField extends React.Component {
    constructor(props) {
      super(props);
      this.state = {
        showText: false
      };
    }

    onClick = () => this.setState({showText: !this.state.showText});

    render() {
      const {show, items, mapping, group} = this.props;
      if(!show) {
        return null;
      }

      return(
        renderUserInteractions ?
          <React.Fragment>
            <button type="button" className="btn btn-warning kgs-field__button" onClick={this.onClick}>{mapping.tagIcon? mapping.tagIcon:null}{mapping.value}</button>
            <ValueField show={this.state.showText} data={items} mapping={mapping} group={group}/>
          </React.Fragment>:
          <ValueField show={true} data={items} mapping={mapping} group={group}/>
      );
    }
  }
  return ButtonField;
};

export const ButtonField = ButtonFieldBase(true);
export const PrintViewButtonField = ButtonFieldBase(false);