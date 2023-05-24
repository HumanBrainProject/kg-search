/*  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This open source software code was developed in part or in whole in the
 *  Human Brain Project, funded from the European Union's Horizon 2020
 *  Framework Programme for Research and Innovation under
 *  Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 *  (Human Brain Project SGA1, SGA2 and SGA3).
 *
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

import React, { useEffect } from "react";
import Auth from "../../services/Auth";
import AuthContext from "../../contexts/AuthContext";
import AuthAdapter from "../../services/AuthAdapter";
import KeycloakAuthAdapter from "../../services/KeycloakAuthAdapter";
import useAuth from "../../hooks/useAuth";
import KeycloakAuthProvider from "./KeycloakAuthProvider";

/* For debugging purpose only, when running the ui app locally but connecting to
 * backend prod (where keycloak is not allowing localhost),
 * if the authentication is not required, you can bypass the keycloak authentication
 * by setting the following variable to true
*/
const BYPASSS_KEYCLOAK_FOR_LOCAL_DEBUGGING = false;

interface AuthSetupProps {
  adapter?: AuthAdapter;
  children?: string|JSX.Element|(null|undefined|string|JSX.Element)[];
}

const AuthSetup = ({ adapter, children }: AuthSetupProps) => {

  const { isAuthenticated, logout } = useAuth();

  useEffect(() => {
    if (adapter?.unauthorizedRequestResponseHandlerProvider) {
      adapter.unauthorizedRequestResponseHandlerProvider.unauthorizedRequestResponseHandler = () => {
        if (isAuthenticated) {
          logout();
        }
      };
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <>
      {children}
    </>
  );
};

const bypassAuth = {
  tokenProvider: undefined,
  isTokenExpired: false,
  error: undefined,
  isError: false,
  isUninitialized: false,
  isInitialized: true,
  isInitializing: false,
  isAuthenticated: false,
  isAuthenticating: false,
  isLogingOut: false,
  loginRequired: false,
  userId: undefined,
  authenticate: async () => Promise.resolve(undefined),
  login: async () => Promise.resolve(undefined),
  logout: async () => Promise.resolve(undefined)
} as Auth;

interface AuthProviderProps {
  adapter?: AuthAdapter;
  loginRequired?: boolean;
  children?: string|JSX.Element|(null|undefined|string|JSX.Element)[];
}

// loginRequired allow to overrule the onLoad option of the keycloak adapter when the authentidation should differ depenging on the route
const AuthProvider = ({ adapter, loginRequired, children }:AuthProviderProps) => {

  useEffect(() => {
    if (!(adapter instanceof KeycloakAuthAdapter) && adapter?.unauthorizedRequestResponseHandlerProvider) {
      adapter.unauthorizedRequestResponseHandlerProvider.unauthorizedRequestResponseHandler = () => {
        bypassAuth.logout();
      };
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (adapter instanceof KeycloakAuthAdapter) {
    const isLoginRequired = loginRequired !== undefined ? loginRequired : adapter.initOptions?.onLoad === "login-required";
    const canBypassKeyCloak = BYPASSS_KEYCLOAK_FOR_LOCAL_DEBUGGING && window.location.host.startsWith("localhost") && !isLoginRequired;
    if (canBypassKeyCloak) {
      console.info("%cAuth: Keycloak authentication is disabled for local development", "color: #f88900;");
    } else {
      return (
        <KeycloakAuthProvider adapter={adapter} loginRequired={loginRequired} >
          <AuthSetup adapter={adapter}>
            {children}
          </AuthSetup>
        </KeycloakAuthProvider>
      );
    }
  }

  return (
    <AuthContext.Provider value={bypassAuth} >
      <AuthSetup adapter={adapter}>
        {children}
      </AuthSetup>
    </AuthContext.Provider>
  );
};

export default AuthProvider;