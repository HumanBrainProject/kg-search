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

import "./FacetCheckbox.css";

export const FacetCheckbox = ({ item: { label, count, checked, hasAnyChildChecked } }) => (
  <div className={`kgs-facet-checkbox ${checked ? "is-active" : ""}  ${hasAnyChildChecked ? "has-any-child-active" : ""}`}>
    <input type="checkbox" tabIndex="-1" />
    <div className="kgs-facet-checkbox__text">{label}</div>
    <div className="kgs-facet-checkbox__count">{count}</div>
  </div>
);

export default FacetCheckbox;