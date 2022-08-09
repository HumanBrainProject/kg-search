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

import React, { Suspense, useEffect } from "react";
import { Route, Routes, Navigate } from "react-router-dom";
import { connect } from "react-redux";
import ReactPiwik from "react-piwik";
import * as Sentry from "@sentry/browser";


import { FetchingPanel } from "../components/Fetching/FetchingPanel";

const Search = React.lazy(() => import("./Search/Search"));
const Instance = React.lazy(() => import("./Instance/Instance"));
const Preview = React.lazy(() => import("./Instance/Preview"));

const ViewComponent = ({matomo, sentry}) => {

  useEffect(() => {
    new ReactPiwik({ //NOSONAR
      url: (matomo?.url)?matomo.url:process.env.REACT_APP_MATOMO_URL,
      siteId: (matomo?.siteId)?matomo.siteId:process.env.REACT_APP_MATOMO_SITE_ID,
      trackErrors: true
    });

    Sentry.init({
      dsn: (sentry?.url)?sentry.url:process.env.REACT_APP_SENTRY_URL,
      environment: window.location.host
    });

    ReactPiwik.push(["trackPageView"]);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
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
  );
};

export default connect(
  state => ({
    matomo: state.settings.matomo,
    sentry: state.settings.sentry
  })
)(ViewComponent);