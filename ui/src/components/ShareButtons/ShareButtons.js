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
import PropTypes from "prop-types";
import { CopyToClipboardButton } from "../CopyToClipboard/CopyToClipboardButton";
import EmailToLink from "../EmailToLink/EmailToLink";
import "./ShareButtons.css";

export function ShareButtons({className, clipboardContent, emailToLink}) {
  const classNames = ["kgs-share-links", className].join(" ");
  return (
    <span className={classNames}>
      <span className="kgs-share-links-panel">
        <CopyToClipboardButton icon="clipboard" title="Copy search link to clipboard" confirmationText="search link copied to clipoard" content={clipboardContent} />
        <EmailToLink icon="envelope" title="Send search link by email" link={emailToLink} />
      </span>
    </span>
  );
}

ShareButtons.propTypes = {
  className: PropTypes.string,
  clipboardContent: PropTypes.string,
  emailToLink: PropTypes.string
};

export default ShareButtons;