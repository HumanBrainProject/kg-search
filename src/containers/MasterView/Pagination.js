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
import { windowWidth } from "../../helpers/BrowserHelpers";
import { TabNavEnabler } from "../../helpers/TabNavEnabler";
import "./Pagination.css";

export function PaginationComponent({pageScope, className}) {
  return (
    <TabNavEnabler className={`kgs-paging ${className?className:""}`} containerSelector={className?("." + className):".kgs-paging"} itemSelector={".sk-toggle-option"} activeItemSelector={".is-active"} disabledItemSelector={".is-disabled"} >
      {/* <Component showNumbers={true} pageScope={pageScope} showLast={false} translations={{"pagination.previous": " ", "pagination.next": " "}}/> */}
    </TabNavEnabler>
  );
}
export class Pagination extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      pageScope: this.pageScope
    };
    this.timer = null;
    this.handleResizeEvent = this.handleResizeEvent.bind(this);
  }
  componentDidMount() {
    window.addEventListener("resize", this.handleResizeEvent, false);
  }
  componentWillUnmount() {
    window.removeEventListener("resize", this.handleResizeEvent);
  }
  handleResizeEvent() {
    clearTimeout(this.timer);
    this.timer = setTimeout(() => this.updatePageScope(), 250);
  }
  get pageScope() {
    const width = windowWidth();
    return (width >= 1400)?3:(width >= 1200)?2:(width >= 1050)?1:(width >= 992)?0:(width >= 750)?3:(width >= 650)?2:(width >= 460)?1:0;
  }
  updatePageScope() {
    this.setState(() => ({pageScope: this.pageScope}));
  }
  render() {
    return <PaginationComponent pageScope={this.state.pageScope} className={this.props.className} />;
  }
}