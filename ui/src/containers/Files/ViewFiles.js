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

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
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

const ViewFilesComponent = ({ files, totalFiles, isFilesInitialized, isFilesLoading, filesError, mapping, group, nameField, urlField, fileMapping, groupingType, allowFolderDownload, fetchFiles, clear }) => {

  useEffect(() => {
    fetchFiles(true);
    return () => {
      clear();
    };
  }, []);

  if (filesError) {
    return (
      <div>
        <span style={{color: "var(--code-color)"}}><FontAwesomeIcon icon="exclamation-triangle"/>{filesError} </span>
        <FontAwesomeIcon icon="sync-alt" onClick={() => fetchFiles(true)} style={{cursor: "pointer"}}/>
      </div>
    );
  }

  const isAllFetched = files.length === totalFiles;

  if (!isFilesInitialized || isFilesLoading) {
    return (
      <>
        {!!files.length && (
          <>
            <Label isAllFetched={isAllFetched} number={files.length} total={totalFiles} />
            &nbsp;&nbsp;
          </>
        )}
        <div className="spinner-border spinner-border-sm" role="status">
          <span className="sr-only">Retrieving files...</span>
        </div>
        {!!files.length && (
          <HierarchicalFiles data={files} mapping={mapping} group={group} groupingType={groupingType} allowFolderDownload={allowFolderDownload} nameField={nameField} urlField={urlField} fileMapping={fileMapping} />
        )}
      </>
    );
  }

  if (files.length === 0) {
    return (
      <span>No files available <FontAwesomeIcon icon="sync-alt" onClick={() => fetchFiles(true)} style={{cursor: "pointer"}}/></span>
    );
  }

  const fetchMoreFiles = () => fetchFiles(false);

  const showMoreStyle = {
    display: "inline",
    paddingTop: 0,
    paddingBottom: 0,
    border: 0,
    verticalAlign: "baseline",
    color: "var(--link-color-1)",
    lineHeight: "1rem"
  };

  return (
    <>
      <Label isAllFetched={isAllFetched} number={files.length} total={totalFiles} />
      {!isAllFetched && (
        <button type="button" className="btn btn-link" onClick={fetchMoreFiles} style={showMoreStyle}>show more</button>
      )}
      <HierarchicalFiles data={files} mapping={mapping} group={group} groupingType={groupingType} allowFolderDownload={allowFolderDownload} nameField={nameField} urlField={urlField} fileMapping={fileMapping} />
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
    nameField: props.nameField,
    urlField: props.urlField,
    fileMapping: state.definition.typeMappings.File && state.definition.typeMappings.File.fields,
    groupingType: state.files.groupingType,
    allowFolderDownload: !state.files.groupingType && !state.files.fileFormat
  }),
  (dispatch, props) => ({
    fetchFiles: reset => props.fetch(reset),
    clear: () => dispatch(actionsFiles.clearFiles())
  })
)(ViewFilesComponent);
