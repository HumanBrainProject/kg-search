/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

import React from "react";
import { connect } from "react-redux";
import * as actionsSearch from "../../../actions/actions.search";
import { Select } from "../../../components/Select/Select";

const SortingSelectorComponent = ({show, className, label, value, list, onChange}) => {
  if (!show) {
    return null;
  }

  return (
    <Select className={className} label={label} value={value} list={list} onChange={onChange} />
  );
};

export const SortingSelector = connect(
  (state, props) => {
    const list = Array.isArray(state.search.sortFields[state.search.selectedType])?state.search.sortFields[state.search.selectedType]:[];
    return {
      show: state.search.total && state.search.total > 0 && list.length > 0,
      className: props.className,
      label: "Sort by",
      value: state.search.sort?state.search.sort:null,
      list: list
    };
  },
  dispatch => ({
    onChange: value => {
      dispatch(actionsSearch.setSort(value));
    }
  })
)(SortingSelectorComponent);