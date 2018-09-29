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
import { connect } from "../../helpers/react-redux-like";
import { Select, SortingSelector as Component} from "searchkit";
import "./SortingSelector.css";

export const SortingSelectorBase = ({className, sortFields}) => {
  if (Array.isArray(sortFields) && sortFields.length) {
    const classNames = `kgs-sorting-selector ${className}`;
    return <span className={classNames?classNames:null}><Component listComponent={Select} options={sortFields} /></span>;
  }
  return null;
};

export const SortingSelector = connect(
  (state, props) => ({
    className: props.className,
    sortFields: state.definition.sortFields
  })
)(SortingSelectorBase);