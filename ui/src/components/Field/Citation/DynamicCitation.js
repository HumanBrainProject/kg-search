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

import React, { useState, useEffect } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {faExclamationTriangle} from "@fortawesome/free-solid-svg-icons/faExclamationTriangle";
import {faSyncAlt} from "@fortawesome/free-solid-svg-icons/faSyncAlt";
import sanitizeHtml from "sanitize-html";
import API from "../../../services/API";
import Citation from "./Citation";

import "./DynamicCitation.css";

const DynamicCitation = ({ title, doi, onCitationDownloaded }) => {

  const [citation, setCitation] = useState();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState();
  const [bibtex, setBibtex] = useState();

  useEffect(() => {
    getCitation();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getCitation = async () => {
    setIsLoading(true);
    setError(null);
    const apaCitationResult = await API.axios.get(API.endpoints.citation(doi, "apa", "text/x-bibliography"));
    const apaCitation = apaCitationResult?.data;
    const bibtexCitationResult = await API.axios.get(API.endpoints.citation(doi, "bibtex", "application/x-bibtex"));
    const bibtexCitation = bibtexCitationResult?.data;
    if (apaCitation) {
      setIsLoading(false);
      setError(null);
      const citation = sanitizeHtml(apaCitation, {
        allowedTags: [],
        allowedAttributes: {},
      });
      setCitation(citation);
      typeof onCitationDownloaded === "function" && onCitationDownloaded(doi, citation);
    } else {
      setIsLoading(false);
      setCitation(null);
      setError(title?`The citation for ${title} was not found.`:`The citation for doi ${doi} was not found.`);
      typeof onCitationDownloaded === "function" && onCitationDownloaded(doi, null);
    }
    if (bibtexCitation) {
      const bibtexUrl = window.URL.createObjectURL(
        new Blob([bibtexCitation]),
      );
      setBibtex(bibtexUrl);
    }
  };

  if (error) {
    return (
      <div className="kgs-citation kgs-citation-error">
        <span style={{ color: "var(--code-color)" }}><FontAwesomeIcon icon={faExclamationTriangle} />{error} </span>
        <FontAwesomeIcon icon={faSyncAlt} onClick={getCitation} style={{ cursor: "pointer" }} />
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="kgs-citation kgs-citation-spinner">
        <span className="spinner-border spinner-border-sm" role="status"></span>
        Retrieving citation{title?` for ${title}`:""}...
      </div>
    );
  }

  return (
    <Citation title={title} citation={citation} bibtex={bibtex} doi={doi} />
  );
};

export default DynamicCitation;