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
import { useSelector, useDispatch } from "react-redux";

import { trackEvent } from "../../../app/services/api";
import { setInstanceId } from "../../../features/instance/instanceSlice";

import { Hit } from "./Hit";

import "./Hits.css";

const HitButton = ({ data }) => {

  const dispatch = useDispatch();

  const group = useSelector(state => state.groups.group);
  const defaultGroup = useSelector(state => state.groups.defaultGroup);

  const handleClick = e => {
    if (!e.ctrlKey) {
      dispatch(setInstanceId(data.id));
    }
  };

  const handleContextMenu = e => {
    e.preventDefault();
    const relativeUrl = `/instances/${data.id}${(group != defaultGroup)?("?group=" + group):""}`;
    trackEvent("Card", "Open in new tab", relativeUrl);
    window.open(relativeUrl, "_blank");
  };

  return (
    <button role="link" className="kgs-hit-button" onClick={handleClick} onContextMenu={handleContextMenu} >
      <Hit data={data} />
    </button>
  );
};

const Hits = () => {

  const items = useSelector(state => state.search.hits);

  if (!Array.isArray(items) || !items.length) {
    return null;
  }

  return (
    <div className="kgs-hits">
      <ul>
        {items.map(item => (
          <li key={item.id}>
            <HitButton data={item} />
          </li>
        ))}
      </ul>
    </div>
  );
};

export default Hits;