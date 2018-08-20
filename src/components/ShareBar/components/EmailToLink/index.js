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
import "./styles.css";

function EmailToLink(props) {

  let title = "Send search link by email";
  if (props.title) {
    title = props.title;
  }

  let iconClassName = "fa fa-envelope-o";
  if (props.icon) {
    iconClassName = props.icon;
  }

  let icon = null;
  let text = null;
  if (props.text) {
    if (props.icon) {
      icon = <i className={iconClassName}></i>;
    }
    text = <span>{props.text}</span>;
  } else {
    icon = <i className={iconClassName}></i>;
  }
  return <a className="kgs-email-link" href={props.emailToLink} title={title}>{icon}{text}</a>;
}


export class EmailToLinkContainer extends PureComponent {
  constructor(props) {
    super(props);
    this.state = {
      emailToLink: this.getEmailToLinkFromBrowserLocation()
    };
    this.shouldhandleStateChange = false;
  }
  getEmailToLinkFromBrowserLocation() {
    const to= "";
    const subject= "Knowledge Graph Search Request";
    const body = "Please have a look to the following Knowledge Graph search request";
    return `mailto:${to}?subject=${subject}&body=${body} ${escape(window.location.href)}.`;
  }
  handleStateChange() {
    setTimeout(() => {
      this.shouldhandleStateChange && this.setState({
        emailToLink: this.getEmailToLinkFromBrowserLocation()
      });
    }, 250);
  }
  componentDidMount() {
    this.shouldhandleStateChange = true;
    document.addEventListener("state", this.handleStateChange.bind(this), false);
    this.handleStateChange();
  }
  componentWillUnmount() {
    this.shouldhandleStateChange = false;
    document.removeEventListener("state", this.handleStateChange);
  }
  render() {
    return (
      <EmailToLink emailToLink={this.state.emailToLink} icon={this.props.icon} title={this.props.title} />
    );
  }
}