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
import React, { useMemo, useState, useEffect } from "react";
import { useLocation, matchPath } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faExclamationTriangle } from "@fortawesome/free-solid-svg-icons/faExclamationTriangle";
import { faSyncAlt } from "@fortawesome/free-solid-svg-icons/faSyncAlt";

import { useListFilesQuery, useListPreviewFilesQuery, useListFormatsQuery, useListPreviewFormatsQuery, useListGroupingTypesQuery, useListPreviewGroupingTypesQuery, getError } from "../../../app/services/api";

import { FileFilter } from "./FileFilter";
import HierarchicalFiles from "./HierarchicalFiles";

const showMoreStyle = {
  display: "inline",
  padding: 0,
  border: 0,
  verticalAlign: "baseline",
  color: "var(--link-color-1)",
  lineHeight: "1rem"
};

const Label = ({ isAllFetched, number, total }) => {
  if (isAllFetched) {
    return (
      <span>
        <i>{total}</i> files
      </span>
    );
  }

  return (
    <span>
      Showing <i>{number}</i> files out of <i>{total}</i>.
    </span>
  );
};

const FetchingFiles = () => (
  <>
    <div className="spinner-border spinner-border-sm" role="status"></div>
    &nbsp;Retrieving Files...
  </>
);

const FileLabel = ({ showLabel, isAllFetched, length, total }) => {
  if (!showLabel) {
    return null;
  }
  return (
    <>
      <Label isAllFetched={isAllFetched} number={length} total={total} />
      &nbsp;
    </>
  );
};

const FileError = ({ error, handleRetry }) => (
  <div>
    <span style={{ color: "var(--code-color)" }}>
      <FontAwesomeIcon icon={faExclamationTriangle} />
      {error}{" "}
    </span>
    <FontAwesomeIcon
      icon={faSyncAlt}
      onClick={handleRetry}
      style={{ cursor: "pointer" }}
    />
  </div>
);

const NoFiles = ({ isLoading, handleRetry }) => {
  if (isLoading) {
    return <FetchingFiles />;
  }
  return (
    <span>
      No files available{" "}
      <FontAwesomeIcon
        icon={faSyncAlt}
        onClick={handleRetry}
        style={{ cursor: "pointer" }}
      />
    </span>
  );
};

const ShowMoreFiles = ({ isLoading, isAllFetched, showMoreStyle, onClick }) => {
  if (isLoading) {
    return <FetchingFiles />;
  }
  if (!isAllFetched) {
    return (
      <button
        type="button"
        className="btn btn-link"
        onClick={onClick}
        style={showMoreStyle}
      >
        show more
      </button>
    );
  }
  return null;
};

export const AsyncHierarchicalFiles = ({
  mapping,
  group,
  type,
  repositoryId,
  nameFieldPath,
  urlFieldPath
}) => {

  const location = useLocation();
  const isLive = !!matchPath({path:"/live/*"}, location.pathname);

  const [files, setFiles] = useState([]);
  const [total, setTotal] = useState(0);
  const [searchAfter, setSearchAfter] = useState(null);
  const [nextSearchAfter, setNextSearchAfter] = useState(null);
  const [fileFormat, setFileFormat] = useState(null);
  const [groupingType, setGroupingType] = useState(null);

  const previewResult = useListPreviewFilesQuery({
    repositoryId: repositoryId,
    searchAfter: searchAfter,
    fileFormat: fileFormat,
    groupingType: groupingType
  }, { skip: !repositoryId || !isLive});
  const filesResult = useListFilesQuery({
    repositoryId: repositoryId,
    group: group,
    searchAfter: searchAfter,
    fileFormat: fileFormat,
    groupingType: groupingType
  }, { skip: !repositoryId || isLive});

  if (!repositoryId) {
    throw new Error("AsyncHierarchicalFiles is missing prop repositoryId");
  }

  const {
    data,
    error,
    isUninitialized,
    //isLoading,
    isFetching,
    //isSuccess,
    isError,
    refetch,
  } = isLive?previewResult:filesResult;

  useEffect(() => {
    if (data) {
      const items = searchAfter?[...files, ...data.data]:data.data;
      setFiles(items);
      setTotal(data.total);
      setNextSearchAfter(data.searchAfter);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [data]);

  const handleSetFileFormat = useMemo(
    () => value => {
      setFileFormat(value);
      setSearchAfter(null);
    },
    []
  );

  const handleSetGroupingType = useMemo(
    () => value => {
      setGroupingType(value);
      setSearchAfter(null);
    },
    []
  );

  const handleRetry = () => {
    if (!searchAfter) {
      refetch();
    } else {
      setSearchAfter(null);
    }
  };

  const handleShowMore = () => {
    setSearchAfter(nextSearchAfter);
  };

  const isAllFetched = useMemo(() => files.length === total, [files, total]);

  const hasFilter = useMemo(
    () => !!groupingType || !!fileFormat,
    [groupingType, fileFormat]
  );

  const showLabel = useMemo(
    () => total !== 1 || files.length !== 1 || !!groupingType || hasFilter,
    [files, total, groupingType, hasFilter]
  );

  return (
    <>
      <FileFilter
        title="Filter by"
        show={!isUninitialized && !isFetching && !isError}
        useQuery={isLive?useListPreviewFormatsQuery:useListFormatsQuery}
        queryParameter={isLive?repositoryId:{ repositoryId, group }}
        value={fileFormat}
        onSelect={handleSetFileFormat}
      />
      <FileFilter
        title="Group by"
        show={!isUninitialized && !isFetching && !isError}
        useQuery={isLive?useListPreviewGroupingTypesQuery:useListGroupingTypesQuery}
        queryParameter={isLive?repositoryId:{ repositoryId, group }}
        value={groupingType}
        onSelect={handleSetGroupingType}
      />

      {isError ? (
        <FileError error={getError(error)} />
      ) : files.length === 0 ? (
        <NoFiles isLoading={isUninitialized || isFetching} handleRetry={handleRetry} />
      ) : (
        <>
          <div>
            <FileLabel
              showLabel={showLabel}
              isAllFetched={isAllFetched}
              length={files.length}
              total={total}
            />
            <ShowMoreFiles isLoading={isUninitialized || isFetching} isAllFetched={isAllFetched} showMoreStyle={showMoreStyle} onClick={handleShowMore}/>
          </div>
          <HierarchicalFiles
            data={files}
            mapping={mapping}
            group={group}
            type={type}
            groupingType={groupingType}
            hasDataFilter={hasFilter}
            nameFieldPath={nameFieldPath}
            urlFieldPath={urlFieldPath}
          />
        </>
      )}
    </>
  );
};

export default AsyncHierarchicalFiles;
