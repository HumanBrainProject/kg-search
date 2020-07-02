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
import ReactPiwik from "react-piwik";

import { Text } from "../Text/Text";
import { CopyToClipboardButton } from "../CopyToClipboard/CopyToClipboardButton";
import { termsOfUse } from "../../data/termsOfUse.js";


import "./FieldsButtons.css";

class Download extends React.PureComponent {

  handleClick = () => {
    const {field, onClick} = this.props;

    typeof onClick === "function" && onClick(field);
  };

  handleDownload = url => e => {
    e.stopPropagation();
    ReactPiwik.push(["trackLink", url, "download"]);
  }

  render() {
    const {data, showTermsOfUse} = this.props;
    return (
      <div className="kgs-download">
        {
          data.map(el => {
            const label = el.value || el.url.split("container=")[1] || el.url;
            return (
              <div className="kgs-download-multiple" key={el.url}>
                <span>
                  <i className="fa fa-file-o"></i>
                  <a href={el.url} onClick={this.handleDownload(el.url)}>{label}</a>
                </span>
              </div>
            );
          })
        }
        {showTermsOfUse && (
          <Text content={termsOfUse} isMarkdown={true} />
        )}
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
      <React.Fragment>
        <CopyToClipboardButton className="kgs-cite-clipboard-button" icon="fa fa-clipboard" title="Copy text to clipboard" confirmationText="text copied to clipoard" content={content} />
        <div className="kgs-cite">
          <div className="kgs-cite-content">
            <Text content={content} isMarkdown={true} />
          </div>
        </div>
      </React.Fragment>
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
    const isListOfUrl = Array.isArray(field.data) && field.data.some(u => u.url);
    const icon = <i className={`fa ${isListOfUrl?"fa-download":"fa-quote-left"}`}></i>;

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

    const isListOfUrl = Array.isArray(field.data) && field.data.some(u => u.url);
    const {value} = field.data;
    const {termsOfUse} = field.mapping;

    return (
      <div className="kgs-fields-buttons__details">
        <div className="kgs-field-fields-buttons__details__panel">
          {isListOfUrl?
            <Download data={field.data} showTermsOfUse={!!termsOfUse} />
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
