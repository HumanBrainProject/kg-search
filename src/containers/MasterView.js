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
import { connect } from "react-redux";
import { withTabKeyNavigation } from "../helpers/withTabKeyNavigation";
import { SearchPanel } from "./MasterView/SearchPanel";
import { ShapesFilterPanel } from "./MasterView/ShapesFilterPanel";
import { FiltersPanel } from "./MasterView/FiltersPanel";
import { ResultsHeader } from "./MasterView/ResultsHeader";
import { HitsPanel } from "./MasterView/HitsPanel";
import { Footer } from "./MasterView/Footer";
import { TermsShortNotice } from "./TermsShortNotice";
import "./MasterView.css";

const MasterViewBase = () => ( <
  div className = "kgs-masterView" >
  <
    SearchPanel / >
  <
    TermsShortNotice className = "kgs-masterView__terms-short-notice" / >
  <
    ShapesFilterPanel / >
  <
    div className = "kgs-masterView__panel" >
    <
      FiltersPanel / >
    <
      div className = "kgs-masterView__main" >
      <
        ResultsHeader / >
      <
        HitsPanel / >
    <
    /div> <
  /div> <
    Footer / >
<
/div>
);

export const MasterViewWithTabKeyNavigation = withTabKeyNavigation(
  "isActive",
  ".kgs-masterView",
  null
)(MasterViewBase);

export const MasterView = connect(
  state => ({
    isActive: !state.instances.currentInstance && !state.application.info
  })
)(MasterViewWithTabKeyNavigation);