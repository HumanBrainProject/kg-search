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
import { connect } from "../../../../store";
import * as actions from "../../../../actions";
import { Select } from "../../../Select";
import "./styles.css";

const GroupSelectionPanelComponent = ({value, list, onChange}) => {
  if (!Array.isArray(list) || list.length <= 1) {
    return null;
  }
  return (
    <div className="kgs-group-selection">
      <Select label="Group" value={value} list={list} onChange={onChange} />
    </div>
  );
};

export const GroupSelectionPanel = connect(
  state => ({
    value: state.search.index,
    list: state.indexes.indexes?state.indexes.indexes:[]
  }),
  dispatch => ({
    onChange: value => dispatch(actions.setIndex(value))
  })
)(GroupSelectionPanelComponent);
