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

import React, { useEffect } from 'react';
import useAuth from '../../hooks/useAuth';
import DefaultMockAuthProvider from './DefaultMockAuthProvider';
import type AuthAdapter from '../../services/AuthAdapter';
import type { AuthProviderProps as AuthProviderPropsType } from '../../services/AuthProvider';
import type { JSX } from 'react';

/* For debugging purpose only, when running the ui app locally but connecting to
   * backend prod (where keycloak is not allowing localhost),
   * if the authentication is not required, you can bypass the keycloak authentication
   * by setting the env variable REACT_APP_BYPASSS_AUTH to true
   */

interface AuthSetupProps {
  adapter: AuthAdapter;
  children?: string | JSX.Element | (null | undefined | string | JSX.Element)[];
}

const AuthSetup = ({ adapter, children }: AuthSetupProps) => {
  const { isAuthenticated, logout } = useAuth();

  useEffect(() => {
    if (adapter.unauthorizedRequestResponseHandlerProvider) {
      adapter.unauthorizedRequestResponseHandlerProvider.unauthorizedRequestResponseHandler = () => {
        if (isAuthenticated) {
          logout();
        }
      };
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return <>{children}</>;
};

interface AuthProviderProps extends AuthProviderPropsType {
  noSilentSSO?: boolean;
}

// loginRequired allow to overrule the onLoad option of the keycloak adapter when the authentidation should differ depenging on the route
const AuthProvider = ({ adapter, loginRequired, noSilentSSO, children }: AuthProviderProps) => {
  const isLoginRequired = loginRequired ?? adapter.initOptions?.onLoad === 'login-required';
  const canBypassAuth = noSilentSSO || (process.env.REACT_APP_BYPASSS_AUTH === 'true' && window.location.host.startsWith('localhost') && !isLoginRequired);
  if (canBypassAuth) {
    console.info('%cAuth: Authentication is disabled for local development', 'color: #f88900;');
  }
  const Provider = canBypassAuth?DefaultMockAuthProvider:adapter.authProvider;

  return (
    <Provider adapter={adapter} loginRequired={loginRequired}>
      <AuthSetup adapter={adapter}>{children}</AuthSetup>
    </Provider>
  );
};

export default AuthProvider;
