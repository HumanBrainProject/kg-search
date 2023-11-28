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
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import useAuth from '../hooks/useAuth';
import { useGetIsFavoriteQuery, useAddFavoriteMutation, useDeleteFavoriteMutation } from '../services/api';

import './FavoriteButton.css';

const FavoriteButton = () => {

  const { isAuthenticated } = useAuth();

  const instanceId = useSelector(state => state.instance.instanceId);

  const favoriteResult = useGetIsFavoriteQuery({instanceId: instanceId}, { skip: !isAuthenticated || !instanceId});
  const [addFavoriteTrigger, addFavoriteResult] = useAddFavoriteMutation();
  const [deleteFavoriteTrigger, deleteFavoriteResult] = useDeleteFavoriteMutation();

  const [isBookmarked, setIsBookmarked] = useState(undefined);

  useEffect(() => {
    if (favoriteResult.isSuccess) {
      setIsBookmarked(!!favoriteResult.data?.bookmarked);
    } else if (favoriteResult.isUninitialized) {
      setIsBookmarked(undefined);
    }
  }, [favoriteResult.data, favoriteResult.error, favoriteResult.isUninitialized, favoriteResult.isFetching, favoriteResult.isSuccess, favoriteResult.isError]);

  useEffect(() => {
    if (!addFavoriteResult.isUninitialized) {
      if (addFavoriteResult.isSuccess) {
        setIsBookmarked(true);
      }
    }
  }, [addFavoriteResult.isSuccess, addFavoriteResult.isError, addFavoriteResult.isLoading, addFavoriteResult.isUninitialized]);


  useEffect(() => {
    if (!deleteFavoriteResult.isUninitialized) {
      if (deleteFavoriteResult.isSuccess) {
        setIsBookmarked(false);
      }
    }
  }, [deleteFavoriteResult.isSuccess, deleteFavoriteResult.isError, deleteFavoriteResult.isLoading, deleteFavoriteResult.isUninitialized]);


  const handleOnClick = async () => {
    const bookmark = !isBookmarked;
    const trigger = bookmark?addFavoriteTrigger:deleteFavoriteTrigger;
    try {
      trigger(instanceId);
    } catch (e) {
      //console.error(e);
    }
  };

  if (!isAuthenticated || !instanceId) {
    return null;
  }

  const isLoading = favoriteResult.isFetching  || addFavoriteResult.isLoading || deleteFavoriteResult.isLoading;
  const isError = favoriteResult.isError ||  addFavoriteResult.isError || deleteFavoriteResult.isError;
  const isDisabled = favoriteResult.isUninitialized || isLoading || isError;

  return (
    <button className="kgs-favorite_button" onClick={handleOnClick} title={isBookmarked?'Remove from bookmarks':'Add to bookmars'} disabled={isDisabled} aria-label={isBookmarked?'Remove from bookmarks':'Add to bookmarks'}>
      <FontAwesomeIcon icon={isBookmarked?on:off} className="kgs-favorite_icon" />
    </button>
  );
};

export default FavoriteButton;