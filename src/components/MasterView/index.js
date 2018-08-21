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

import React, { PureComponent }from "react";
import { store } from "../../store";
import { Layout, LayoutBody, LayoutResults } from "searchkit";
import { isMobile, tabAblesSelectors } from "../../Helpers/BrowserHelpers";
import { SearchPanel } from "./components/SearchPanel";
import { ShapesFilterPanel } from "./components/ShapesFilterPanel";
import { FiltersPanel } from "./components/FiltersPanel";
import { ResultsHeader } from "./components/ResultsHeader";
import { ResultsPanel } from "./components/ResultsPanel";
import { ResultsFooter } from "./components/ResultsFooter";
import { TermsShortNotice } from "../TermsShortNotice";
import "./styles.css";

export class MasterView extends PureComponent {
  constructor(props) {
    super(props);
    this.tabAbles = [];
    this.isActive = true;
  }
  handleStateChange() {
    const state = store.getState();
    const isActive = !state.hits.currentHit;
    if (isActive !== this.isActive) {
      this.isActive = isActive;
      if (this.isActive) {

        //window.console.debug(new Date().toLocaleTimeString() + ": masterView enable tabs=" + this.tabAbles.length);
        this.tabAbles.forEach(e => {
          if (e.tabIndex >= 0) {
            e.node.setAttribute("tabIndex", e.tabIndex);
          } else {
            e.node.removeAttribute("tabIndex");
          }
        });

      } else {

        const rootNode = document.body.querySelector(".kgs-masterView");
        this.tabAbles = Object.values(rootNode.querySelectorAll(tabAblesSelectors.join(",")))
          .map(node => ({node: node, tabIndex: node.tabIndex}));

        //window.console.debug(new Date().toLocaleTimeString() + ": masterView disable tabs=" + this.tabAbles.length);
        this.tabAbles.forEach(e => e.node.setAttribute("tabIndex", -1));

      }
    }
  }
  componentDidMount() {
    //window.console.debug("MasterView mount");
    if (!isMobile) {
      document.addEventListener("state", this.handleStateChange.bind(this), false);
      this.handleStateChange();
    }
  }
  componentWillUnmount() {
    if (!isMobile) {
      document.removeEventListener("state", this.handleStateChange);
    }
  }
  render() {

    const searchPanelRelatedElements = [
      {querySelector: "body>header"},
      {querySelector: "body>header + nav.navbar"},
      {querySelector: "#CookielawBanner", cookieKey: "cookielaw_accepted"}
    ];

    const resultFooterRelatedElements = [
      {querySelector: ".sk-layout__body + .terms-short-notice", localStorageKey: "hbp.kgs-terms-conditions-consent"},
      {querySelector: ".main-content + hr + .container"},
      {querySelector: "footer.footer[role=\"contentinfo\"]"}
    ];
    //window.console.debug("MasterView rendering...");
    return (
      <div className="kgs-masterView">
        <Layout>
          <SearchPanel relatedElements={searchPanelRelatedElements} />
          <TermsShortNotice/>
          <ShapesFilterPanel/>
          <LayoutBody>
            <FiltersPanel/>
            <LayoutResults>
              <ResultsHeader/>
              <ResultsPanel/>
              <ResultsFooter relatedElements={resultFooterRelatedElements} />
            </LayoutResults>
          </LayoutBody>
        </Layout>
      </div>
    );
  }
}