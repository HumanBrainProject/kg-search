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
import React, { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { connect } from "react-redux";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {faBan} from "@fortawesome/free-solid-svg-icons/faBan";
import {faSyncAlt} from "@fortawesome/free-solid-svg-icons/faSyncAlt";
import {faCircleNotch} from "@fortawesome/free-solid-svg-icons/faCircleNotch";

import API from "../services/API";
import { sessionFailure } from "../actions/actions";
import LinkedInstance from "./LinkedInstance";

import "./AsyncLinkedInstance.css";

const AsyncLinkedInstanceComponent = ({ id, name, group, type, onSessionFailure }) => {

  const location = useLocation();

  const [data, setData] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetch = () => {
    setIsLoading(true);
    setError(null);
    const url = location.pathname.startsWith("/live/")?API.endpoints.preview(id):API.endpoints.instance(group, id);
    API.axios
      .get(url)
      .then(response => {
        if (response.data && !response.data.error) {
          setData(response.data.fields);
        } else if (response.data && response.data.error) {
          setError(response.data.message ? response.data.message : response.data.error);
        } else {
          const errorMessage = `The instance with id ${id} is not available.`;
          setError(errorMessage);
        }
        setIsLoading(false);
      })
      .catch(e => {
        switch (e?.response?.status) {
        case 401: // Unauthorized
        case 403: // Forbidden
        case 511: // Network Authentication Required
        {
          setIsLoading(false);
          onSessionFailure();
          break;
        }
        case 404:
        {
          const errorMessage = `The instance with id ${id} is not available.`;
          setError(errorMessage);
          break;
        }
        case 500:
        default:
        {
          setError(`The service is temporarily unavailable. Please retry in a few minutes. (${e.message?e.message:e})`);
          setIsLoading(false);
        }
        }
      });
  };

  useEffect(() => {
    fetch();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, group]);

  if (error) {
    return (
      <div className="kgs-async-linked-instance__error">
        <FontAwesomeIcon icon={faBan} />
        &nbsp;{error}
        <button onClick={fetch} title="Retry"><FontAwesomeIcon icon={faSyncAlt} /></button>
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
    <LinkedInstance data={data} group={data.group || group} type={data.type || type} />
  );
};

const AsyncLinkedInstance = connect(
  (_, props) => ({
    id: props.id,
    name: props.name,
    group: props.group,
    type: props.type
  }),
  dispatch => ({
    onSessionFailure: () => {
      dispatch(sessionFailure("Your session has expired. Please login again."));
    }
  })
)(AsyncLinkedInstanceComponent);

export default AsyncLinkedInstance;