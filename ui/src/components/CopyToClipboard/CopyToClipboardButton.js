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

import React from "react";
import PropTypes from "prop-types";
import "./CopyToClipboardButton.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

export const CopyToClipboardButton = props => {

  const clickHandler = (event) => {

    const div = document.createElement("div");
    div.innerHTML = props.content;

    const content = div.innerText;

    const textArea = document.createElement("textarea");
    textArea.style.position = "absolute";
    textArea.style.top = 0;
    textArea.style.left = 0;
    textArea.style.width = "2em";
    textArea.style.height = "2em";
    textArea.style.padding = 0;
    textArea.style.margin = 0;
    textArea.style.border = "none";
    textArea.style.outline = "none";
    textArea.style.boxShadow = "none";
    textArea.style.background = "transparent";
    textArea.style.color = "transparent";

    textArea.value = content;

    const button = event.currentTarget;

    button.appendChild(textArea);

    textArea.select();

    try {
      document.execCommand("copy");

      button.setAttribute("show", "true");
      setTimeout(() => button.removeAttribute("show"), 1000);

    } catch (e) {
      //window.console.debug("could not run execCommand copy");
    }

    button.removeChild(textArea);
  };

  let title = "Send to clipboard";
  if (props.title) {
    title = props.title;
  }

  let confirmationText = "sent to clipoard";
  if (props.confirmationText) {
    confirmationText = props.confirmationText;
  }

  let iconClassName = "clipboard";
  if (props.icon) {
    iconClassName = props.icon;
  }

  let icon = null;
  let text = null;
  if (props.text) {
    if (props.icon) {
      icon = <FontAwesomeIcon icon={iconClassName} />;
    }
    text = <span>{props.text}</span>;
  } else {
    icon = <FontAwesomeIcon icon={iconClassName} />;
  }

  const classNames = ["kgs-copy-to-clipboard", props.className].join(" ");

  return (
    <span className={classNames}>
      <button role="link" onClick={clickHandler} title={title}>{icon}{text}</button>
      <div className="kgs-copy-confirmation">{confirmationText}</div>
    </span>
  );
};

CopyToClipboardButton.propTypes = {
  content: PropTypes.string,
  title: PropTypes.string,
  confirmationText: PropTypes.string,
  icon: PropTypes.string
};

export default CopyToClipboardButton;