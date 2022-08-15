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

import React, { useEffect } from "react";
import { connect } from "react-redux";
import { logout } from "../actions/actions.auth";
import { loadGroups as actionLoadGroups, clearGroupError } from "../actions/actions.groups";

import { BgError } from "../components/BgError/BgError";
import { FetchingPanel } from "../components/Fetching/FetchingPanel";
import View from "./View";

const Groups = ({ useGroups, error, isLoading, isReady, loadGroups, onCancel, onRetry}) => {

  useEffect(() => {
    if (useGroups && !isReady && !error && !isLoading) {
      loadGroups();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isReady, error, isLoading]);

  if (error) {
    return (
      <BgError message={error} cancelLabel="Back to search" onCancelClick={onCancel} onRetryClick={onRetry} retryVariant="primary" />
    );
  }

  if (isLoading) {
    return (
      <FetchingPanel message="Retrieving your profile..." />
    );
  }

  if (!useGroups || isReady) {
    return (
      <View />
    );
  }

  return null;
};

export default connect(
  state => ({
    useGroups: state.groups.useGroups && state.auth.isAuthenticated,
    error: state.groups.error,
    isLoading: state.groups.isLoading,
    isReady: state.groups.isReady
  }),
  dispatch => ({
    loadGroups: () => {
      dispatch(actionLoadGroups());
    },
    onCancel: () => {
      dispatch(logout());
    },
    onRetry: () => {
      dispatch(clearGroupError());
    }
  })
)(Groups);
