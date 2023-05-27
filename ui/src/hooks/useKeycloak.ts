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

import Keycloak from "keycloak-js";
import { useState } from "react";
import type Auth from "../services/Auth";
import type KeycloakAuthAdapter from "../services/KeycloakAuthAdapter";
import type { KeycloakError } from "keycloak-js";

const useKeycloak = (adapter: KeycloakAuthAdapter, loginRequired?: boolean) : Auth => {

  const [isUninitialized, setUninitialized] = useState(true);
  const [isInitialized, setInitialized] = useState(false);
  const [isInitializing, setInitializing] = useState(false);
  const [error, setError] = useState<string|undefined>(undefined);
  const [isError, setIsError] = useState(false);
  const [isTokenExpired, setTokenExpired] = useState<boolean|undefined>(undefined);
  const [isAuthenticated, setAuthenticated] = useState(false);
  const [isAuthenticating, setAuthenticating] = useState(false);
  const [isLogingOut, setLogingOut] = useState(false);
  const [userId, setUserId] = useState<string|undefined>(undefined);

  const authenticate = async () => {
    if (adapter.config?.url) {
      setUninitialized(false);
      setInitializing(true);
      setAuthenticating(true);
      setError(undefined);
      setIsError(false);
      try {
        const keycloak = Keycloak(adapter.config);
        adapter.setKeycloak(keycloak);
        keycloak.onReady = (authenticated: boolean) => {
          setAuthenticated(authenticated);
          setInitialized(true);
          setInitializing(false);
          setAuthenticating(false);
          setTokenExpired(false);
        };
        keycloak.onAuthSuccess = () => {
          setUserId(keycloak.subject);
          setAuthenticated(true);
          setAuthenticating(false);
          setTokenExpired(false);
        };
        keycloak.onAuthError = (e: KeycloakError) => {
          keycloak.clearToken();
          setUserId(undefined);
          setTokenExpired(undefined);
          const message = (e?.error_description)?e.error_description:"Failed to authenticate";
          setError(message);
          setIsError(true);
          setInitializing(false);
          setAuthenticated (false);
          setAuthenticating(false);
          setLogingOut(false);
        };
        keycloak.onTokenExpired = () => {
          keycloak
            .updateToken(30)
            .catch(() => {
              keycloak.clearToken();
              setUserId(undefined);
              setTokenExpired(true);
              setAuthenticated(false);
              setAuthenticating(false);
              setLogingOut(false);
            });
        };
        const initOptions = adapter.initOptions?{
          ...adapter.initOptions,
          onLoad: loginRequired?"login-required":adapter.initOptions.onLoad,
          checkLoginIframe: !!adapter.initOptions.checkLoginIframe && !window.location.host.startsWith("localhost") // avoid CORS error with UI running on localhost with Firefox
        }:{};
        keycloak
          .init(initOptions)
          .catch(() => {
            setError("Failed to initialize authentication");
            setIsError(true);
            setInitialized(false);
            setInitializing (false);
            setAuthenticating(false);
            setLogingOut(false);
          });
      } catch (e) { // if keycloak script url return unexpected content
        setError("Failed to initialize authentication");
        setInitialized(false);
        setInitializing(false);
        setAuthenticating(false);
      }
    } else {
      //throw new Error("keycloak cannot be initialized due to missing/uncomplete config!");
      setError("Failed to initialize authentication");
      setInitialized(false);
      setInitializing(false);
      setAuthenticating(false);
    }
  };

  const login = async (): Promise<void> => {
    if (!adapter.keycloak || isUninitialized || isInitializing || isAuthenticating || isError) {
      throw new Error("login cannot be called when keycloak is not initialized!");
    }
    await adapter.keycloak.login();
  };

  const logout = async (): Promise<void> => {
    if (!adapter.keycloak || isUninitialized || isInitializing || isAuthenticating || isError) {
      throw new Error("logout cannot be called when keycloak is not initialized!");
    }
    setLogingOut(true);
    adapter.keycloak.clearToken();
    const options = adapter.redirectUri ? {redirectUri: adapter.redirectUri} : undefined;
    await adapter.keycloak.logout(options);
    setLogingOut(false);
    setTokenExpired(true);
    setAuthenticated(false);
  };

  return {
    tokenProvider: adapter.tokenProvider,
    isTokenExpired: isTokenExpired,
    error: error,
    isError: isError,
    isUninitialized: isUninitialized,
    isInitialized: isInitialized,
    isInitializing: isInitializing,
    isAuthenticated: isAuthenticated,
    isAuthenticating: isAuthenticating,
    isLogingOut: isLogingOut,
    loginRequired: loginRequired ?? adapter.initOptions?.onLoad === "login-required",
    userId: userId,
    authenticate: authenticate,
    login: login,
    logout: logout
  };

};

export default useKeycloak;