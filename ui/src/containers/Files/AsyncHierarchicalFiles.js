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
import React, { useEffect, useMemo, useState } from "react";
import { connect } from "react-redux";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faExclamationTriangle } from "@fortawesome/free-solid-svg-icons/faExclamationTriangle";
import { faSyncAlt } from "@fortawesome/free-solid-svg-icons/faSyncAlt";

import { FileFilter } from "./FileFilter";
import HierarchicalFiles from "../../components/Field/Files/HierarchicalFiles";
import API from "../../services/API";
import { sessionFailure } from "../../actions/actions";

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
  <div className="spinner-border spinner-border-sm" role="status">
    <span className="sr-only">Retrieving files...</span>
  </div>
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

const ShowMoreFiles = ({ isLoading, isAllFetched, showMoreStyle, fetch }) => {
  if (isLoading) {
    return <FetchingFiles />;
  }
  if (!isAllFetched) {
    return (
      <button
        type="button"
        className="btn btn-link"
        onClick={fetch}
        style={showMoreStyle}
      >
        show more
      </button>
    );
  }
  return null;
};

export const AsyncHierarchicalFilesComponent = ({
  mapping,
  group,
  type,
  filesUrl,
  nameFieldPath,
  urlFieldPath,
  fileFormatsUrl,
  groupingTypesUrl,
  onSessionFailure
}) => {
  const [files, setFiles] = useState([]);
  const [total, setTotal] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchAfter, setSearchAfter] = useState(null);
  const [fileFormat, setFileFormat] = useState(null);
  const [groupingType, setGroupingType] = useState(null);
  const [isReset, setIsReset] = useState(true);

  const fetch = () => {
    if (!filesUrl) {
      throw new Error("AsyncHierarchicalFiles is missing prop url");
    }
    setIsLoading(true);
    setError(null);
    const params = {};
    if (searchAfter) {
      params["searchAfter"] = searchAfter;
    }
    if (groupingType) {
      params["groupingType"] = groupingType;
    }
    if (fileFormat) {
      params["format"] = fileFormat;
    }
    const paramsString = Object.entries(params)
      .map(([k, v]) => `${k}=${encodeURIComponent(v)}`)
      .join("&");
    const url = filesUrl + (paramsString.length ? `?${paramsString}` : "");
    API.axios
      .get(url)
      .then(response => {
        const data = response.data;
        const items = searchAfter ? [...files, ...data.data] : data.data;
        setFiles(items);
        setIsLoading(false);
        setTotal(data.total);
        setSearchAfter(data.searchAfter);
      })
      .catch(e => {
        switch (e?.response?.status) {
        case 401: // Unauthorized
        case 403: // Forbidden
        case 511: {
          // Network Authentication Required
          setIsLoading(false);
          onSessionFailure();
          break;
        }
        case 500:
        case 404:
        default: {
          setError(
            `The service is temporarily unavailable. Please retry in a few minutes. (${
              e.message ? e.message : e
            })`
          );
          setIsLoading(false);
        }
        }
      });
  };

  useEffect(() => {
    if (isReset) {
      setSearchAfter(null);
      setIsReset(false);
    } else {
      fetch();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isReset, groupingType, fileFormat]);

  const handleSetFileFormat = useMemo(
    () => value => {
      setFileFormat(value);
      setIsReset(true);
    },
    []
  );

  const handleSetGroupingType = useMemo(
    () => value => {
      setGroupingType(value);
      setIsReset(true);
    },
    []
  );

  const handleRetry = () => setIsReset(true);

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
        show={!isLoading && !error}
        url={fileFormatsUrl}
        value={fileFormat}
        onSelect={handleSetFileFormat}
        onSessionFailure={onSessionFailure}
      />
      <FileFilter
        title="Group by"
        show={!isLoading && !error}
        url={groupingTypesUrl}
        value={groupingType}
        onSelect={handleSetGroupingType}
        onSessionFailure={onSessionFailure}
      />

      {error ? (
        <FileError error={error} />
      ) : files.length === 0 ? (
        <NoFiles isLoading={isLoading} handleRetry={handleRetry} />
      ) : (
        <>
          <div>
            <FileLabel
              showLabel={showLabel}
              isAllFetched={isAllFetched}
              length={files.length}
              total={total}
            />
            <ShowMoreFiles isLoading={isLoading} isAllFetched={isAllFetched} showMoreStyle={showMoreStyle} fetch={fetch}/>
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

export const AsyncHierarchicalFiles = connect(
  (_, props) => ({
    mapping: props.mapping,
    group: props.group,
    type: props.type,
    nameFieldPath: props.nameFieldPath,
    urlFieldPath: props.urlFieldPath,
    fileFormatsUrl: props.fileFormatsUrl,
    groupingTypesUrl: props.groupingTypesUrl
  }),
  dispatch => ({
    onSessionFailure: () => {
      dispatch(sessionFailure("Your session has expired. Please login again."));
    }
  })
)(AsyncHierarchicalFilesComponent);
