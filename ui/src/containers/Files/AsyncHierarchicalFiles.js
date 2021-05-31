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

const AsyncHierarchicalFilesComponent = ({ url, data, total, isInitialized, isLoading, error, mapping, group, fetch, clear }) => {

  useEffect(() => {
    fetch(url);
    return () => {
      clear();
    };
  }, []);

  if (error) {
    return (
      <div>
        <span style={{color: "var(--code-color)"}}><FontAwesomeIcon icon="exclamation-triangle"/>{error} </span>
        <FontAwesomeIcon icon="sync-alt" onClick={() => fetch(url)} style={{cursor: "pointer"}}/>
      </div>
    );
  }

  if (!isInitialized || isLoading) {
    return (
      <div className="spinner-border spinner-border-sm" role="status">
        <span className="sr-only">Retrieving files...</span>
      </div>
    );
  }

  if (data.length === 0) {
    return (
      <span>No files available <FontAwesomeIcon icon="sync-alt" onClick={() => fetch(url)} style={{cursor: "pointer"}}/></span>
    );
  }

  const label = data.length === total?`${total} files`:`showing ${data.length} files out of ${total}`;

  return (
    <>
      <span>{label}</span>
      <HierarchicalFiles data={data} mapping={mapping} group={group} />
    </>
  );
};

export const AsyncHierarchicalFiles = connect(
  (state, props) => ({
    url: props.url,
    data: state.files.files,
    total: state.files.total,
    isInitialized: state.files.isInitialized,
    isLoading: state.files.isLoading,
    error: state.files.error,
    mapping: props.mapping,
    group: props.group
  }),
  dispatch => ({
    fetch: url => dispatch(actionsFiles.loadFiles(url)),
    clear: () => dispatch(actionsFiles.clearFiles())
  })
)(AsyncHierarchicalFilesComponent);