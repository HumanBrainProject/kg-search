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
import { SearchPanel } from "./components/SearchPanel";
import { ShapesFilterPanel } from "./components/ShapesFilterPanel";
import { FiltersPanel } from "./components/FiltersPanel";
import { ResultsHeader } from "./components/ResultsHeader";
import { ResultsPanel } from "./components/ResultsPanel";
import { ResultsFooter } from "./components/ResultsFooter";
import { TermsShortNotice } from "../TermsShortNotice";
import { connect } from "../../store";
import "./styles.css";

const searchPanelRelatedElements = [
  {querySelector: "body>header"},
  {querySelector: "body>header + nav.navbar"},
  {querySelector: "#CookielawBanner", cookieKey: "cookielaw_accepted"}
];

const resultFooterRelatedElements = [
  {querySelector: ".main-content + hr + .container"},
  {querySelector: "footer.footer[role=\"contentinfo\"]"}
];

const MasterViewComponent = () => (
  <div className="kgs-masterView">
    <SearchPanel floatingPosition="top" relatedElements={searchPanelRelatedElements} />
    <TermsShortNotice/>
    <ShapesFilterPanel/>
    <div className="kgs-masterView__panel">
      <FiltersPanel/>
      <div>
        <ResultsHeader/>
        <ResultsPanel/>
      </div>
    </div>
    <ResultsFooter floatingPosition="bottom" relatedElements={resultFooterRelatedElements} />
  </div>
);

export const TabKeyNavigationMasterView = withTabKeyNavigation(
  "isActive",
  ".kgs-masterView",
  null
)(MasterViewComponent);

export const MasterView = connect(
  state => ({
    isActive: !state.hits.currentHit && !state.application.info
  })
)(TabKeyNavigationMasterView);


