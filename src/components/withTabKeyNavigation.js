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
import { isMobile, tabAblesSelectors } from "../Helpers/BrowserHelpers";
import { store } from "../store";

export function withTabKeyNavigation(WrappedComponent, isContentActive, rootNodeQuerySelector, excludeNodeQuerySelector) {
  class withTabKey extends PureComponent {
    constructor(props) {
      super(props);
      this.shouldHandleChange = false;
      this.handleChange = this.handleChange.bind(this);
      this.tabAbles = [];
      this.isActive = this.isContentActive();
    }
    componentDidMount() {
      if (!isMobile) {
        this.shouldHandleChange = true;
        document.addEventListener("state", this.handleChange.bind(this), false);
        this.handleChange();
      }
    }
    componentWillUnmount() {
      if (!isMobile) {
        this.shouldHandleChange = false;
        document.removeEventListener("state", this.handleChange);
      }
    }
    isContentActive() {
      const storeState = store.getState();
      const active = isContentActive(storeState, this.props);
      return active;
    }
    handleChange() {
      const isActive = this.isContentActive();
      if (isActive !== this.isActive) {
        this.isActive = isActive;
        if (this.isActive) {

          //window.console.debug(new Date().toLocaleTimeString() + ": enable tabs", this.tabAbles);
          this.tabAbles.forEach(e => {
            if (e.tabIndex >= 0) {
              e.node.setAttribute("tabIndex", e.tabIndex);
            } else {
              e.node.removeAttribute("tabIndex");
            }
          });

        } else {

          const excludeNode = excludeNodeQuerySelector?document.body.querySelector(excludeNodeQuerySelector):null;
          const rootNode = rootNodeQuerySelector?document.body.querySelector(rootNodeQuerySelector):document.body;
          if (rootNode) {
            this.tabAbles = Object.values(rootNode.querySelectorAll(tabAblesSelectors.join(",")))
              .filter(e => !excludeNode || !excludeNode.contains(e))
              .map(node => ({node: node, tabIndex: node.tabIndex}));

            //window.console.debug(new Date().toLocaleTimeString() + ": masterView disable tabs", this.tabAbles);
            this.tabAbles.forEach(e => e.node.setAttribute("tabIndex", -1));
          }
        }
      }
    }
    render() {
      return <WrappedComponent {...this.props} />;
    }
  }
  return withTabKey;
}