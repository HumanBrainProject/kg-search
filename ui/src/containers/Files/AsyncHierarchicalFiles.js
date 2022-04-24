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
import React, { useMemo } from "react";
import { connect } from "react-redux";

import { FileFormatFilter } from "./FileFormatFilter";
import { GroupingTypeFilter } from "./GroupingTypeFilter";
import { ViewFiles } from "./ViewFiles";
import * as actionsFiles from "../../actions/actions.files";

export const AsyncHierarchicalFilesComponent = ({mapping, group, type, nameFieldPath, urlFieldPath, searchFilesAfter, groupingType, fileFormat, fetchFiles, fetchFileFormats, fetchGroupingTypes}) => {

  const handleSelectFileFormat = useMemo(() => format => {
    fetchFiles(searchFilesAfter, groupingType, format, true);
  }, [searchFilesAfter, groupingType]);

  const handleSelectGroupingType = useMemo(() => type => {
    fetchFiles(searchFilesAfter, type, fileFormat, true);
  }, [searchFilesAfter, fileFormat]);

  const handleFetchFiles = useMemo(() => reset => {
    fetchFiles(searchFilesAfter, groupingType, fileFormat, reset);
  }, [searchFilesAfter, groupingType, fileFormat]);

  return (
    <>
      <FileFormatFilter onSelect={handleSelectFileFormat} fetch={fetchFileFormats} />
      <GroupingTypeFilter onSelect={handleSelectGroupingType} fetch={fetchGroupingTypes} />
      <ViewFiles mapping={mapping} group={group} type={type} fetch={handleFetchFiles} nameFieldPath={nameFieldPath} urlFieldPath={urlFieldPath} />
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
    groupingType: state.files.groupingType,
    mapping: props.mapping,
    group: props.group,
    type: props.type,
    nameFieldPath: props.nameFieldPath,
    urlFieldPath: props.urlFieldPath
  }),
  (dispatch, props) => ({
    fetchFiles: (searchAfter, groupingType, fileFilter, reset) => {
      dispatch(actionsFiles.loadFiles(props.filesUrl, searchAfter, groupingType, fileFilter, reset));
    },
    fetchFileFormats: () => {
      dispatch(actionsFiles.loadFileFormats(props.fileFormatsUrl));
    },
    fetchGroupingTypes: () => {
      dispatch(actionsFiles.loadGroupingTypes(props.groupingTypesUrl));
    }
  })
)(AsyncHierarchicalFilesComponent);
