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

import React, { Component } from "react";
import { store } from "../../../../store";
import "./styles.css";

export class HitStatsPanel extends Component {
  constructor(props) {
    super(props);
    this.state = this.getState();
  }
  getState() {
    const globalState = store.getState();
    const from = (globalState.search.from?globalState.search.from:0) + 1;
    const count = globalState.search.results?(globalState.search.results.hits?(globalState.search.results.hits.hits?globalState.search.results.hits.hits.length:0):0):0;
    const to = from + count - 1;
    return {
      show: globalState.search.isReady && !globalState.search.isLoading,
      hitCount: globalState.search.results?(globalState.search.results.hits?globalState.search.results.hits.total:0):0,
      from: from,
      to: to
    };
  }
  handleStateChange() {
    setTimeout(() => {
      const nextState = this.getState();
      this.setState(nextState);
    });
  }
  componentDidMount() {
    document.addEventListener("state", this.handleStateChange.bind(this), false);
    this.handleStateChange();
  }
  componentWillUnmount() {
    document.removeEventListener("state", this.handleStateChange);
  }
  render() {
    if (!this.state.show) {
      return null;
    }
    if (this.state.hitCount === 0) {
      return (
        <span className="kgs-hitStats">No results were found. Please refine your search.</span>
      );
    }
    return (
      <span className="kgs-hitStats">Viewing <span className="kgs-hitStats-highlight">{this.state.from}-{this.state.to}</span> of <span className="kgs-hitStats-highlight">{this.state.hitCount}</span> results</span>
    );
  }
}