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

const defaultSvg = color => `
  <svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlnsXlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="40px" height="40px" viewBox="0 0 40 40" enableBackground="new 0 0 40 40" xmlSpace="preserve">
    <g>
      <line fill="none" stroke="${color}" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" x1="9.25" y1="9.168" x2="31.25" y2="9.168"/>
      <line fill="none" stroke="${color}" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" x1="9.25" y1="31.168" x2="31.25" y2="31.168"/>
      <line fill="none" stroke="${color}" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" x1="9.25" y1="16.168" x2="31.25" y2="16.168"/>
      <line fill="none" stroke="${color}" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" x1="9.25" y1="24.168" x2="31.25" y2="24.168"/>
    </g>
  </svg>
`;

const allSvg = color => `
  <svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlnsXlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="40px" height="40px" viewBox="0 0 40 40" xmlSpace="preserve">
    <g>
      <line fill="none" stroke="${color}" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" x1="9.25" y1="9.168" x2="31.25" y2="9.168"/>
      <line fill="none" stroke="${color}" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" x1="9.25" y1="31.168" x2="31.25" y2="31.168"/>
      <line fill="none" stroke="${color}" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" x1="9.25" y1="16.168" x2="31.25" y2="16.168"/>
      <line fill="none" stroke="${color}" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" x1="9.25" y1="24.168" x2="31.25" y2="24.168"/>
    </g>
  </svg>
`;

const replaceColorInSvg = (icon, colorToReplace, replacementColor) => {
  if (typeof icon !== "string" || typeof colorToReplace !== "string" || typeof replacementColor !== "string") {
    return icon;
  }
  const reg = new RegExp(colorToReplace, "g");
  return icon.replace(reg, replacementColor);
};

const IconComponent = ({label, imageUrl, icon}) => {
  if (imageUrl) {
    return (
      <img src={imageUrl} alt={label} width="100%" height="100%" />
    );
  }

  if (icon) {
    return (
      <div dangerouslySetInnerHTML={{__html:icon}} width="100%" height="100%" />
    );
  }

  return null;
};

const Icon = ({label, shape, active}) => {

  const highlightColor = "#ED5554";
  const defaultColor = "#4D4D4D";

  let imageUrl = null;
  let icon = null;

  if (shape === "$all") {
    icon = allSvg(active?highlightColor:defaultColor);
  } else {
    const state = store.getState();
    const mapping = state.definition.shapeMappings[shape];
    if (mapping) {
      if (mapping.image && mapping.image.url) {
        imageUrl == mapping.image.url;
      } else if (mapping.icon) {
        icon = active?replaceColorInSvg(mapping.icon, defaultColor, highlightColor):mapping.icon;
      } else {
        icon = defaultSvg(active?highlightColor:defaultColor);
      }
    } else {
      icon = defaultSvg(active?highlightColor:defaultColor);
    }
  }

  const iconProps = {
    label: label,
    imageUrl: imageUrl,
    icon: icon
  };

  return (
    <IconComponent {...iconProps} />
  );
};

// {itemKey, label, count, rawCount, listDocCount, active, disabled, showCount, bemBlocks, onClick}
const itemComponent = ({itemKey, label, count, active, disabled, onClick}) => {
  return (
    <div className={`kgs-shapesFilter__shape${active?" is-active":""}${disabled?" is-disabled":""}`}>
      <button key={itemKey} onClick={onClick} className="kgs-shapesFilter__button" disabled={disabled}>
        <div>
          <div className="kgs-shapesFilter__icon">
            <Icon label={label} shape={itemKey} active={active} />
          </div>
          <div className="kgs-shapesFilter__label">{label}</div>
          <div className="kgs-shapesFilter__count">{count} Results</div>
        </div>
      </button>
    </div>
  );
};

export function ShapesFilterPanel() {
  return (
    <div className="kgs-shapesFilter">
      <MenuFilter field={"_type"} id="facet_type" itemComponent={itemComponent}/>
    </div>
  );
}
