/*
*   Copyright (c) 2018, EPFL/Human Brain Project PCO
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
*/

import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import './index.css';

// GLOBAL CONSTANTS DEFINED OUTSIDE THE APP
const SearchApiHostEnvKey        = "SearchApiHost";        // "https://kg.humanbrainproject.org"

// APP PARAMETERS
const hitsPerPage = 20;
const searchOnLoad = true; // when set to true it will trigger an initial search after initilizsation
const enableAutoWildcardAndFuzzySearch = true;
const oidcUri = "https://services.humanbrainproject.eu/oidc/authorize";
const oidcClientId = "nexus-kg-search";

const config = {
  searchApiHost: window[SearchApiHostEnvKey]?window[SearchApiHostEnvKey]:"",
  hitsPerPage: hitsPerPage,
  searchOnLoad: searchOnLoad,
  enableAutoWildcardAndFuzzySearch,
  oidcUri: oidcUri,
  oidcClientId: oidcClientId
};

ReactDOM.render(
  <App config={config} />,
  document.getElementById('root')
);
