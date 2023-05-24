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

export type KeycloakOnLoad = 'login-required'|'check-sso';
export type KeycloakFlow = 'standard'|'implicit'|'hybrid';

export interface KeycloakConfig {
  clientId: string;
  realm: string;
  url: string;
}

export interface KeycloakLogoutOptions {
  redirectUri: string;
}

export interface KeycloakInitSettings {
  onLoad: KeycloakOnLoad;
  flow: KeycloakFlow,
  pkceMethod: string;
  checkLoginIframe: boolean;
}

export interface KeycloakError {
  error: string;
  error_description: string;
}

export interface KeycloakInstance {
  token?: string;
  login: () => void;
  logout: (options: KeycloakLogoutOptions) => void;
  onAuthSuccess: () => void;
  onAuthError: (error: KeycloakError) => void;
  onTokenExpired: () => void;
  init: (settings: KeycloakInitSettings) => Promise<void>;
  updateToken: (interval: number) => Promise<void>;
  onReady: (callback: (authenticated: boolean) => void) => void;
}
