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

import { Text } from "./Text";
import { CopyToClipboardButton } from "./CopyToClipboardButton";
import { termsOfUse } from "../data/termsOfUse.js";


import "./FieldsButtons.css";

class Download extends React.PureComponent {

  handleClick = () => {
    const {field, onClick} = this.props;

    typeof onClick === "function" && onClick(field);
  };

  render() {
    const {url, label} = this.props;

    if (!url) {
      return null;
    }

    return (
      <div className="kgs-download">
        <div><span><i className="fa fa-2x fa-file-o"></i><a href={url}>{label}</a></span></div>
        <Text content={termsOfUse} isMarkdown={true} />
      </div>
    );
  }
}

class Cite extends React.PureComponent {

  render() {
    const {content} = this.props;

    if (!content) {
      return null;
    }

    return (
      <div className="kgs-cite">
        <div className="kgs-cite-content">
          <i className="fa fa-quote-left"></i>
          <Text content={content} isMarkdown={true} />
          <i className="fa fa-quote-right"></i>
        </div>
        <CopyToClipboardButton className="kgs-cite-clipboard-button" icon="fa fa-2x fa-clipboard" title="Copy text to clipboard" confirmationText="text copied to clipoard" content={content} />
      </div>
    );
  }
}

class Button extends React.PureComponent {

  handleClick = () => {
    const {field, onClick} = this.props;

    typeof onClick === "function" && onClick(field);
  };

  render() {
    const {field, active} = this.props;

    if (!field) {
      return null;
    }

    const {value} = field.mapping;

    const [name, type] = value.split(" ");

    const {url} = field.data;
    const icon = <i className={`fa ${url?"fa-download":"fa-quote-left"}`}></i>;

    return (
      <button type="button" className={`btn kgs-fields-buttons__button ${active?"is-active":""}`} onClick={this.handleClick}>{icon}{name}{type?(<span>{type}</span>):null}</button>
    );
  }
}

class Content extends React.PureComponent {

  handleClose = () => {
    const {onClose} = this.props;

    typeof onClose === "function" && onClose();
  };

  render() {
    const {field } = this.props;

    if (!field) {
      return null;
    }

    const {url, value} = field.data;

    return (
      <div className="kgs-fields-buttons__details">
        <div className="kgs-field-fields-buttons__details__panel">
          {url?
            <Download url={url} label={value} />
            :
            <Cite content={value} />
          }
          <button className="kgs-field-fields-buttons__close-button" onClick={this.handleClose} title="close"><i className="fa fa-2x fa-close"></i></button>
        </div>
      </div>
    );
  }
}

export class FieldsButtons extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      value: null
    };
  }

  handleClick = value => {
    this.setState({value: value});
  }

  render() {
    const {className, fields} = this.props;
    if (!fields || !fields.length) {
      return null;
    }
    return (
      <div className={`kgs-fields-buttons ${className?className:""}`}>
        <div className="kgs-fields-buttons__selectors">
          {fields.map(field => (
            <Button key={field.name} field={field} type="button" active={field === this.state.value} onClick={this.handleClick} />
          ))}
        </div>
        <div className="kgs-fields-buttons__content">
          <Content field={this.state.value} onClose={this.handleClick} />
        </div>
      </div>
    );
  }
}
