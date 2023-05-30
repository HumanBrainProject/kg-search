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

import {faMoon} from '@fortawesome/free-solid-svg-icons/faMoon';
import {faSun} from '@fortawesome/free-solid-svg-icons/faSun';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import React from 'react';
import { useSelector, useDispatch } from 'react-redux';

import { setTheme } from '../application/applicationSlice';

import './ThemeToggle.css';

const themes = [
  {
    name: 'dark',
    icon: faMoon
  },
  {
    name: 'default',
    icon: faSun
  }
];

const ThemeButton = ({ theme }) => {

  const dispatch = useDispatch();

  const current = useSelector(state => state.application.theme);

  const handleClick = () => {
    dispatch(setTheme(theme.name));
  };

  return (
    <button className={`kgs-theme_toggle__button ${(theme.name === current || (!current && theme.name === 'default'))?'selected':''}`} onClick={handleClick}>
      <FontAwesomeIcon icon={theme.icon} />
    </button>
  );
};

const ThemeToggle = () => (
  <div className="kgs-theme_toggle">
    {themes.map(t => (
      <ThemeButton key={t.name} theme={t} />
    ))}
  </div>
);

export default ThemeToggle;