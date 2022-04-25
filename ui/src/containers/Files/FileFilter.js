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

import React, { useEffect, useState, useMemo } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {faExclamationTriangle} from "@fortawesome/free-solid-svg-icons/faExclamationTriangle";
import {faSyncAlt} from "@fortawesome/free-solid-svg-icons/faSyncAlt";

import API from "../../services/API";
import { Select } from "../../components/Select/Select";

import "./FileFilter.css";


const FileFilterComponent = ({ title, value, list, error, isLoading, onSelect, onRetry }) => {

  const handleChange = useMemo(() => value => onSelect(value?value:null), [onSelect]);

  const selectList = useMemo(() => {
    if (!Array.isArray(list) || !list.length) {
      return [];
    }
    return list.reduce((acc, value) => {
      acc.push({label: value, value: value});
      return acc;
    }, [{label: "none", value: ""}]);
  }, [list]);

  if (error) {
    return (
      <div>
        <span style={{color: "var(--code-color)"}}><FontAwesomeIcon icon={faExclamationTriangle} />{error} </span>
        <FontAwesomeIcon icon={faSyncAlt} onClick={onRetry} style={{cursor: "pointer"}}/>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="spinner-border spinner-border-sm" role="status">
        <span className="sr-only">Retrieving {title}...</span>
      </div>
    );
  }

  if (!selectList.length) {
    return null;
  }

  return (
    <div><Select className="kgs-fileFilter" label={title} value={value} list={selectList} onChange={handleChange} /></div>
  );

};

export const FileFilter = ({ title, show, url, value, onSelect, onSessionFailure }) => {

  const [list, setList] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetch = () => {
    if (!url) {
      throw new Error("FileFilter is missing prop url");
    }
    setIsLoading(true);
    setError(null);
    API.axios
      .get(url)
      .then(response => {
        const data = response.data;
        const items = Array.isArray(data.data)?data.data.sort():[];
        setList(items);
        setIsLoading(false);
      })
      .catch(e => {
        const { response } = e;
        if (response) {
          const { status } = response;
          switch (status) {
          case 401: // Unauthorized
          case 403: // Forbidden
          case 511: // Network Authentication Required
          {
            setIsLoading(false);
            typeof onSessionFailure === "function" && onSessionFailure();
            break;
          }
          case 500:
          case 404:
          default:
          {
            setError(`The service is temporarily unavailable. Please retry in a few minutes. (${e.message?e.message:e})`);
            setIsLoading(false);
          }
          }
        } else {
          setError(`The service is temporarily unavailable. Please retry in a few minutes. (${e.message?e.message:e})`);
          setIsLoading(false);
        }
      });
  };

  useEffect(fetch, []);

  if (!show) {
    return null;
  }

  return (
    <FileFilterComponent title={title} value={value} list={list} error={error} isLoading={isLoading} onSelect={onSelect} onRetry={fetch} />
  );

};

export default FileFilter;