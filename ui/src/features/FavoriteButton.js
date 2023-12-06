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
import {faBookmark as off} from '@fortawesome/free-regular-svg-icons/faBookmark';
import {faBookmark as on} from '@fortawesome/free-solid-svg-icons/faBookmark';
import {faCircleNotch} from '@fortawesome/free-solid-svg-icons/faCircleNotch';
import {faExclamationTriangle} from '@fortawesome/free-solid-svg-icons/faExclamationTriangle';
import {faSyncAlt} from '@fortawesome/free-solid-svg-icons/faSyncAlt';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import useAuth from '../hooks/useAuth';
import { useGetIsFavoriteQuery, useAddFavoriteMutation, useDeleteFavoriteMutation, getError } from '../services/api';

import './FavoriteButton.css';

const FavoriteToggle = ({ instanceId, isBookmarked, onClick }) => {

  const addFavoriteMutation = useAddFavoriteMutation();
  const deleteFavoriteMutation = useDeleteFavoriteMutation();

  const [trigger, { error, isLoading, isSuccess, isError, isUninitialized, reset }] = isBookmarked?deleteFavoriteMutation:addFavoriteMutation;

  useEffect(() => {
    if (!isUninitialized && isSuccess) {
      reset();
      onClick(!isBookmarked);
    }
  }, [error, isSuccess, isError, isUninitialized, isBookmarked, reset, onClick]);

  const handleToggle = () => trigger(instanceId);

  if (isError) {
    return (
      <FavoriteError error={getError(error, isBookmarked?'Failed to add bookmark':'Failed to remove bookmark')} onRetry={handleToggle} />
    );
  }

  if (isLoading) {
    return (
      <FavoriteLoading isBookmarked={isBookmarked} />
    );
  }

  const label = isBookmarked?'Remove from bookmarks':'Add to bookmarks';
  return (
    <button className="kgs-favorite_button" onClick={handleToggle} title={label} aria-label={label}>
      <FontAwesomeIcon icon={isBookmarked?on:off} />
    </button>
  );
};


const FavoriteError = ({ error, onRetry }) => (
  <div className="kgs-favorite_error">
    <span className="kgs-favorite_error_message"><FontAwesomeIcon icon={faExclamationTriangle} />&nbsp;{error} </span>
    <FontAwesomeIcon icon={faSyncAlt} onClick={onRetry} style={{cursor: 'pointer'}} title="Retry" />
  </div>
);


const FavoriteLoading = ({ isBookmarked }) => (
  <span className="kgs-favorite_button kgs-favorite_loading" aria-label={isBookmarked?'Removing from bookmarks':'Adding to bookmarks'} >
    <span className="fa-layers fa-fw kgs-favorite_stack">
      <FontAwesomeIcon icon={isBookmarked?on:off} size="2x" />
      <span className={`kgs-favorite_inner ${isBookmarked?'kgs-favorite_on':''}`}>
        <FontAwesomeIcon icon={faCircleNotch} spin size="1x" />
      </span>
    </span>
  </span>
);


const FavoriteButton = () => {

  const { isAuthenticated } = useAuth();

  const instanceId = useSelector(state => state.instance.instanceId);

  const {
    data,
    //currentData,
    error,
    isUninitialized,
    //isLoading,
    isFetching,
    isSuccess,
    isError,
    refetch
  } = useGetIsFavoriteQuery(instanceId, { skip: !isAuthenticated || !instanceId});

  const [isBookmarked, setIsBookmarked] = useState(undefined);

  useEffect(() => {
    if (isSuccess) {
      setIsBookmarked(!!data?.bookmarked);
    }
  }, [data, error, isSuccess, isError]);

  if (!isAuthenticated || !instanceId) {
    return null;
  }

  if (isError) {
    return (
      <FavoriteError error={getError(error, 'Failed to retrieve bookmark status')} onRetry={refetch} />
    );
  }

  if (isUninitialized || isFetching) {
    return (
      <FavoriteLoading isBookmarked={!!isBookmarked} />
    );
  }

  return (
    <FavoriteToggle instanceId={instanceId} isBookmarked={isBookmarked} onClick={setIsBookmarked}  />
  );
};

export default FavoriteButton;