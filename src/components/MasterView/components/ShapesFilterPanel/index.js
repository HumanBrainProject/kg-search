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
  const highlightColor = "#ED5554";
  const defaultColor = "#4D4D4D";
  // {itemKey, label, count, rawCount, listDocCount, active, disabled, showCount, bemBlocks, onClick}
  const itemComponent = ({itemKey, label, count, active, disabled, onClick}) => {
    let iconTag = <div width="100%" height="100%"><svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlnsXlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="40px" height="40px" viewBox="0 0 40 40" enableBackground="new 0 0 40 40" xmlSpace="preserve"><g>
      <line fill="none" stroke={defaultColor} strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" x1="9.25" y1="9.168" x2="31.25" y2="9.168"/>
      <line fill="none" stroke={defaultColor} strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" x1="9.25" y1="31.168" x2="31.25" y2="31.168"/>
      <line fill="none" stroke={defaultColor} strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" x1="9.25" y1="16.168" x2="31.25" y2="16.168"/>
      <line fill="none" stroke={defaultColor} strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" x1="9.25" y1="24.168" x2="31.25" y2="24.168"/>
    </g>
    </svg></div>;
    if (itemKey === "$all") {
      iconTag = <div width="100%" height="100%">
        <svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlnsXlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="40px" height="40px" viewBox="0 0 40 40" enableBackground="new 0 0 40 40" xmlSpace="preserve"><g>
          <line fill="none" stroke={active? highlightColor:defaultColor} strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" x1="9.25" y1="9.168" x2="31.25" y2="9.168"/>
          <line fill="none" stroke={active? highlightColor:defaultColor} strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" x1="9.25" y1="31.168" x2="31.25" y2="31.168"/>
          <line fill="none" stroke={active? highlightColor:defaultColor} strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" x1="9.25" y1="16.168" x2="31.25" y2="16.168"/>
          <line fill="none" stroke={active? highlightColor:defaultColor} strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" x1="9.25" y1="24.168" x2="31.25" y2="24.168"/>
        </g>
        </svg>
      </div>;
    } else {
      const mapping = state.definition.shapeMappings[itemKey];
      if (mapping) {
        if (mapping.image && mapping.image.url) {
          iconTag = <img src={mapping.image.url} alt={label} width="100%" height="100%" />;
        } else if (mapping.icon) {
          let icon = active ? mapping.icon.replace(/#4D4D4D/g, highlightColor): mapping.icon;
          iconTag = <div dangerouslySetInnerHTML={{__html:icon}} width="100%" height="100%" />;
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
