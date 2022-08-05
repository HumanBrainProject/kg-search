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

import React, { useState, Suspense } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {faExclamationTriangle} from "@fortawesome/free-solid-svg-icons/faExclamationTriangle";
import showdown from "showdown";
import xssFilter from "showdown-xss-filter";
import {faClipboard} from "@fortawesome/free-solid-svg-icons/faClipboard";
import CopyToClipboardButton from "../../CopyToClipboard/CopyToClipboardButton";

const CitationsList = React.lazy(() => import("./CitationsList"));

import "./Citations.css";

const converter = new showdown.Converter({extensions: [xssFilter]});

const Loading = () => (
  <>
    <div className="spinner-border spinner-border-sm" role="status"></div>
    &nbsp;Loading...
  </>
);

const Citations = ({ data }) => {

  const [citations, setCitations] = useState(data
    .map(c => {
      if (c.citation) {
        return {
          ...c,
          key: c.id,
          citation: converter.makeHtml(c.citation)
        };
      }
      return {
        ...c,
        key: c.doi,
        isDynamic: true
      };;
    })
    .reduce((acc, c) => {
      acc[c.key] = c;
      return acc;
    }, {})
  );

  const handleOnCitationDownloaded = (doi, citation) => {
    setCitations(citations => ({
      ...citations,
      [doi]: citation?
        {
          ...citations[doi],
          citation: citation,
          error: false
        }
        :
        {
          ...citations[doi],
          error: true
        }
    }));
  };

  const list =  Object.values(citations);
  const allCitations = list.filter(item => item.citation);
  const errors = list.filter(item => item.error).length;
  const number = allCitations.length;
  const total = list.length;
  const text = allCitations.map(item => item.title?`<h6><strong>${item.title}</strong></h6>\n${item.citation}`:item.citation).join("\n\n");

  return (
    <div className="kgs-citations">
      <div className="kgs-citations-header">
        {(number + errors) === total?
          <>
            {number !== total && (
              <span style={{ color: "var(--code-color)" }} title={`Only ${number} out of ${total} citations available)`}><FontAwesomeIcon icon={faExclamationTriangle} /></span>
            )}
            <CopyToClipboardButton icon={faClipboard} title={(number === total)?"Copy all citations":`Copy available citations (${number} out of ${total} )`} confirmationText="Citations copied" content={text} />
          </>
          :
          <div className="kgs-citations-spinner">
            <span className="spinner-border spinner-border-sm" role="status"></span>
            Retrieving citations ({number + errors}/{total})...
          </div>
        }
      </div>
      <div className="kgs-citations-body">
        <Suspense fallback={<Loading />}>
          <CitationsList citations={Object.values(citations)} onCitationDownloaded={handleOnCitationDownloaded} />
        </Suspense>
      </div>
    </div>
  );
};

export default Citations;