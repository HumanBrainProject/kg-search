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
import {useLocation, useNavigate, matchPath} from "react-router-dom";
import * as actionsAuth from "../../actions/actions.auth";
import * as actionsGroups from "../../actions/actions.groups";

import { BgError } from "../../components/BgError/BgError";
import View from "../View";

const Authentication = ({ defaultGroup, authEndpoint, error, authenticatedMode, isLoading, authenticationInitialized, authenticationInitializing, isAuthenticated, isAuthenticating, isloginOut, login, setUpAuthenticationAndLogin, loadAuthEndpoint, setAuthMode }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const isLogout = !!matchPath({path:"/logout"}, location.pathname);
  const isLive = !!matchPath({path:"/live/*"}, location.pathname);

  const authenticate = () => {
    if (authEndpoint) {
      if (authenticationInitialized) {
        login();
      } else {
        setUpAuthenticationAndLogin(authEndpoint);
      }
    } else {
      loadAuthEndpoint();
    }
  };

  useEffect(() => {
    if (!error && authenticatedMode && !isLoading && !authenticationInitializing && !isAuthenticating && !isAuthenticated && !isloginOut) {
      if (isLogout) {
        navigate("/");
      }
      authenticate();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [authEndpoint, error, authenticatedMode, isLoading, authenticationInitialized, authenticationInitializing, isAuthenticated, isAuthenticating, isloginOut, isLogout]);

  const loginBack = () => {
    setAuthMode(true);
  };

  const cancelLogin = () => {
    if (isLive) {
      navigate(location.pathname.replace("/live/", "/instances/"));
    }
    setAuthMode(false, defaultGroup);
  };

  if (error) {
    return (
      <BgError message={error} onCancelClick={cancelLogin} cancelLabel="Cancel authentication"  onRetryClick={loginBack} retryLabel="Login" retryVariant="primary" />
    );
  }

  if (isLogout) {
    return (
      <BgError message="You have been successfully logged out" onRetryClick={loginBack} retryLabel="Login" retryVariant="primary" />
    );
  }

  if (!isloginOut && (!authenticatedMode || isAuthenticated)) {
    return (
      <View />
    );
  }

  return null;
};

export default connect(
  state => ({
    defaultGroup: state.groups.defaultGroup,
    authEndpoint: state.auth.authEndpoint,
    error: state.auth.error,
    authenticatedMode: state.auth.authenticatedMode,
    isLoading: state.auth.isLoading,
    authenticationInitialized: state.auth.authenticationInitialized,
    authenticationInitializing: state.auth.authenticationInitializing,
    isAuthenticated: state.auth.isAuthenticated,
    isAuthenticating: state.auth.isAuthenticating,
    isloginOut: state.auth.isloginOut
  }),
  dispatch => ({
    setAuthMode: (active, defaultGroup) => {
      if (!active) {
        dispatch(actionsGroups.setInitialGroup(defaultGroup));
        dispatch(actionsGroups.setGroup(defaultGroup));
      }
      dispatch(actionsAuth.setAuthMode(active));
    },
    login: () => {
      dispatch(actionsAuth.login);
    },
    setUpAuthenticationAndLogin: authEndpoint => {
      dispatch(actionsAuth.setUpAuthenticationAndLogin(authEndpoint));
    },
    loadAuthEndpoint: () => {
      dispatch(actionsAuth.loadAuthEndpoint());
    }
  })
)(Authentication);
