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

import './Details.css';
import {faExclamationCircle} from '@fortawesome/free-solid-svg-icons/faExclamationCircle';
import {faTimes} from '@fortawesome/free-solid-svg-icons/faTimes';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import React, { useState } from 'react';
import { Text } from '../Text/Text';

export const Details = ({ toggleLabel, content, asPopup }) => {
  const [collapsed, setCollapsed] = useState(true);

  const handleToggle = () => setCollapsed(isCollapsed => !isCollapsed);

  const handleClose = () => setCollapsed(true);

  if (!content) {
    return null;
  }

  const className = `toggle ${collapsed ? '' : 'in'}`;
  const classNameAsPopup = `popup ${collapsed ? '' : 'show'}`;
  return (
    <span className="field-details">
      <button className={className} onClick={handleToggle}>
        <FontAwesomeIcon icon={faExclamationCircle} />
        {toggleLabel && (
          <span>{toggleLabel}</span>
        )}
      </button>
      {!asPopup ?
        <div className="collapsible">
          {!collapsed && (
            <div className="field-details__panel">
              <Text content={content} isMarkdown={true} />
              <button className="field-details__close-button" onClick={handleClose} title="close">
                <FontAwesomeIcon icon={faTimes} size="2x" />
              </button>
            </div>
          )}
        </div>
        :
        <div className={classNameAsPopup}>
          <div className="field-details__panel">
            <Text content={content} isMarkdown={true} />
            <button className="field-details__close-button" onClick={handleClose} title="close">
              <FontAwesomeIcon icon={faTimes} size="2x" />
            </button>
          </div>
        </div>
      }
    </span>
  );

};

export default Details;