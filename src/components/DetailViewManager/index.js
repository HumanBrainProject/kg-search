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
import { store, dispatch } from "../../store";
import * as actions from "../../actions";
import { isMobile } from "../../Helpers/BrowserHelpers";
import { DetailView } from "./components/DetailView";
import "./styles.css";

export class DetailViewManager extends Component {
  constructor(props) {
    super(props);
    this.state = {
      hits: {
        previousHits: [],
        currentHit: null
      },
      detail: {
        currentViewId: 1,
        data: [null, null, null, null, null]
      }
    };
  }
  setPreviousHit() {
    dispatch(actions.setPreviousHit());
  }
  clearAllHits() {
    dispatch(actions.clearAllHits());
  }
  _keyupHandler(event) {
    if (this.state.hits.currentHit) {
      if (event.keyCode === 27) {
        event.preventDefault();
        this.clearAllHits();
      } else if (event.keyCode === 8) {
        event.preventDefault();
        this.setPreviousHit();
      }
    }
  }
  handleStateChange() {
    setTimeout(() => {
      const nextState = store.getState();

      // hit change
      if (!(nextState.hits.currentHit && this.state.hits.currentHit
          && nextState.hits.currentHit._type === this.state.hits.currentHit._type
          && nextState.hits.currentHit._id === this.state.hits.currentHit._id)
        && (nextState.hits.currentHit || this.state.hits.currentHit)) {

        let viewId = 1;
        let data = [null, null, null, null, null];

        // Detail closed
        if (this.state.hits.currentHit && !nextState.hits.currentHit) {

          viewId = 1;
          data = [null, null, null, null, null];

        // Detail opening
        } else if (!this.state.hits.currentHit && nextState.hits.currentHit) {

          viewId = 1;
          data = [nextState.hits.currentHit, null, null, null, null];

        // next detail
        } else if (nextState.hits.previousHits.length === this.state.hits.previousHits.length + 1) {

          viewId = this.state.detail.currentViewId % 5 + 1;
          data = [...this.state.detail.data];
          data[viewId-1] = nextState.hits.currentHit;

        // any previous detail
        } else {

          viewId = this.state.detail.currentViewId === 1?5:(this.state.detail.currentViewId-1) % 5;
          data = [...this.state.detail.data];
          data[viewId-1] = nextState.hits.currentHit;
        }

        const state = Object.assign({}, nextState, {
          detail: {
            currentViewId: viewId,
            data: data
          }
        });
        this.setState(state);
      }
    });
  }
  shouldComponentUpdate(nextProps, nextState) {
    // View change
    if (nextState.detail.currentViewId !== this.state.detail.currentViewId) {
      return true;
    }

    // Detail opening
    if (nextState.hits.currentHit && !this.state.hits.currentHit) {
      return true;
    }

    // Detail closed
    if (!nextState.hits.currentHit && this.state.hits.currentHit) {
      return true;
    }

    /*
    // hit change
    if (!(nextState.hits.currentHit && this.state.hits.currentHit
      && nextState.hits.currentHit._type === this.state.hits.currentHit._type
      && nextState.hits.currentHit._id === this.state.hits.currentHit._id))
        return true;

    // preview hits change
    if (nextState.hits.previousHits.length && !this.state.hits.previousHits.length)
      return true;
    if (!nextState.hits.previousHits.length && this.state.hits.previousHits.length)
      return true;
    */
    return false;
  }
  componentDidMount() {
    document.addEventListener("state", this.handleStateChange.bind(this), false);
    this.handleStateChange();
    if (!isMobile) {
      window.addEventListener("keyup", this._keyupHandler.bind(this), false);
    }
  }
  componentWillUnmount() {
    document.removeEventListener("state", this.handleStateChange);
    if (!isMobile) {
      window.removeEventListener("keyup", this._keyupHandler);
    }
  }
  render() {
    //window.console.debug("DetailView rendering: " + this.state.hits.currentHit);
    if (!this.state.hits.currentHit) {
      return null;
    }
    return (
      <div data-viewId={this.state.detail.currentViewId} data-hasPreviousHit={!!this.state.hits.previousHits.length} className="kgs-detailViewManager">
        <div className="kgs-detailViewManager__views">
          <DetailView viewId="1" data={this.state.detail.data[0]} isActive={!!this.state.hits.currentHit && this.state.detail.currentViewId === 1} onPreviousClick={this.setPreviousHit} onCloseClick={this.clearAllHits} />
          <DetailView viewId="2" data={this.state.detail.data[1]} isActive={!!this.state.hits.currentHit && this.state.detail.currentViewId === 2} onPreviousClick={this.setPreviousHit} onCloseClick={this.clearAllHits} />
          <DetailView viewId="3" data={this.state.detail.data[2]} isActive={!!this.state.hits.currentHit && this.state.detail.currentViewId === 3} onPreviousClick={this.setPreviousHit} onCloseClick={this.clearAllHits} />
          <DetailView viewId="4" data={this.state.detail.data[3]} isActive={!!this.state.hits.currentHit && this.state.detail.currentViewId === 4} onPreviousClick={this.setPreviousHit} onCloseClick={this.clearAllHits} />
          <DetailView viewId="5" data={this.state.detail.data[4]} isActive={!!this.state.hits.currentHit && this.state.detail.currentViewId === 5} onPreviousClick={this.setPreviousHit} onCloseClick={this.clearAllHits} />
        </div>
      </div>
    );
  }
}
