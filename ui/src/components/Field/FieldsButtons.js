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

import React, { useState } from "react";
import ReactPiwik from "react-piwik";

import { Text } from "../Text/Text";
import { CopyToClipboardButton } from "../CopyToClipboard/CopyToClipboardButton";
import { termsOfUse } from "../../data/termsOfUse.js";
import "./FieldsButtons.css";

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

const Download = ({data, showTermsOfUse, isListOfUrl}) => {

  const handleDownload = url => e => {
    e.stopPropagation();
    ReactPiwik.push(["trackLink", url, "download"]);
  };

  return (
    <div className="kgs-download">
      {isListOfUrl ?
        data.map(el => {
          const label = el.value || el.url.split("container=")[1] || el.url;
          return (
            <div className="kgs-download-multiple" key={el.url}>
              <span>
                <FontAwesomeIcon icon="file" />
                <a href={el.url} onClick={handleDownload(el.url)}>{label}</a>
              </span>
            </div>
          );
        })
        :
        <div>
          <span>
            <FontAwesomeIcon icon="file" />&nbsp;
            <a href={data.url} onClick={handleDownload(data.url)}>{data.value}</a>
          </span>
        </div>
      }
      {showTermsOfUse && (
        <Text content={termsOfUse} isMarkdown={true} />
      )}
    </div>
  );
};

const Cite = ({content}) => {
  if (!content) {
    return null;
  }

  return (
    <React.Fragment>
      <CopyToClipboardButton className="kgs-cite-clipboard-button" icon="clipboard" title="Copy text to clipboard" confirmationText="text copied to clipoard" content={content} />
      <div className="kgs-cite">
        <div className="kgs-cite-content">
          <Text content={content} isMarkdown={true} />
        </div>
      </div>
    </React.Fragment>
  );
};

const Button = ({field, onClick, active}) => {

  const handleClick = () => typeof onClick === "function" && onClick(field);

  if (!field) {
    return null;
  }

  const {value, icon} = field.mapping;

  const [name, type] = value.split(" ");
  return (
    <button type="button" className={`btn kgs-fields-buttons__button ${active?"is-active":""}`} onClick={handleClick}>
      <FontAwesomeIcon icon={icon} />&nbsp;{name}{type?(<span>{type}</span>):null}
    </button>
  );

};

const Content = ({onClose, field}) => {

  const handleClose = () => typeof onClose === "function" && onClose();

  if (!field) {
    return null;
  }

  const isListOfUrl = Array.isArray(field.data) && field.data.some(u => u.url);
  const {url, value} = field.data;
  const {termsOfUse} = field.mapping;

  return (
    <div className="kgs-fields-buttons__details">
      <div className="kgs-field-fields-buttons__details__panel">
        {url || isListOfUrl?
          <Download data={field.data} showTermsOfUse={!!termsOfUse} isListOfUrl={isListOfUrl} />
          :
          <Cite content={value} />
        }
        <button className="kgs-field-fields-buttons__close-button" onClick={handleClose} title="close">
          <FontAwesomeIcon icon="times" size="2x" />
        </button>
      </div>
    </div>
  );

};

export const FieldsButtons = ({className, fields}) => {
  const [value, setValue] = useState();

  const handleClick = value => setValue(value);

  if (!fields || !fields.length) {
    return null;
  }
  return (
    <div className={`kgs-fields-buttons ${className?className:""}`}>
      <div className="kgs-fields-buttons__selectors">
        {fields.map(field => (
          <Button key={field.name} field={field} type="button" active={field === value} onClick={handleClick} />
        ))}
      </div>
      <div className="kgs-fields-buttons__content">
        <Content field={value} onClose={handleClick} />
      </div>
    </div>
  );
};
