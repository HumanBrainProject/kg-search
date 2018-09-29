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
import { ShapeIcon } from "./ShapeIcon";
import { MenuFilter } from "searchkit";
import "./ShapesFilterPanel.css";

// {itemKey, label, count, rawCount, listDocCount, active, disabled, showCount, bemBlocks, onClick}
const ShapeFilter = ({itemKey, label, count, active, disabled, onClick}) => {
  return (
    <div className={`kgs-fieldsFilter__shape${active?" is-active":""}${disabled?" is-disabled":""}`}>
      <button key={itemKey} onClick={onClick} className="kgs-fieldsFilter__button" disabled={disabled}>
        <div>
          <div className="kgs-fieldsFilter__icon">
            <ShapeIcon label={label} shape={itemKey} active={active} />
          </div>
          <div className="kgs-fieldsFilter__label">{label}</div>
          <div className="kgs-fieldsFilter__count">{count} Results</div>
        </div>
      </button>
    </div>
  );
};

export const ShapesFilterPanel = () => {
  return (
    <div className="kgs-fieldsFilter">
      <MenuFilter field={"_type"} id="facet_type" itemComponent={ShapeFilter}/>
    </div>
  );
};