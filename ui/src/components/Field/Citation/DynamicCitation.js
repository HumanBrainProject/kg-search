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
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {faExclamationTriangle} from "@fortawesome/free-solid-svg-icons/faExclamationTriangle";
import {faSyncAlt} from "@fortawesome/free-solid-svg-icons/faSyncAlt";

import { useGetCitationQuery, useGetBibtexQuery } from "../../../app/services/api";

import Citation from "./Citation";

import "./DynamicCitation.css";

const DynamicCitation = ({ title, doi, onCitationDownloaded }) => {

  const citation = useGetCitationQuery(doi);
  const bibtex = useGetBibtexQuery(doi);
  //const { data, currentData, error, isUninitialized, isLoading, isFetching, isSuccess, isError, refetch } = citation;

  useEffect(() => {
    if (citation.data && typeof onCitationDownloaded === "function") {
      onCitationDownloaded(doi, citation.data);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [citation.data, onCitationDownloaded]);

  if (citation.isError) {
    const error = title?`The citation for ${title} was not found.`:`The citation for doi ${doi} was not found.`;
    return (
      <div className="kgs-citation kgs-citation-error">
        <span style={{ color: "var(--code-color)" }}><FontAwesomeIcon icon={faExclamationTriangle} />{error} </span>
        <FontAwesomeIcon icon={faSyncAlt} onClick={citation.refetch} style={{ cursor: "pointer" }} />
      </div>
    );
  }

  if(citation.isUninitialized || citation.isFetching) {
    return (
      <div className="kgs-citation kgs-citation-spinner">
        <span className="spinner-border spinner-border-sm" role="status"></span>
        Retrieving citation{title?` for ${title}`:""}...
      </div>
    );
  }

  return (
    <Citation title={title} citation={citation.data} bibtex={bibtex.data} doi={doi} />
  );
};

export default DynamicCitation;