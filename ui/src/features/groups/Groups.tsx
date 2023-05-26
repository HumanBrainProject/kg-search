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

import React, { JSX } from "react";
import { useSelector } from "react-redux";

import { RootState } from "../../services/store";
import { useListGroupsQuery, getError } from "../../services/api";

import BgError from "../../components/BgError/BgError";
import FetchingPanel from "../../components/FetchingPanel/FetchingPanel";
import useAuth from "../../hooks/useAuth";

interface GroupsProps {
  children?: string|JSX.Element|(null|undefined|string|JSX.Element)[];
}

const Groups = ({ children }: GroupsProps) => {

  const { logout, isAuthenticated } = useAuth();

  const useGroups = useSelector((state:RootState) => state.groups.useGroups) && isAuthenticated;

  const {
    //data: groups,
    error,
    isUninitialized,
    //isLoading,
    isFetching,
    isSuccess,
    isError,
    refetch,
  } = useListGroupsQuery(null, { skip: !useGroups });

  if (isError) {
    return (
      <BgError message={getError(error)} cancelLabel="Back to search" onCancelClick={logout} onRetryClick={refetch} retryVariant="primary" />
    );
  }

  if (useGroups && (isUninitialized || isFetching)) {
    return (
      <FetchingPanel message="Retrieving your profile..." />
    );
  }

  if (!useGroups || isSuccess) {
    return (
      <>
        {children}
      </>
    );
  }

  return null;
};

export default Groups;