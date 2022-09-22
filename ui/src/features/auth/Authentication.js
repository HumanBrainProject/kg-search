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

import React, { useEffect, Suspense, useRef } from "react";
import { useSelector, useDispatch } from "react-redux";
import {useLocation, useNavigate, matchPath} from "react-router-dom";

import { tagsToInvalidateOnLogout, api, getError } from "../../app/services/api";
import { setLoginRequired, login, setUpAuthentication, clearAuthError } from "./authSlice";
import { resetGroups } from "../groups/groupsSlice";

import FetchingPanel from "../../components/FetchingPanel/FetchingPanel";
import BgError from "../../components/BgError/BgError";

const Groups = React.lazy(() => import("../groups/Groups"));

const Authentication = () => {

  const initializedRef = useRef(false);

  const navigate = useNavigate();
  const location = useLocation();
  const isLogout = !!matchPath({path:"/logout"}, location.pathname);
  const isLive = !!matchPath({path:"/live/*"}, location.pathname);

  const dispatch = useDispatch();

  const isUnavailble = useSelector(state => state.auth.isUnavailble);
  const settings = useSelector(state => state.auth.settings);
  const error = useSelector(state => state.auth.error);
  const loginRequired = useSelector(state => state.auth.loginRequired);
  const authenticationInitialized = useSelector(state => state.auth.authenticationInitialized);
  const authenticationInitializing = useSelector(state => state.auth.authenticationInitializing);
  const isAuthenticated = useSelector(state => state.auth.isAuthenticated);
  const isAuthenticating = useSelector(state => state.auth.isAuthenticating);
  const isLogingOut = useSelector(state => state.auth.isLogingOut);

  const authenticate = () => {
    if (authenticationInitialized) {
      dispatch(login());
    } else {
      dispatch(setUpAuthentication(settings, loginRequired));
    }
  };

  useEffect(() => {
    if (!authenticationInitializing && !isAuthenticating && !isLogingOut) {
      if (error) {
        initializedRef.current = false;
      } else if (isLogout) {
        initializedRef.current = false;
        dispatch(api.util.invalidateTags(tagsToInvalidateOnLogout));
        if (loginRequired) {
          navigate("/");
        }
      } else {
        if (authenticationInitialized && !isAuthenticated && loginRequired && initializedRef.current) { // user clicked on login button
          initializedRef.current = false;
        }
        if (!isUnavailble && !initializedRef.current) {
          initializedRef.current = true;
          authenticate();
        }
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [settings, error, loginRequired, authenticationInitialized, authenticationInitializing, isAuthenticated, isAuthenticating, isLogingOut, isLogout]);

  const requireLogin = required => {
    if (!required) {
      dispatch(resetGroups());
    }
    dispatch(setLoginRequired(required));
  };

  const cancelLogin = () => {
    if (isLive) {
      navigate(location.pathname.replace("/live/", "/instances/"));
    }
    requireLogin(false);
  };

  const handleRetry = () => {
    dispatch(clearAuthError());
  };

  const loginBack = () => {
    requireLogin(true);
  };

  if (error) {

    if (loginRequired) {
      return (
        <BgError message={getError(error)} onCancelClick={cancelLogin} cancelLabel="Browse public webpage"  onRetryClick={handleRetry} retryLabel="Retry" retryVariant="primary" />
      );
    }

    return (
      <BgError message={error} onRetryClick={handleRetry} retryLabel="Retry" retryVariant="primary" />
    );
  }

  if (isLogout) {
    return (
      <BgError message="You have been successfully logged out" onRetryClick={loginBack} retryLabel="Login" retryVariant="primary" />
    );
  }

  if (authenticationInitializing) {
    return (
      <FetchingPanel message={loginRequired?"Initializing authentication...":"Initializing application..."} />
    );
  }

  if (isAuthenticating) {
    return (
      <FetchingPanel message="Authenicating..." />
    );
  }

  if (isLogingOut) {
    return (
      <FetchingPanel message="Loging out..." />
    );
  }

  if (isUnavailble || isAuthenticated || (!loginRequired && authenticationInitialized)) {
    return (
      <Suspense fallback={<FetchingPanel message="Loading resource..." />}>
        <Groups />
      </Suspense>
    );
  }

  return null;
};

export default Authentication;