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
import { isMobile, tabAblesSelectors } from "../../../../Helpers/BrowserHelpers";
import { ShareBar } from "../../../ShareBar";
import { Shape } from "../../../Shape";
import "./styles.css";

export class DetailView extends Component {
  constructor(props) {
    super(props);
    this.componentContext = {
      tabAbles: []
    };
  }
  UNSAFE_componentWillUpdate() {
    this.componentContext.tabAbles.forEach(e => {
      if (e.tabIndex >= 0) {
        e.node.setAttribute("tabIndex", e.tabIndex);
      } else {
        e.node.removeAttribute("tabIndex");
      }
    });
  }
  componentDidUpdate() {
    if (!isMobile) {
      //window.console.debug(new Date().toLocaleTimeString() + ": view=" + this.props.viewId + ", tabs active=" + this.props.isActive);
      if (!this.props.isActive) {
        const rootNode = document.body.querySelector(".kgs-detailView[data-viewId=\"" + this.props.viewId + "\"]");
        this.componentContext.tabAbles = Object.values(rootNode.querySelectorAll(tabAblesSelectors.join(",")))
          .map(node => ({node: node, tabIndex: node.tabIndex}));
        this.componentContext.tabAbles
          .forEach(e => e.node.setAttribute("tabIndex", -1));
      }
    }
  }
  render() {
    const {viewId, data, onPreviousClick, onCloseClick} = this.props;
    return (
      <div className="kgs-detailView" data-type={data && data._type} data-viewId={viewId} >
        {data && <div className="kgs-detailView__outerPanel">
          <div className="kgs-detailView__navigation">
            <div className="kgs-detailView__navigation-panel">
              <button className="kgs-detailView__previousButton" onClick={onPreviousClick}>
                <i className="fa fa-chevron-left" /> Previous
              </button>
              <ShareBar/>
              <button className="kgs-detailView__closeButton" onClick={onCloseClick}>
                <i className="fa fa-close" />
              </button>
            </div>
          </div>
          <div className="kgs-detailView__innerPanel" tabIndex={-1}>
            <Shape data={data} detailViewMode={true} />
          </div>
        </div>}
      </div>
    );
  }
}
