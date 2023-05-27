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

import {faClipboard} from "@fortawesome/free-solid-svg-icons/faClipboard";
import {faDownload} from "@fortawesome/free-solid-svg-icons/faDownload";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React from "react";
import CopyToClipboardButton from "../../CopyToClipboard/CopyToClipboardButton";

import "./Citation.css";

const Citation = ({title, citation, doi, bibtex}) => {
  const html = title?`<h6><strong>${title}</strong></h6>\n${citation}`:citation;
  return (
    <div className="kgs-citation">
      {citation ?
        <pre>
          <span dangerouslySetInnerHTML={{ __html: html }} />
          <CopyToClipboardButton icon={faClipboard} title="Copy citation" confirmationText="Citation copied" content={html} />
          {bibtex && <a className="kgs-citation-download" href={bibtex} download={`${doi}.bib`}><FontAwesomeIcon icon={faDownload} /> Download as bibtex</a>}
        </pre>
        :
        <>
          {title && (
            <h6><strong>${title}</strong></h6>
          )}
          <a href={`https://doi.org/${doi}`}>DOI: {doi}</a>
        </>
      }
    </div>
  );
};

export default Citation;