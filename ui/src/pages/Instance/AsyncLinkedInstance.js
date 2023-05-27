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
import {faBan} from "@fortawesome/free-solid-svg-icons/faBan";
import {faCircleNotch} from "@fortawesome/free-solid-svg-icons/faCircleNotch";
import {faSyncAlt} from "@fortawesome/free-solid-svg-icons/faSyncAlt";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React from "react";
import { useSelector } from "react-redux";
import { useLocation, matchPath } from "react-router-dom";

import { useGetLinkedInstanceQuery, useGetLinkedPreviewQuery, getError } from "../../services/api";

import LinkedInstance from "./LinkedInstance";

import "./AsyncLinkedInstance.css";

const AsyncLinkedInstance = ({ id, name, type }) => {

  const location = useLocation();

  const group = useSelector(state => state.groups.group);

  const isLive = !!matchPath({path:"/live/*"}, location.pathname);

  const previewResult = useGetLinkedPreviewQuery(id, { skip: !id || !isLive});
  const instanceResult = useGetLinkedInstanceQuery({id: id, group: group}, { skip: !id || isLive});

  const {
    data,
    error,
    isUninitialized,
    //isLoading,
    isFetching,
    //isSuccess,
    isError,
    refetch,
  } = isLive?previewResult:instanceResult;

  if (!id) {
    return null;
  }

  if (isError) {
    let message = getError(error);
    if (error.status == 404) {
      message = `The instance with id ${id} is not available.`;
    }
    return (
      <div className="kgs-async-linked-instance__error">
        <FontAwesomeIcon icon={faBan} />
        &nbsp;{message}
        <button onClick={refetch} title="Retry"><FontAwesomeIcon icon={faSyncAlt} /></button>
      </div>
    );
  }

  if (isUninitialized || isFetching) {
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
    <LinkedInstance data={data.fields} type={data.type || type} />
  );
};

export default AsyncLinkedInstance;