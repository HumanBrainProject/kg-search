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
import { store } from "./store";
import AppManager from "./app.manager";
import { isMobile, isFirefox, tabAblesSelectors } from "./Helpers/BrowserHelpers";
import { SearchkitProvider } from "searchkit";
import { MasterView } from "./components/MasterView";
import { DetailViewManager } from "./components/DetailViewManager";
import { TermsShortNotice } from "./components/TermsShortNotice";
import { FetchingPanel } from "./components/FetchingPanel";
import { ErrorPanel } from "./components/ErrorPanel";

export class Views extends PureComponent {
  constructor(props) {
    super(props);
    this.state = store.getState();
    this.tabAbles = [];
    this.isActive = true;
  }
  getState() {
    const globalState = store.getState();
    return {
      isReady: globalState.application.isReady
    };
  }
  handleStateChange() {
    if (!isMobile) {
      this.handleTabAbles();
    }
    setTimeout(() => {
      const nextState = this.getState();
      this.setState(nextState);
    });
  }
  handleTabAbles() {
    const state = store.getState();
    const isActive = !state.hits.currentHit;
    if (isActive !== this.isActive) {
      this.isActive = isActive;
      if (this.isActive) {

        //window.console.debug(new Date().toLocaleTimeString() + ": app enable tabs=" + this.tabAbles.length);
        this.tabAbles.forEach(e => {
          if (e.tabIndex >= 0) {
            e.node.setAttribute("tabIndex", e.tabIndex);
          } else {
            e.node.removeAttribute("tabIndex");
          }
        });

      } else {

        const rootNode = document.body.querySelector(".kgs-app");
        this.tabAbles = Object.values(document.body.querySelectorAll(tabAblesSelectors.join(",")))
          .filter(e => !rootNode.contains(e))
          .map(node => ({node: node, tabIndex: node.tabIndex}));

        //window.console.debug(new Date().toLocaleTimeString() + ": app disable tabs=" + this.tabAbles.length);
        this.tabAbles.forEach(e => e.node.setAttribute("tabIndex", -1));

      }
    }
  }
  componentDidMount() {
    document.addEventListener("state", this.handleStateChange.bind(this), false);
    this.handleStateChange();
  }
  componentWillUnmount() {
    document.removeEventListener("state", this.handleStateChange);
  }
  get searchkit() {
    return this.props.manager && this.props.manager.searchkit;
  }
  render() {
    if (!this.state.isReady) {
      return null;
    }
    if (!this.searchkit) {
      window.console.error("application failed to instanciate searchkit");
      return null;
    }
    //window.console.debug("Views rendering...");
    return (
      <span>
        <SearchkitProvider searchkit={this.searchkit}>
          <MasterView />
        </SearchkitProvider>
        <DetailViewManager/>
      </span>
    );
  }
}

export class App extends PureComponent {
  constructor(props) {
    super(props);
    this.manager = new AppManager(props.config || {});
  }
  componentDidMount() {
    if (!isMobile) {
      if (isFirefox) {
        document.body.setAttribute("isFirefox", true);
      }
    }
    this.manager.start();
  }
  componentWillUnmount() {
    this.manager.stop();
  }
  render() {
    //window.console.debug("App rendering...");
    return (
      <div className="kgs-app">
        <Views manager={this.manager} />
        <TermsShortNotice/>
        <FetchingPanel/>
        <ErrorPanel/>
      </div>
    );
  }
}

export default App;