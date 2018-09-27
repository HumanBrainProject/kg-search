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
import "./FiltersPanel.css";

export const FiltersPanel = ({className, show, hasFilters, facets, facetComponent, onReset}) => {
  if (!show) {
    return null;
  }
  const classNames = ["kgs-filters", className].join(" ");
  const Facet = facetComponent;
  return (
    <div className={classNames}>
      <span>
        <div className="kgs-filters__header">
          <div className="kgs-filters__title">Filters</div>
          {hasFilters && (
            <div className="kgs-filters__reset"><button type="button" className="kgs-filters__reset-button" onClick={onReset}>Reset</button></div>
          )}
        </div>
        <span>
          {facets.map(f => (
            <Facet key={f.id} id={f.id} name={f.name} facet={f.facet} isVisible={f.isVisible} />
          ))}
        </span>
        {!hasFilters && (
          <span className="kgs-filters__no-filters">No filters available for your current search.</span>
        )}
      </span>
    </div>
  );
};