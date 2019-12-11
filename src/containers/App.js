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

import React from "react";
import { ConnectedRouter } from "connected-react-router";
import { Route, Switch } from "react-router-dom";

import { history } from "../store";
import { Search } from "./Search";
import { Instance } from "./Instance";
import { NotFound } from "../components/NotFound";
import { Preview } from "./Preview";
import { FetchingPanel } from "./FetchingPanel";
import { ErrorPanel } from "./ErrorPanel";
import { InfoPanel } from "./InfoPanel";
import "./App.css";

class App extends React.Component {
  render() {
    return (
      <ConnectedRouter history={history}>
        <div className="kgs-app">
          <Switch>
            <Route path="/instances/:type/:id" exact component={Instance} />
            <Route path="/previews/:org/:domain/:schema/:version/:id" exact component={Preview} />
            <Route path="/" exact component={Search} />
            <Route component={NotFound} />
          </Switch>
          <FetchingPanel />
          <ErrorPanel />
          <InfoPanel />
        </div>
      </ConnectedRouter>
    );
  }
}

export default App;