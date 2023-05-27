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
import { faExclamationTriangle } from '@fortawesome/free-solid-svg-icons/faExclamationTriangle';
import { faSyncAlt } from '@fortawesome/free-solid-svg-icons/faSyncAlt';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import { useLocation, matchPath } from 'react-router-dom';

import Matomo from '../../../services/Matomo';
import {
  useListFilesQuery,
  useListPreviewFilesQuery,
  useListFormatsQuery,
  useListPreviewFormatsQuery,
  useListGroupingTypesQuery,
  useListPreviewGroupingTypesQuery,
  getError
} from '../../../services/api';

import { FileFilter } from './FileFilter';
import HierarchicalFiles from './HierarchicalFiles';

const FetchingFiles = () => (
  <span>
    <div className="spinner-border spinner-border-sm" role="status" />
    &nbsp;Retrieving Files...
  </span>
);

const FilesBrowser = ({ files, total, mapping, type, fileFormat, groupingType, nameFieldPath, urlFieldPath, onRefresh }) => {

  const hasFilter = !!groupingType || !!fileFormat;

  if (files.length === 0) {
    return (
      <span>
        No files available{' '}
        <FontAwesomeIcon
          icon={faSyncAlt}
          onClick={onRefresh}
          style={{ cursor: 'pointer' }}
        />
      </span>
    );
  }

  return (
    <>
      <div>
        <span>
          <i>{total}</i> {total>1?'files':'file'}
        </span>
      </div>
      <HierarchicalFiles
        data={files}
        mapping={mapping}
        type={type}
        groupingType={groupingType}
        hasDataFilter={hasFilter}
        nameFieldPath={nameFieldPath}
        urlFieldPath={urlFieldPath}
      />
    </>
  );
};

export const AsyncHierarchicalFiles = ({
  mapping,
  type,
  repositoryId,
  nameFieldPath,
  urlFieldPath
}) => {
  const location = useLocation();
  const isLive = !!matchPath({ path: '/live/*' }, location.pathname);

  const [files, setFiles] = useState([]);
  const [total, setTotal] = useState(0);
  const [fileFormat, setFileFormat] = useState(null);
  const [groupingType, setGroupingType] = useState(null);

  const group = useSelector(state => state.groups.group);

  const previewResult = useListPreviewFilesQuery(
    {
      repositoryId: repositoryId,
      fileFormat: fileFormat,
      groupingType: groupingType
    },
    { skip: !repositoryId || !isLive }
  );
  const filesResult = useListFilesQuery(
    {
      repositoryId: repositoryId,
      group: group,
      fileFormat: fileFormat,
      groupingType: groupingType
    },
    { skip: !repositoryId || isLive }
  );

  if (!repositoryId) {
    throw new Error('AsyncHierarchicalFiles is missing prop repositoryId');
  }

  const {
    data,
    error,
    isUninitialized,
    //isLoading,
    isFetching,
    //isSuccess,
    isError,
    refetch
  } = isLive ? previewResult : filesResult;

  useEffect(() => {
    if (data) {
      setFiles(data.data);
      setTotal(data.total);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [data]);

  const handleSetFileFormat = value => {
    Matomo.trackEvent('Files', 'Filter', value);
    setFileFormat(value);
  };

  const handleSetGroupingType = value => {
    Matomo.trackEvent('Files', 'Group by', value);
    setGroupingType(value);
  };

  if (isUninitialized || isFetching) {
    return (
      <FetchingFiles />
    );
  }

  if (isError) {
    return (
      <div>
        <span style={{ color: 'var(--code-color)' }}>
          <FontAwesomeIcon icon={faExclamationTriangle} />
          {getError(error)}{' '}
        </span>
        <FontAwesomeIcon
          icon={faSyncAlt}
          onClick={isError}
          style={{ cursor: 'pointer' }}
        />
      </div>
    );
  }

  return (
    <>
      <FileFilter
        title="Filter by"
        useQuery={isLive ? useListPreviewFormatsQuery : useListFormatsQuery}
        queryParameter={isLive ? repositoryId : { repositoryId, group }}
        value={fileFormat}
        onSelect={handleSetFileFormat}
      />
      <FileFilter
        title="Group by"
        useQuery={
          isLive ? useListPreviewGroupingTypesQuery : useListGroupingTypesQuery
        }
        queryParameter={isLive ? repositoryId : { repositoryId, group }}
        value={groupingType}
        onSelect={handleSetGroupingType}
      />
      <FilesBrowser
        files={files}
        total={total}
        mapping={mapping}
        type={type}
        fileFormat={fileFormat}
        groupingType={groupingType}
        nameFieldPath={nameFieldPath}
        urlFieldPath={urlFieldPath}
        onRefresh={refetch}
      />
    </>
  );
};

export default AsyncHierarchicalFiles;
