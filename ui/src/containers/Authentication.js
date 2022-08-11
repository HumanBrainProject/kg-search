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

import React, { useEffect, Suspense } from "react";
import { connect } from "react-redux";
import {useLocation, useNavigate, matchPath} from "react-router-dom";

import { setLoginRequired as actionSetLoginRequired, login as actionLogin, setUpAuthentication as actionSetUpAuthentication, loadAuthSettings as actionLoadAuthSettings, clearAuthSettingsError } from "../actions/actions.auth";
import { resetGroups } from "../actions/actions.groups";

import { FetchingPanel } from "../components/Fetching/FetchingPanel";
import { BgError } from "../components/BgError/BgError";

const Groups = React.lazy(() => import("./Groups"));

const Authentication = ({ isUnavailble, settings, error, loginRequired, isLoading, authenticationInitialized, authenticationInitializing, isAuthenticated, isAuthenticating, isLogingOut, login, setUpAuthentication, loadAuthSettings, setLoginRequired, clearError }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const isLogout = !!matchPath({path:"/logout"}, location.pathname);
  const isLive = !!matchPath({path:"/live/*"}, location.pathname);

  const authenticate = () => {
    if (settings) {
      if (authenticationInitialized) {
        login();
      } else {
        setUpAuthentication(settings, loginRequired);
      }
    } else {
      loadAuthSettings(loginRequired);
    }
  };

  useEffect(() => {
    if (!error && !isLoading && !authenticationInitializing && !isAuthenticating && !isAuthenticated && !isLogingOut) {
      if (isLogout && loginRequired) {
        navigate("/");
      }
      if (!isUnavailble && ((!isLogout && !authenticationInitialized) || loginRequired)) {
        authenticate();
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [settings, error, loginRequired, isLoading, authenticationInitialized, authenticationInitializing, isAuthenticated, isAuthenticating, isLogingOut, isLogout]);

  const cancelLogin = () => {
    if (isLive) {
      navigate(location.pathname.replace("/live/", "/instances/"));
    }
    setLoginRequired(false);
  };

  const handleRetry = () => {
    clearError();
  };

  const loginBack = () => {
    setLoginRequired(true);
  };

  if (error) {

    if (loginRequired) {
      return (
        <BgError message={error} onCancelClick={cancelLogin} cancelLabel="Browse public webpage"  onRetryClick={handleRetry} retryLabel="Retry" retryVariant="primary" />
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

  if(isLoading) {
    return (
      <FetchingPanel message={loginRequired?"Retrieving authentication settings...":"Retrieving application configuration..."} />
    );
  }

  if (authenticationInitializing) {
    return (
      <FetchingPanel message={loginRequired?"Initalizing authentication...":"Initalizing application..."} />
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

export default connect(
  state => ({
    isUnavailble: state.auth.isUnavailble,
    settings: state.auth.settings,
    error: state.auth.error,
    loginRequired: state.auth.loginRequired,
    isLoading: state.auth.isLoading,
    authenticationInitialized: state.auth.authenticationInitialized,
    authenticationInitializing: state.auth.authenticationInitializing,
    isAuthenticated: state.auth.isAuthenticated,
    isAuthenticating: state.auth.isAuthenticating,
    isLogingOut: state.auth.isLogingOut
  }),
  dispatch => ({
    setLoginRequired: required => {
      if (!required) {
        dispatch(resetGroups());
      }
      dispatch(actionSetLoginRequired(required));
    },
    clearError: () => {
      dispatch(clearAuthSettingsError());
    },
    login: () => {
      dispatch(actionLogin());
    },
    setUpAuthentication: (settings, loginRequired) => {
      dispatch(actionSetUpAuthentication(settings, loginRequired));
    },
    loadAuthSettings: loginRequired => {
      dispatch(actionLoadAuthSettings(loginRequired));
    }
  })
)(Authentication);