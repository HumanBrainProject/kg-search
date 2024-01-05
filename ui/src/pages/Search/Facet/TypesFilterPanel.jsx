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

import React from 'react';
import { useSelector, useDispatch } from 'react-redux';

import { setType } from '../../../features/search/searchSlice';
import './TypesFilterPanel.css';

const TypeFilter = ({ type }) => {

  const dispatch = useDispatch();

  const handleOnClick = () => {
    dispatch(setType(type.type));
  };

  return (
    <div className={`kgs-fieldsFilter-checkbox ${type.active?'is-active':''}`} onClick={handleOnClick} >
      <div className="kgs-fieldsFilter-checkbox__text">{type.label}</div>
      <div className="kgs-fieldsFilter-checkbox__count">{type.count}</div>
    </div>
  );
};

const TypesFilterPanel = () => {

  const types = useSelector(state => state.search.types
    .filter(t => t.count > 0 || t.type === state.search.selectedType)
    .map(t => ({
      ...t,
      active: t.type === state.search.selectedType
    }))
  );

  return (
    <div className="kgs-fieldsFilter" >
      <div className="kgs-fieldsFilter-title" >Categories</div>
      {types.map(type =>
        <TypeFilter key={type.type} type={type} />
      )}
    </div>
  );
};

export default TypesFilterPanel;