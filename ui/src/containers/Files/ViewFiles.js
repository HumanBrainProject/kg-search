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

import { useMemo } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {faExclamationTriangle} from "@fortawesome/free-solid-svg-icons/faExclamationTriangle";
import {faSyncAlt} from "@fortawesome/free-solid-svg-icons/faSyncAlt";
import React, { useEffect } from "react";
import { connect } from "react-redux";

import * as actionsFiles from "../../actions/actions.files";
import HierarchicalFiles from "../../components/Field/Files/HierarchicalFiles";

const Label = ({isAllFetched, number, total}) => {

  if (isAllFetched) {
    return (
      <span><i>{total}</i> files</span>
    );
  }

  return (
    <span>Showing <i>{number}</i> files out of <i>{total}</i>.</span>
  );
};

const ViewFilesComponent = ({ files, totalFiles, isFilesInitialized, isFilesLoading, filesError, mapping, group, type, nameFieldPath, urlFieldPath, groupingType, hasDataFilter, fetchFiles, clear }) => {

  useEffect(() => {
    fetchFiles(true);
    return () => {
      clear();
    };
  }, []);

  if (filesError) {
    return (
      <div>
        <span style={{color: "var(--code-color)"}}><FontAwesomeIcon icon={faExclamationTriangle} />{filesError} </span>
        <FontAwesomeIcon icon={faSyncAlt} onClick={() => fetchFiles(true)} style={{cursor: "pointer"}}/>
      </div>
    );
  }

  const isAllFetched = files.length === totalFiles;

  if (files.length === 0) {
    if (!isFilesInitialized || isFilesLoading) {
      return (
        <div className="spinner-border spinner-border-sm" role="status">
          <span className="sr-only">Retrieving files...</span>
        </div>
      );
    }
    return (
      <span>No files available <FontAwesomeIcon icon={faSyncAlt} onClick={() => fetchFiles(true)} style={{cursor: "pointer"}}/></span>
    );
  }

  const fetchMoreFiles = useMemo(() => () => fetchFiles(false), [fetchFiles]);

  const showMoreStyle = {
    display: "inline",
    padding: 0,
    border: 0,
    verticalAlign: "baseline",
    color: "var(--link-color-1)",
    lineHeight: "1rem"
  };

  const showLabel = totalFiles !== 1 || files.length !== 1 || !!groupingType || hasDataFilter;

  return (
    <>
      <div>
        {showLabel && (
          <>
            <Label isAllFetched={isAllFetched} number={files.length} total={totalFiles} />&nbsp;
          </>
        )}
        {isFilesLoading?
          <div className="spinner-border spinner-border-sm" role="status">
            <span className="sr-only">Retrieving files...</span>
          </div>
          :
          !isAllFetched && (
            <button type="button" className="btn btn-link" onClick={fetchMoreFiles} style={showMoreStyle}>show more</button>
          )
        }
      </div>
      <HierarchicalFiles data={files} mapping={mapping} group={group} type={type} groupingType={groupingType} hasDataFilter={hasDataFilter} nameFieldPath={nameFieldPath} urlFieldPath={urlFieldPath} />
    </>
  );
};

export const ViewFiles = connect(
  (state, props) => ({
    files: state.files.files,
    totalFiles: state.files.totalFiles,
    isFilesInitialized: state.files.isFilesInitialized,
    isFilesLoading: state.files.isFilesLoading,
    filesError: state.files.filesError,
    mapping: props.mapping,
    group: props.group,
    type: props.type,
    nameFieldPath: props.nameFieldPath,
    urlFieldPath: props.urlFieldPath,
    groupingType: state.files.groupingType,
    hasDataFilter: state.files.groupingType || state.files.fileFormat
  }),
  (dispatch, props) => ({
    fetchFiles: reset => props.fetch(reset),
    clear: () => dispatch(actionsFiles.clearFiles())
  })
)(ViewFilesComponent);
