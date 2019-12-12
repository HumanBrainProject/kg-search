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
import * as actions from "../../actions";
import { Select } from "../../components/Select";
import { ElasticSearchHelpers } from "../../helpers/ElasticSearchHelpers";
import { getUpdatedUrl } from "../../helpers/BrowserHelpers";
import { history } from "../../store";

class SortingSelectorComponent extends React.Component {

  componentDidUpdate(prevProps) {
    const { param, location } = this.props;
    if (param !== prevProps.param) {
      const url = getUpdatedUrl("sort", true, param, false, location);
      history.push(url);
    }
  }

  render() {
    const {className, label, value, list, onChange} = this.props;
    return (
      <Select className={className} label={label} value={value} list={list} onChange={onChange} />
    );
  }
}

export const SortingSelector = connect(
  (state, props) => ({
    className: props.className,
    label: "Sort by",
    value: state.search.sort?state.search.sort.label:null,
    param: state.search.sort?state.search.sort.param:null,
    list: state.search.sortFields.map(f => ({
      label: f.label,
      value: f.key
    })),
    searchParams: ElasticSearchHelpers.getSearchParamsFromState(state),
    group: state.search.group,
    location: state.router.location
  }),
  dispatch => ({
    onChange: value => dispatch(actions.setSort(value))
  })
)(SortingSelectorComponent);