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

import React, { useEffect, useState, useRef, Suspense } from 'react';
import { Provider, useDispatch } from 'react-redux';
import { BrowserRouter, Route, Routes, Navigate, useLocation, useNavigate, matchPath } from 'react-router-dom';
import 'normalize.css/normalize.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.min.js';
import FetchingPanel from './components/FetchingPanel/FetchingPanel';
import Notification from './components/Notification/Notification';
import notification from './data/notification';
import ErrorBoundary from './features/ErrorBoundary';
import { InfoPanel } from './features/InfoPanel';
import AuthProvider from './features/auth/AuthProvider';
import Authenticate from './features/auth/Authenticate';
import Groups from './features/groups/Groups';
import { setInitialGroup, setUseGroups } from './features/groups/groupsSlice';
import Settings from './features/settings/Settings';
import Theme from './features/theme/Theme';
import { searchToObj, getHashKey } from './helpers/BrowserHelpers';
import Footer from './pages/Footer/Footer';
import Header from './pages/Header/Header';
import type AuthAdapter from './services/AuthAdapter';
import type { Store } from 'redux';

const Search = React.lazy(() => import('./pages/Search'));
const Instance = React.lazy(() => import('./pages/Instance'));
const Preview = React.lazy(() => import('./pages/Preview'));

import './App.css';

const App = ({ authAdapter}: { authAdapter: AuthAdapter; }) => {

  const initializedRef = useRef(false);

  const [isReady, setReady] = useState(false);
  const [loginRequired, setLoginRequired] = useState(false);
  const [isNoSilentSSO, setIsNoSilentSSO] = useState(false);

  const location = useLocation();
  const navigate = useNavigate();

  const dispatch = useDispatch();

  useEffect(() => {
    if (!initializedRef.current) {
      initializedRef.current = true;

      const isLive = !!matchPath({path:'/live/*'}, location.pathname);
      const group = (searchToObj() as {[key:string]: string})['group'];
      const hasGroup = !isLive && (group === 'public' || group === 'curated');
      const hasAuthSession = !!getHashKey('session_state');
      const noSilentSSO = (searchToObj() as {[key:string]: string})['noSilentSSO'];
      setIsNoSilentSSO(noSilentSSO === 'true' && !isLive && !group);

      // search with instance + refresh
      const instance = !hasAuthSession && location.pathname === '/' && !location.hash.startsWith('#error') && location.hash.substring(1);
      if (instance) {
        const url = `/instances/${instance}${hasGroup?('?group=' + group):''}`;
        navigate(url, {replace: true});
      }

      const authMode = hasAuthSession || isLive || hasGroup;
      const useGroups = hasAuthSession && !isLive;
      if(hasGroup) {
        dispatch(setInitialGroup(group));
      }

      if (authMode) {
        if (useGroups) {
          dispatch(setUseGroups());
        }
        setLoginRequired(true);
        setReady(true);
      } else {
        setReady(true);
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!isReady) {
    return null;
  }

  return (
    <AuthProvider adapter={authAdapter} loginRequired={loginRequired} noSilentSSO={isNoSilentSSO} >
      <Theme />
      <Header />
      <main>
        <Notification className={undefined} text={notification} />
        <ErrorBoundary>
          <Settings authAdapter={authAdapter} >
            <Authenticate>
              <Groups>
                <Suspense fallback={<FetchingPanel message="Loading resource..." />}>
                  <Routes>
                    <Route path="/" element={<Search />} />
                    <Route path="/instances/:id" element={<Instance />} />
                    <Route path="/instances/:type/:id" element={<Instance />} />
                    <Route path="/live/:org/:domain/:schema/:version/:id" element={<Preview />} />
                    <Route path="/live/:id" element={<Preview />} />
                    <Route path="*" element={<Navigate to="/" replace={true} />} />
                  </Routes>
                </Suspense>
              </Groups>
            </Authenticate>
          </Settings>
        </ErrorBoundary>
        <InfoPanel />
      </main>
      <Footer />
    </AuthProvider>
  );
};

const Component = ({ store, authAdapter}: { store: Store, authAdapter: AuthAdapter; }) => (
  <Provider store={store}>
    <BrowserRouter>
      <App authAdapter={authAdapter}/>
    </BrowserRouter>
  </Provider>
);
export default Component;