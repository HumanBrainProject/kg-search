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

import * as actionsSearch from "../../actions/actions.search";
import "./TypesFilterPanel.css";

const TypeFilterBase = ({ type: { label, count, active }, onClick }) => (
  <div className={`kgs-fieldsFilter-checkbox ${active?"is-active":""}`} onClick = { onClick } >
    <div className="kgs-fieldsFilter-checkbox__text">{label}</div>
    <div className="kgs-fieldsFilter-checkbox__count">{count}</div>
  </div>
);

class TypeFilter extends React.Component {

    onClick = () => this.props.onClick(this.props.type.type);

    render() {
      return ( <TypeFilterBase type = { this.props.type }
        onClick = { this.onClick }
      />
      );
    }
}

const TypesFilterPanelBase = ({ types, onClick }) => (
  <div className = "kgs-fieldsFilter" >
    <div className = "kgs-fieldsFilter-title" > Categories </div>
    {
      types.map(type =>
        <TypeFilter type = { type }
          key = { type.type }
          onClick = { onClick }
        />
      )
    }
  </div>
);

export const TypesFilterPanel = connect(
  state => ({
    types: state.search.types
      .filter(t => t.count > 0 || t.type === state.search.selectedType)
      .map(t => ({
        ...t,
        active: t.type === state.search.selectedType
      }))
  }),
  dispatch => ({
    onClick: value => dispatch(actionsSearch.setType(value))
  })
)(TypesFilterPanelBase);