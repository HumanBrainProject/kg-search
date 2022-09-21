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

import React, { useEffect, useRef } from "react";
import { useSelector, useDispatch } from "react-redux";
import { useLocation, useNavigate, matchPath } from "react-router-dom";

import notification from "../data/notification";

import { setApplicationReady } from "../features/application/applicationSlice";
import { setLoginRequired } from "../features/auth/authSlice";
import { setInitialGroup, setUseGroups } from "../features/groups/groupsSlice";

import { searchToObj, getHashKey } from "../helpers/BrowserHelpers";

import Notification from "../components/Notification/Notification";

import Settings from "../features/settings/Settings";
import { InfoPanel } from "../features/InfoPanel";
import ErrorBoundary from "../features/ErrorBoundary";
import Footer from "./Footer/Footer";
import Header from "./Header/Header";
import Theme from "../features/theme/Theme";

import "./App.css";

const App = () => {

  const initializedRef = useRef(false);

  const location = useLocation();
  const navigate = useNavigate();

  const dispatch = useDispatch();

  const isApplicationReady = useSelector(state => state.application.isReady);

  useEffect(() => {
    if (!initializedRef.current) {
      initializedRef.current = true;

      const isLive = !!matchPath({path:"/live/*"}, location.pathname);
      const group = searchToObj()["group"];
      const hasGroup = !isLive && (group === "public" || group === "curated");
      const hasAuthSession = !!getHashKey("session_state");

      // search with instance + refresh
      const instance = !hasAuthSession && location.pathname === "/" && !location.hash.startsWith("#error") && location.hash.substring(1);
      if (instance) {
        const url = `/instances/${instance}${hasGroup?("?group=" + group):""}`;
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
        dispatch(setLoginRequired(true));
        dispatch(setApplicationReady());
      } else {
        dispatch(setApplicationReady());
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!isApplicationReady) {
    return null;
  }

  return (
    <>
      <Theme />
      <Header />
      <main>
        <Notification text={notification} />
        <ErrorBoundary>
          <Settings/>
        </ErrorBoundary>
        <InfoPanel />
      </main>
      <Footer />
    </>
  );
};

export default App;