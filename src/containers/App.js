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
import { connect } from "react-redux";
import { ConnectedRouter } from "connected-react-router";
import { store, history } from "../store";
import { MasterDetail } from "./MasterDetail";
import { FetchingPanel } from "./FetchingPanel";
import { ErrorPanel } from "./ErrorPanel";
import { InfoPanel } from "./InfoPanel";
import * as actions from "../actions";
import "./App.css";

const searchAPIHost = "https://kg.ebrains.eu";

class App extends React.Component {
  componentDidMount() {
    this.props.loadDefinition(searchAPIHost);
  }

  render() {
    return (
      <ConnectedRouter history={history}>
        <div className="kgs-app">
          <MasterDetail />
          <FetchingPanel />
          <ErrorPanel />
          <InfoPanel />
        </div>
      </ConnectedRouter>
    );
  }
}

const mapDispatchToProps = dispatch => ({
  loadDefinition: searchAPIHost => dispatch(actions.loadDefinition(searchAPIHost))
});

export default connect(
  null,
  mapDispatchToProps
)(App);
