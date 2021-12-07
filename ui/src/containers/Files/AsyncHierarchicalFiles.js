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
import React from "react";
import { connect } from "react-redux";

import { FileFormatFilter } from "./FileFormatFilter";
import { FileBundleFilter } from "./FileBundleFilter";
import { ViewFiles } from "./ViewFiles";
import * as actionsFiles from "../../actions/actions.files";

export const AsyncHierarchicalFilesComponent = ({mapping, group, urlField, fileMapping, searchFilesAfter, fileBundle, fileFormat, fetchFiles, fetchFileFormats, fetchFileBundles}) => {

  const handleSelectFileFormat = format => {
    console.log("format", format);
    fetchFiles(searchFilesAfter, fileBundle, format, true);
  };

  const handleSelectFileBundle = bundle => {
    console.log("bundle", bundle);
    fetchFiles(searchFilesAfter, bundle, fileFormat, true);
  };

  const handleFetchFiles = reset => {
    fetchFiles(searchFilesAfter, fileBundle, fileFormat, reset);
  };

  return (
    <>
      <FileFormatFilter onSelect={handleSelectFileFormat} fetch={fetchFileFormats} />
      <FileBundleFilter onSelect={handleSelectFileBundle} fetch={fetchFileBundles} />
      <ViewFiles mapping={mapping} group={group} fetch={handleFetchFiles} urlField={urlField} fileMapping={fileMapping} />
    </>
  );
};

export const AsyncHierarchicalFiles = connect(
  (state, props) => ({
    files: state.files.files,
    searchFilesAfter: state.files.searchFilesAfter,
    isFilesInitialized: state.files.isFilesInitialized,
    isFilesLoading: state.files.isFilesLoading,
    filesError: state.files.filesError,
    fileFormat: state.files.fileFormat,
    fileBundle: state.files.fileBundle,
    mapping: props.mapping,
    group: props.group,
    urlField: props.urlField,
    fileMapping: state.definition.typeMappings.File && state.definition.typeMappings.File.fields
  }),
  (dispatch, props) => ({
    fetchFiles: (searchAfter, fileBundle, fileFilter, reset) => dispatch(actionsFiles.loadFiles(props.filesUrl, searchAfter, fileBundle, fileFilter, reset)),
    fetchFileFormats: () => dispatch(actionsFiles.loadFileFormats(props.fileFormatsUrl)),
    fetchFileBundles: () => dispatch(actionsFiles.loadFileBundles(props.fileBundlesUrl))
  })
)(AsyncHierarchicalFilesComponent);
