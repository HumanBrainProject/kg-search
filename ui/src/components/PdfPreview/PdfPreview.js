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
import React, {useState} from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Document, Page } from "react-pdf/dist/esm/entry.webpack";
import "./PdfPreview.css";


const PdfPreview = ({ show, data }) => {
  if(!show) {
    return null;
  }

  const [numPages, setNumPages] = useState(null);
  const [pageNumber, setPageNumber] = useState(1);

  const onDocumentLoadSuccess = ({ numPages }) =>  {
    setNumPages(numPages);
    setPageNumber(1);
  };

  const changePage = offset  => setPageNumber(prevPageNumber => prevPageNumber + offset);

  const previousPage = () => changePage(-1);

  const nextPage = () => changePage(1);

  const renderLoader = () =>(
    <div style={{paddingTop:"10px"}}>
      <FontAwesomeIcon icon="circle-notch" spin  />
      <span style={{marginLeft:"5px"}}>Loading pdf...</span>
    </div>
  );


  return (
    <div className="kgs-pdf-preview">
      {numPages && <a className="kgs-pdf-preview-download" href={data.url} rel="noreferrer" target="_blank"><FontAwesomeIcon icon="download" /></a>}
      <Document
        className="kgs-pdf-preview-document"
        file={data.url}
        loading={renderLoader}
        onLoadSuccess={onDocumentLoadSuccess}
      >
        <Page
          scale={1.3}
          pageNumber={pageNumber} />
      </Document>
      {pageNumber && numPages && (
        <div className="kgs-pdf-preview-controls-container">
          <button
            className="kgs-pdf-preview-controls"
            disabled={pageNumber <= 1}
            onClick={previousPage}>
            <FontAwesomeIcon icon="chevron-left" />
          </button>
          <p>
            {pageNumber}/{numPages}
          </p>
          <button
            className="kgs-pdf-preview-controls"
            disabled={pageNumber >= numPages}
            onClick={nextPage}>
            <FontAwesomeIcon icon="chevron-right"/>
          </button>
        </div>
      )}
    </div>
  );
};

export default PdfPreview;