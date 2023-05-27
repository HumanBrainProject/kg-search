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

import React, { useEffect, useState } from "react";
import { useDispatch } from "react-redux";

import BgError from "../../components/BgError/BgError";
import FetchingPanel from "../../components/FetchingPanel/FetchingPanel";
import KeycloakAuthAdapter from "../../services/KeycloakAuthAdapter";
import Matomo from "../../services/Matomo";
import Sentry from "../../services/Sentry";
import { useGetSettingsQuery, getError } from "../../services/api";
import { setCommit } from "../application/applicationSlice";
import type AuthAdapter from "../../services/AuthAdapter";

import type { JSX } from "react";

interface SettingsProps {
  authAdapter?: AuthAdapter;
  children?: string|JSX.Element|(null|undefined|string|JSX.Element)[];
}

const Settings = ({ authAdapter, children}: SettingsProps) => {

  const [isReady, setReady] = useState(false);

  const dispatch = useDispatch();

  const {
    data: settings,
    error,
    isUninitialized,
    //isLoading,
    isFetching,
    isError,
    refetch,
  } = useGetSettingsQuery(undefined);

  useEffect(() => {
    if (settings && !isReady) {
      Matomo.initialize(settings?.matomo);
      Sentry.initialize(settings?.sentry);
      dispatch(setCommit(settings?.commit));
      if (authAdapter instanceof KeycloakAuthAdapter && settings.keycloak) {
        authAdapter.setConfig(settings.keycloak);
      }
      setReady(true);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [settings, isReady]);

  if (isError) {
    return (
      <BgError message={getError(error)} onRetryClick={refetch} retryLabel="Retry" retryVariant="primary" />
    );
  }

  if(isUninitialized || isFetching) {
    return (
      <FetchingPanel message="Retrieving application configuration..." />
    );
  }

  if (isReady) {
    return (
      <>
        {children}
      </>
    );
  }

  return null;
};

export default Settings;