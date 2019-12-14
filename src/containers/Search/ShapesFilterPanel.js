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

import { history } from "../../store";
import { getUpdatedUrl } from "../../helpers/BrowserHelpers";
import * as actions from "../../actions";
import { ShapeIcon } from "./ShapeIcon";

import "./ShapesFilterPanel.css";

// {itemKey, label, count, rawCount, listDocCount, active, disabled, showCount, bemBlocks, onClick}
const ShapeFilterBase = ({ type: { type, label, count, active, disabled }, onClick }) => (
  <div className = { `kgs-fieldsFilter__shape${active ? " is-active" : ""}${disabled ? " is-disabled" : ""}` } >
    <button onClick = { onClick }
      className = "kgs-fieldsFilter__button"
      disabled = { disabled || active } >
      <div >
        <div className = "kgs-fieldsFilter__icon" >
          <ShapeIcon label = { label }
            shape = { type }
            active = { active }
          />
        </div>
        <div className = "kgs-fieldsFilter__label" > { label } </div>
        <div className = "kgs-fieldsFilter__count" > { `${count ? count : 0} ${count && count > 1 ? "Results" : "Result"}` } </div>
      </div>
    </button>
  </div>
);

class ShapeFilter extends React.Component {

    onClick = () => this.props.onClick(this.props.type.type);

    render() {
      return ( <ShapeFilterBase type = { this.props.type }
        onClick = { this.onClick }
      />
      );
    }
}

class ShapesFilterPanelBase extends React.Component {

  componentDidUpdate(prevProps) {
    const { selectedType, location } = this.props;
    if (selectedType !== prevProps.selectedType) {
      const url = getUpdatedUrl("facet_type[0]", true, selectedType, false, location);
      history.push(url);
    }
  }

  render() {
    const { types, onClick } = this.props;
    return (
      <div className = "kgs-fieldsFilter" >
        {
          types.map(type =>
            <ShapeFilter type = { type }
              key = { type.type }
              onClick = { onClick }
            />
          )
        }
      </div>
    );
  }
}

export const ShapesFilterPanel = connect(
  state => ({
    selectedType: state.search.selectedType,
    types: state.search.types
      .filter(t => t.count > 0 || t.type === state.search.selectedType)
      .map(t => ({
        ...t,
        active: t.type === state.search.selectedType
      })),
    group: state.groups.group,
    location: state.router.location
  }),
  dispatch => ({
    onClick: value => dispatch(actions.setType(value))
  })
)(ShapesFilterPanelBase);