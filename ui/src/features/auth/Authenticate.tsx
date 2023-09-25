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

import React, { useEffect, useRef } from 'react';
import { useDispatch } from 'react-redux';
import {useLocation} from 'react-router-dom';

import BgError from '../../components/BgError/BgError';
import FetchingPanel from '../../components/FetchingPanel/FetchingPanel';
import useAuth from '../../hooks/useAuth';
import { api, getError, tagsToInvalidateOnLogout } from '../../services/api';
import type { ReactNode } from 'react';

interface AuthenticateProps {
  children?: ReactNode;
}

const Authenticate = ({children}: AuthenticateProps) => {

  const initializedRef = useRef(false);

  const {
    isTokenExpired,
    error,
    isError,
    isUninitialized,
    isInitializing,
    isAuthenticated,
    isAuthenticating,
    loginRequired,
    isLogingOut,
    authenticate,
    login
  } = useAuth();

  const location = useLocation();

  const dispatch = useDispatch();

  useEffect(() => {
    if (!initializedRef.current) {
      initializedRef.current = true;
      authenticate();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (isTokenExpired) {
      dispatch(api.util.invalidateTags(tagsToInvalidateOnLogout));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isTokenExpired]);

  const cancelLogin = () => {
    window.location.replace(location.pathname.replace('/live/', '/instances/').replace(/&?group=[^&]+/gi, ''));
  };

  if (isTokenExpired) {
    return (
      <BgError message="Your session has expired" onCancelClick={cancelLogin} cancelLabel="Browse public webpage"  onRetryClick={login} retryLabel="Re-Login" retryVariant="primary" />
    );
  }

  if (isError) {

    if (loginRequired) {
      return (
        <BgError message={getError(error)} onCancelClick={cancelLogin} cancelLabel="Browse public webpage"  onRetryClick={login} retryLabel="Retry" retryVariant="primary" />
      );
    }

    return (
      <BgError message={error} onRetryClick={authenticate} retryLabel="Retry" retryVariant="primary" />
    );
  }

  if (isUninitialized || isInitializing) {
    return (
      <FetchingPanel message={loginRequired?'Initializing authentication...':'Initializing application...'} />
    );
  }

  if (isAuthenticating) {
    return (
      <FetchingPanel message="Authenticating..." />
    );
  }

  if (isLogingOut) {
    return (
      <FetchingPanel message="Logging out..." />
    );
  }

  if (isAuthenticated || !loginRequired) {
    return (
      <>
        {children}
      </>
    );
  }
  return null;
};

export default Authenticate;