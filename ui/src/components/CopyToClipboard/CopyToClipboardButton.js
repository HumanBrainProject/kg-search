/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

import "./CopyToClipboardButton.css";
import {faClipboard} from "@fortawesome/free-solid-svg-icons/faClipboard";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React from "react";

const ButtonContent = ({icon, text}) => {
  if (text) {
    if (icon) {
      return (
        <React.Fragment>
          <FontAwesomeIcon icon={icon} />
          <span>{text}</span>
        </React.Fragment>
      );
    }
    return <span>{text}</span>;
  }
  return <FontAwesomeIcon icon={icon??faClipboard} />;
};

const CopyToClipboardButton = ({
  className,
  icon,
  text,
  title="Send to clipboard",
  confirmationText="sent to clipoard",
  content
}) => {

  const clickHandler = (event) => {

    const div = document.createElement("div");
    div.innerHTML = content;

    navigator.clipboard.writeText(div.innerText);

    const button = event.currentTarget;

    button.setAttribute("show", "true");
    setTimeout(() => button.removeAttribute("show"), 1000);

  };

  return (
    <span className={`kgs-copy-to-clipboard ${className?className:""}`}>
      <button role="link" onClick={clickHandler} title={title}>
        <ButtonContent icon={icon} text={text} />
      </button>
      <div className="kgs-copy-confirmation">{confirmationText}</div>
    </span>
  );
};

export default CopyToClipboardButton;