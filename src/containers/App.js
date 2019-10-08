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

import React, { PureComponent } from "react";
import { Provider } from "react-redux";
import { store } from "../store";
import AppManager from "../services/app.manager";
import { MasterDetail } from "./MasterDetail";
import { FetchingPanel } from "./FetchingPanel";
import { ErrorPanel } from "./ErrorPanel";
import { InfoPanel } from "./InfoPanel";
import "./App.css";

export default class App extends PureComponent {
  constructor(props) {
    super(props);
    this.manager = new AppManager(store, props.config || {});
  }
  componentDidMount() {
    this.manager.start();
  }
  componentWillUnmount() {
    this.manager.stop();
  }
  render() {
    //window.console.debug("App rendering...");
    return (
      <Provider store={store}>
        <div className="kgs-app">
          <MasterDetail manager={this.manager} />
          <FetchingPanel />
          <ErrorPanel />
          <InfoPanel />
        </div>
      </Provider>
    );
  }
}