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
import * as actionsSearch from "../../actions/actions.search";
import { TypeIcon } from "./TypeIcon";

import "./TypesFilterPanel.css";

// {itemKey, label, count, rawCount, listDocCount, active, disabled, showCount, bemBlocks, onClick}
const TypeFilterBase = ({ type: { type, label, count, active, disabled }, onClick }) => (
  <div className = { `kgs-fieldsFilter__type${active ? " is-active" : ""}${disabled ? " is-disabled" : ""}` } >
    <button onClick = { onClick }
      className = "kgs-fieldsFilter__button"
      disabled = { disabled || active } >
      <div >
        <div className = "kgs-fieldsFilter__icon" >
          <TypeIcon label = { label }
            type = { type }
            active = { active }
          />
        </div>
        <div className = "kgs-fieldsFilter__label" > { label } </div>
        <div className = "kgs-fieldsFilter__count" > { `${count ? count : 0} ${count && count > 1 ? "Results" : "Result"}` } </div>
      </div>
    </button>
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

class TypesFilterPanelBase extends React.Component {
  componentDidMount() {
    const { selectedType, location } = this.props;
    const url = getUpdatedUrl("facet_type[0]", true, selectedType, false, location);
    history.push(url);
  }

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
            <TypeFilter type = { type }
              key = { type.type }
              onClick = { onClick }
            />
          )
        }
      </div>
    );
  }
}

export const TypesFilterPanel = connect(
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
    onClick: value => dispatch(actionsSearch.setType(value))
  })
)(TypesFilterPanelBase);