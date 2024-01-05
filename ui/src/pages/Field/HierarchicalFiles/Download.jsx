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

import {faDownload} from '@fortawesome/free-solid-svg-icons/faDownload';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import React, { useState } from 'react';

import { InfoPanel } from '../../../components/InfoPanel/InfoPanel';

import { termsOfUse } from '../../../data/termsOfUse.jsx';
import Matomo from '../../../services/Matomo';

import './Download.css';

const Download = ({name, type, url}) => {

  const [showTermsOfUse, toggleTermsOfUse] = useState(false);

  const trackDownload = e => {
    e.stopPropagation();
    Matomo.trackLink(url, 'download');
  };

  const openTermsOfUse = e => {
    e && e.preventDefault();
    toggleTermsOfUse(true);
  };

  const closeTermsOfUse = e => {
    e && e.preventDefault();
    toggleTermsOfUse(false);
  };

  return (
    <>
      <a type="button" className="btn kgs-hierarchical-files__info_link" rel="noopener noreferrer" target="_blank" href={url} onClick={trackDownload} >
        <FontAwesomeIcon icon={faDownload} /> {name}
      </a>
      <div className="kgs-hierarchical-files__info_agreement"><span>By downloading the {type} you agree to the <button onClick={openTermsOfUse}><strong>Terms of use</strong></button></span></div>
      {showTermsOfUse && (
        <InfoPanel text={termsOfUse} onClose={closeTermsOfUse} />
      )}
    </>
  );
};

export default Download;
