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
import React, { useEffect } from "react";
import { connect } from "react-redux";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {faBan} from "@fortawesome/free-solid-svg-icons/faBan";
import {faSyncAlt} from "@fortawesome/free-solid-svg-icons/faSyncAlt";
import {faCircleNotch} from "@fortawesome/free-solid-svg-icons/faCircleNotch";

import * as actionsLinkedInstance from "../actions/actions.linkedInstance";
import LinkedInstance from "./LinkedInstance";

import "./AsyncLinkedInstance.css";
import { useLocation } from "react-router-dom";

const AsyncLinkedInstanceComponent = ({ id, name, group, type, data, error, isLoading, fetchInstance, fetchPreviewInstance }) => {

  const location = useLocation();

  useEffect(() => {
    if (location.pathname.startsWith("/live/")) {
      fetchPreviewInstance(group, id);
    } else {
      fetchInstance(id);
    }
  }, [id, group]);

  if (error) {
    return (
      <div className="kgs-async-linked-instance__error">
        <FontAwesomeIcon icon={faBan} />
        &nbsp;{error}
        <button onClick={() => fetch(group, id)} title="Retry"><FontAwesomeIcon icon={faSyncAlt} /></button>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="kgs-async-linked-instance__loading">
        <FontAwesomeIcon icon={faCircleNotch} spin />
        {` Loading ${type} ${name ? name : id}`}
      </div>
    );
  }

  if (!data) {
    return null;
  }

  return (
    <LinkedInstance data={data} group={group} type={data.type?.value || type} />
  );
};

const AsyncLinkedInstanceContainer = connect(
  (state, props) => ({
    id: props.id,
    name: props.name,
    group: props.group,
    type: props.type,
    data: state.linkedInstance.data,
    error: state.linkedInstance.error,
    isLoading: state.linkedInstance.isLoading
  }),
  dispatch => ({
    fetchInstance: id => dispatch(actionsLinkedInstance.loadLinkedInstancePreview(id)),
    fetchPreviewInstance: (group, id) => dispatch(actionsLinkedInstance.loadLinkedInstance(group, id))
  })
)(AsyncLinkedInstanceComponent);

export default AsyncLinkedInstanceContainer;