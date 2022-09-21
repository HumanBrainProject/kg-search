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

import React, { useEffect, useRef } from "react";
import { useSelector, useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";

import { useGetInstanceQuery, useGetPreviewQuery, trackEvent, getError } from "../../app/services/api";
import { reset } from "./instanceSlice";
import { selectIsCurated } from "../groups/groupsSlice";
import { logout } from "../auth/authSlice";

import FetchingPanel from "../../components/FetchingPanel/FetchingPanel";
import ErrorPanel from "../../components/ErrorPanel/ErrorPanel";

const Instance = ({ isPreview, isSearch, path }) => {

  const cardOpenedUrlRef = useRef(null);

  const navigate = useNavigate();

  const dispatch = useDispatch();

  const id = useSelector(state => state.instance.instanceId);
  const group = useSelector(state => state.groups.group);
  const defaultGroup = useSelector(state => state.groups.defaultGroup);
  const isCurated = useSelector(state => selectIsCurated(state));
  const isSearchInitialized = useSelector(state => state.search.isInitialized);

  useEffect(() => {
    if (id && (!isSearch || isSearchInitialized)) {
      const relativeUrl = `${path}${id}${(group && group !== defaultGroup)?("?group=" + group):""}`;
      if (cardOpenedUrlRef.current !== relativeUrl) {
        cardOpenedUrlRef.current = relativeUrl;
        trackEvent("Card", "Opened", relativeUrl);
      }
    }
  }, [id, group, defaultGroup, path, isSearch, isSearchInitialized]);

  const handleOnCancelClick = () => {
    if (!group) {
      dispatch(logout());
    }
    dispatch(reset());
    navigate(`/${(group && group !== defaultGroup)?("?group=" + group):""}`, {replace:true});
  };

  const previewResult = useGetPreviewQuery(id, { skip: !id || !isPreview || (isSearch && !isSearchInitialized)});
  const instanceResult = useGetInstanceQuery({id: id, group: group}, { skip: !id || isPreview || (isSearch && !isSearchInitialized)});

  const {
    //data,
    error,
    isUninitialized,
    //isLoading,
    isFetching,
    //isSuccess,
    isError,
    refetch,
  } = isPreview?previewResult:instanceResult;

  if (!id || (isSearch && !isSearchInitialized)) {
    return null;
  }

  if (isError) {
    let message = getError(error);
    if (error.status == 404) {
      if (isSearch || isPreview || isCurated) {
        message = "The page you requested was not found.";
      } else {
        const url = `${window.location.protocol}//${window.location.host}${window.location.pathname}?group=curated`;
        const link = `<a href=${url}>${url}</a>`;
        message = `The page you requested was not found. It might not yet be public and authorized users might have access to it in the ${link} or in in-progress view`;
      }
    }

    return (
      <ErrorPanel message={message} cancelLabel={isSearch?"Cancel":"Back to search"} onCancelClick={handleOnCancelClick}  onRetryClick={refetch} retryVariant="primary" />
    );
  }

  if(isUninitialized || isFetching) {
    return (
      <FetchingPanel message="Loading instance..." />
    );
  }

  return null;
};

export default Instance;