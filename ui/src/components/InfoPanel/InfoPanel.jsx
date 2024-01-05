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

import {faTimes} from '@fortawesome/free-solid-svg-icons/faTimes';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import PropTypes from 'prop-types';
import React, { useRef } from 'react';
import showdown from 'showdown';
import DOMPurify from 'dompurify';

import './InfoPanel.css';

const converter = new showdown.Converter();

export const InfoPanel = ({text, onClose}) => {
  const ref = useRef();

  const handleOnClose = e => {
    if (ref.current && !ref.current.contains(e.target)) {
      typeof onClose === 'function' && onClose();
    }
  };

  const handleOnCloseButton = () => typeof onClose === 'function' && onClose();

  if (!text) {
    return null;
  }

  const html = DOMPurify.sanitize(converter.makeHtml(text));
  return (
    <div className="kgs-info" onClick={handleOnClose} >
      <div className="kgs-info__panel" ref={ref}>
        <div className="kgs-info__container">
          <span className="kgs-info__content" dangerouslySetInnerHTML={{__html:html}} />
          <button className="kgs-info__closeButton" onClick={handleOnCloseButton}>
            <FontAwesomeIcon icon={faTimes} />
          </button>
        </div>
      </div>
    </div>
  );

};


InfoPanel.propTypes = {
  text: PropTypes.string,
  onClose: PropTypes.func
};

export default InfoPanel;