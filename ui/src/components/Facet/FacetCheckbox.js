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

import {faCheck} from '@fortawesome/free-solid-svg-icons/faCheck';
import {faMinus} from '@fortawesome/free-solid-svg-icons/faMinus';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import React from 'react';

import './FacetCheckbox.css';

const Icon = ({checked, hasAnyChildChecked}) => {
  if(checked) {
    return <FontAwesomeIcon icon={faCheck} />;
  }
  if(hasAnyChildChecked) {
    return <FontAwesomeIcon icon={faMinus} />;
  }
  return null;
};

const FacetCheckbox = ({ item: { label, count, checked, hasAnyChildChecked } }) => (
  <div className={`kgs-facet-checkbox ${checked ? 'is-active' : ''}  ${hasAnyChildChecked ? 'has-any-child-active' : ''}`}>
    <input type="checkbox" tabIndex="-1" name={label}/><Icon checked={checked} hasAnyChildChecked={hasAnyChildChecked} />
    <div className="kgs-facet-checkbox__text">{label}</div>
    {count !== undefined && (
      <div className="kgs-facet-checkbox__count">{count}</div>
    )}
  </div>
);

export default FacetCheckbox;