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

import React, {useState, useEffect} from "react";
import axios from "axios";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import API from "../../../services/API";
import Select from "../../Select/Select";
import "./Citation.css";

const CITATION_STYLES = [
  {label: "european-journal-of-neuroscience", value: "european-journal-of-neuroscience"},
  {label: "bibtex", value: "bibtex"}
];

const Citation = ({show, data}) => {
  if(!show) {
    return null;
  }
  const [citation, setCitation] = useState();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState();
  const [citationStyle, setCitationStyle] = useState("european-journal-of-neuroscience");

  const doi = data && data.value;

  useEffect(() => getCitation(), [citationStyle]);

  const getCitation = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const _citation = await axios.get(API.endpoints.citation(doi, citationStyle));
      const result = _citation && _citation.data?_citation.data:null;
      setIsLoading(false);
      setError(null);
      setCitation(result);
    } catch(e) {
      setIsLoading(false);
      setCitation(null);
      const { response } = e;
      const { status } = response;
      switch (status) {
      case 404:
      {
        const error = `The citation for doi ${doi} was not found.`;
        setError(error);
        break;
      }
      default:
      {
        const error = "Something went wrong. Please try again!";
        setError(error);
      }
      }
    }
  };

  const onChange = value => setCitationStyle(value);

  if(error) {
    return(<div>
      <span style={{color: "var(--code-color)"}}><FontAwesomeIcon icon="exclamation-triangle"/>{error} </span>
      <FontAwesomeIcon icon="sync-alt" onClick={() => getCitation()} style={{cursor: "pointer"}}/>
    </div>);
  }

  return(
    <div>
      <div className="kgs-citation-label">Select Formatting Style:</div>
      <Select className="kgs-citation-select" value={citationStyle} list={CITATION_STYLES} onChange={onChange} />
      {isLoading?
        <div className="kgs-citation-spinner spinner-border spinner-border-sm" role="status">
          <span className="sr-only">Retrieving citation...</span>
        </div>:
        <div className="kgs-citation">
          <pre>{citation}</pre>
        </div>}
    </div>
  );
};

export default Citation;