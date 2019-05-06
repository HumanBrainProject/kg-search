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
import "./CookielawBanner.css";

export class CookielawBanner extends PureComponent {
  constructor(props) {
    super(props);
    this.state = {
      counter: 0
    };
  }
  handleClick = () => {
    this.setState(state => ({counter: state.counter+1}));
  }
  render() {
    const {className, querySelector, cookieKey} = this.props;

    if (!querySelector || typeof querySelector !== "string" || !cookieKey || typeof cookieKey !== "string") {
      return null;
    }

    const content = document.querySelector(querySelector);
    if (!content) {
      return null;
    }

    const inline = content.innerHTML;

    const cookie = "; " + document.cookie;
    const parts = cookie.split("; " + cookieKey + "=");
    if (parts.length === 2) {
      const value = parts.pop().split(";").shift();
      if (value !== "") {
        return null;
      }
    }

    const classNames = ["kgs-cookie", className].join(" ");
    return (
      <div className={classNames} dangerouslySetInnerHTML={{__html: inline}} onClick={this.handleClick} />
    );
  }
}