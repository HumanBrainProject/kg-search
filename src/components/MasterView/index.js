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
import { withTabKeyNavigation } from "../withTabKeyNavigation";
import { Layout, LayoutBody, LayoutResults } from "searchkit";
import { SearchPanel } from "./components/SearchPanel";
import { ShapesFilterPanel } from "./components/ShapesFilterPanel";
import { FiltersPanel } from "./components/FiltersPanel";
import { ResultsHeader } from "./components/ResultsHeader";
import { ResultsPanel } from "./components/ResultsPanel";
import { ResultsFooter } from "./components/ResultsFooter";
import { TermsShortNotice } from "../TermsShortNotice";
import "./styles.css";

const MasterViewComponent = () => {
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
        <SearchPanel floatingPosition="top" relatedElements={searchPanelRelatedElements} />
        <TermsShortNotice/>
        <ShapesFilterPanel/>
        <LayoutBody>
          <FiltersPanel/>
          <LayoutResults>
            <ResultsHeader/>
            <ResultsPanel/>
            <ResultsFooter floatingPosition="bottom" relatedElements={resultFooterRelatedElements} />
          </LayoutResults>
        </LayoutBody>
      </Layout>
    </div>
  );
};

export const MasterView = withTabKeyNavigation(
  MasterViewComponent,
  data => !data.hits.currentHit,
  ".kgs-masterView",
  null
);


