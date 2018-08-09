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

import React, { Component } from "react";
import "./styles.css";

export class SignInButton extends Component {
  constructor(props) {
    super(props);
    this.buttonRef = null;
    this.setButtonRef = element => {
      this.buttonRef = element;
    };
  }
  render() {
    const { show, onClick } = this.props;
    const button = <a ref={this.setButtonRef} href={onClick}>Log in</a>;
    return (
      show?<div ref={this.setButtonRef} className="kgs-sign-in">{button}</div>:null
    );
  }
}