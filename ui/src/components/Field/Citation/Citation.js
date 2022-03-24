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
import sanitizeHtml from "sanitize-html";
import "./Citation.css";
import API from "../../../services/API";
import CopyToClipboardButton from "../../CopyToClipboard/CopyToClipboardButton";

const Citation = ({ show, data }) => {
  if (!show) {
    return null;
  }
  const [citation, setCitation] = useState();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState();
  const [bibtex, setBibtex] = useState();

  const doi = data && data.value;

  useEffect(() => getCitation(), []);

  const getCitation = async () => {
    setIsLoading(true);
    setError(null);
    const _citationApa = await API.axios.get(API.endpoints.citation(doi, "apa", "text/x-bibliography"));
    const result = _citationApa?.data;
    const _citationBibtex = await API.axios.get(API.endpoints.citation(doi, "bibtex", "application/x-bibtex"));
    const resultBibtex = _citationBibtex?.data;
    if (result) {
      setIsLoading(false);
      setError(null);
      setCitation(sanitizeHtml(result, {
        allowedTags: [],
        allowedAttributes: {},
      }));
    } else {
      setIsLoading(false);
      setCitation(null);
      setError(`The citation for doi ${doi} was not found.`);
    }
    if (resultBibtex) {
      const url = window.URL.createObjectURL(
        new Blob([resultBibtex]),
      );
      setBibtex(url);
    }
  };


  if (error) {
    return (<div>
      <span style={{ color: "var(--code-color)" }}><FontAwesomeIcon icon="exclamation-triangle" />{error} </span>
      <FontAwesomeIcon icon="sync-alt" onClick={() => getCitation()} style={{ cursor: "pointer" }} />
    </div>);
  }

  return (
    <div>
      {isLoading ?
        <div className="kgs-citation-spinner spinner-border spinner-border-sm" role="status">
          <span className="sr-only">Retrieving citation...</span>
        </div> :
        <div className="kgs-citation">
          {citation ?
            <pre>
              <span dangerouslySetInnerHTML={{ __html: citation }} />
              <CopyToClipboardButton icon="clipboard" title="Copy citation" confirmationText="Citation copied" content={citation} />
              {bibtex && <a className="kgs-citation-download" href={bibtex} download={`${doi}.bib`}><FontAwesomeIcon icon="download" /> Download as bibtex</a>}
            </pre> : <a href={`https://doi.org/${doi}`}>DOI: {doi}</a>}
        </div>}
    </div>
  );
};

export default Citation;