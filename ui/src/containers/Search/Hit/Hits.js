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
import { useNavigate } from "react-router-dom";
import { connect } from "react-redux";

import API from "../../../services/API";
import { loadInstance } from "../../../actions/actions.instances";

import { Hit } from "./Hit";

import "./Hits.css";

const HitButton = ({ data, onClick }) => {
  const handleClick = e => {
    if (!e.ctrlKey) {
      onClick(data, e.currentTarget);
    }
  };

  const handleContextMenu = e => {
    e.preventDefault();
    onClick(data, "_blank");
  };

  return (
    <button role="link" className="kgs-hit-button" onClick={handleClick} onContextMenu={handleContextMenu} >
      <Hit data={data} />
    </button>
  );
};

const HitsBase = ({ items, onClick, group, defaultGroup }) => {
  const navigate = useNavigate();
  const handleClick = (data, target) => onClick(data, target, group, defaultGroup, navigate);
  if (!Array.isArray(items) || items.length === 0) {
    return null;
  }
  return (
    <div className="kgs-hits">
      <ul>
        {items.map(item => (
          <li key={item.id}>
            <HitButton data={item} onClick={handleClick} />
          </li>
        ))}
      </ul>
    </div>
  );
};

export const Hits = connect(
  state => ({
    items: state.search.hits,
    group: state.groups.group,
    defaultGroup: state.groups.defaultGroup
  }),
  dispatch => ({
    onClick: (data, target, group, defaultGroup, navigate) => {
      if (target === "_blank") {
        const relativeUrl = `/instances/${data.id}${(group != defaultGroup)?("?group=" + group):""}`;
        API.trackEvent("Card", "Open in new tab", relativeUrl);
        window.open(relativeUrl, "_blank");
      } else {
        dispatch(loadInstance(group, data.id, () => {
          navigate(`/${window.location.search}#${data.id}`);
        }));
      }
    }
  })
)(HitsBase);