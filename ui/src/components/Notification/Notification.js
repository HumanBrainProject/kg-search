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
import showdown from "showdown";
/*import FilterXSS from 'xss';*/
import xssFilter from "showdown-xss-filter";
import "./Notification.css";

const converter = new showdown.Converter({extensions: [xssFilter]});

export const Notification = ({className, show, text}) => {
  if (!show || !text) {
    return null;
  }

  const html = converter.makeHtml(text);
  const classNames = `kgs-notification ${className?className:""}`;
  return (
    <div className={classNames}>
      <i className="fa fa-warning fa-2x kgs-notification_warning-icon"></i>
      <span className="kgs-notification_text" dangerouslySetInnerHTML={{__html:html}} />
    </div>
  );
};

Notification.propTypes = {
  className: PropTypes.string,
  show: PropTypes.number,
  text: PropTypes.string
};

export default Notification;