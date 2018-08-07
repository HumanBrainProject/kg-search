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
import { store } from "../../../../store";
import { MenuFilter } from "searchkit";
import "./styles.css";

export function ShapesFilterPanel() {

  const state = store.getState();

  // {itemKey, label, count, rawCount, listDocCount, active, disabled, showCount, bemBlocks, onClick}
  const itemComponent = ({itemKey, label, count, rawCount, listDocCount, active, disabled, showCount, bemBlocks, onClick}) => {

    let iconTag = <div width="100%" height="100%"><i className="fa fa-tag fa-3x" /></div>;
    if (itemKey === "$all") {
      iconTag = <div width="100%" height="100%"><i className="fa fa-bars fa-3x" /></div>;
    } else {
      const mapping = state.configuration.shapeMappings[itemKey];
      if (mapping) {
        if (mapping.image && mapping.image.url) {
          iconTag = <img src={mapping.image.url} alt={label} width="100%" height="100%" />;
        } else if (mapping.icon) {
          iconTag = <div dangerouslySetInnerHTML={{__html: mapping.icon}} width="100%" height="100%" />;
        }
      }
    }

    return  <div className={`kgs-shape${active?" is-active":""}${disabled?" is-disabled":""}`}>
      <button key={itemKey} onClick={onClick} className="kgs-shape__button" disabled={disabled}>
        <div>
          <div className="kgs-shape__icon">{iconTag}</div>
          <div className="kgs-shape__label">{label}</div>
          <div className="kgs-shape__count">{count} Results</div>
        </div>
      </button>
    </div>;
  };

  return (
    <div className="kgs-shapesFilter">
      <MenuFilter field={"_type"} id="facet_type" itemComponent={itemComponent}/>
    </div>
  );
}
