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

import * as actions from "../actions";
import SearchManager from "./search.manager";
import { generateKey, getAuthUrl } from "../helpers/OIDCHelpers";

export default class AppManager {
  constructor(store, options) {
    this.store = store;
    this.search = new SearchManager(store);
    this.isStarted = false;
    this.initialHitReference = null;
    this.hitIssuer = null;
    this.previousStateHits = store.getState().hits;
    this.isEventFiredByBrowserNav = false;
    this.isEventFiredByAppNav = false;
    this.locationHref = window.location.href;
    this.unsubscribe = null;

    // check initial hit reference in url
    const m = window.location.href.match(/(.*)#(.*)$/);
    if (m && m.length === 3) {
      if (m[2].indexOf("access_token=") !== -1) {
        const m2 = m[2].match(/^access_token=([^&]+)&.*$/);
        if (m2 && m2.length === 2) {
          const accessToken = m2[1];
          store.dispatch(actions.authenticate(accessToken));
        }
      } else {
        this.initialHitReference = m[2];
      }
      const historyState = window.history.state;
      window.history.replaceState(historyState, "Knowledge Graph Search", m[1]);
    }

    this.start(options);
  }
  start(options) {
    if (!this.isStarted) {
      this.isStarted = true;
      const store = this.store;
      this.unsubscribe = store.subscribe(() => {this.handleStateChange();});
      window.addEventListener("hashchange", this.catchBrowserNavigationChange.bind(this), false);
      store.dispatch(actions.initializeConfig(options));
    }
  }
  stop() {
    this.unsubscribe && this.unsubscribe();
    window.removeEventListener("hashchange", this.catchBrowserNavigationChange);
  }
  get searchkit() {
    return this.search && this.search.searchkit;
  }
  handleStateChange = () => {
    const store = this.store;
    const state = store.getState();

    if (state.auth.authenticate) {
      const stateKey = generateKey();
      const nonceKey = generateKey();
      window.location.href = getAuthUrl(state.configuration.oidcUri, state.configuration.oidcClientId, stateKey, nonceKey);
    }

    if (!state.application.isReady) {

      if (state.search.isReady) {

        if (!state.definition.isReady) {
          if (!state.definition.hasRequest && !state.definition.isLoading && !state.definition.hasError) {
            store.dispatch(actions.loadDefinition());
          }
          return;
        }

        if (!state.indexes.isReady) {
          if (!state.indexes.hasRequest && !state.indexes.isLoading && !state.indexes.hasError) {
            store.dispatch(actions.loadIndexes());
          }
          return;
        }

        store.dispatch(actions.setApplicationReady(true));
      }

      return;
    }

    if (state.search.initialRequestDone && this.initialHitReference) {
      //window.console.log("AppManager load initial Hit: " + this.initialHitReference);
      store.dispatch(actions.loadHit(this.initialHitReference));
      this.initialHitReference = null;

      return;
    }

    //window.console.debug("App Manager state change", state);

    //Remove the ability to scroll the body when the modal is open
    this.setBodyScrolling(!state.hits.currentHit && !state.application.info);

    // store detail view laucher button in order to set back focus to it when detail popup close
    this.manageHitFocus(state.hits.currentHit);

    this.manageHistory(state);
  }
  setBodyScrolling(enableScrolling) {
    //Remove the ability to scroll the body when the modal is open
    if (enableScrolling) {
      document.documentElement.style.overflow = "";
      document.body.style.overflow = "";
    } else {
      document.documentElement.style.overflow = "hidden";
      document.body.style.overflow = "hidden";
    }
  }
  manageHitFocus(hit) {
    if (hit) {
      // store detail view laucher button in order to set back focus to it when detail popup close
      this.hitIssuer = hit;

    } else if (this.hitIssuer) {
      // on detail popup close put back focus to issuer
      const node = document.body.querySelector("button[data-type=\"" + this.hitIssuer._type + "\"][data-id=\"" + this.hitIssuer._id +"\"]");
      node && node.focus();
    }
  }
  manageHistory(state) {
    // check history todos
    const pauseSearchkitHistoryListening = state.hits.currentHit && !this.previousStateHits.currentHit;
    const resumeSearchkitHistoryListening = !state.hits.currentHit && this.previousStateHits.currentHit;
    const pushHistoryState = (state.hits.currentHit && !this.previousStateHits.currentHit)
        || state.hits.previousHits.length > this.previousStateHits.previousHits.length;
    const backHistoryCounts = ((context, previousState, state) => {
      if (context.isEventFiredByBrowserNav) {
        context.isEventFiredByBrowserNav = false;
        return 0;
      }
      let backs = previousState.previousHits.length - state.previousHits.length;
      if (backs < 0) {
        backs = 0;
      }
      if (previousState.currentHit && !state.currentHit) {
        backs++;
      }
      return backs<0?0:backs;
    })(this, this.previousStateHits, state.hits);

    this.previousStateHits = Object.assign({}, state.hits);

    // apply history todos
    if (pauseSearchkitHistoryListening) {
      this.search && this.search.searchkit && this.search.searchkit.unlistenHistory();
    }
    if (pushHistoryState) {
      //window.console.debug(new Date().toLocaleTimeString() + ": new history");
      const historyState = window.history.state;
      window.history.pushState(historyState, "Knowledge Graph Search", window.location.href.replace(/#.*$/,"") + "#" + state.hits.currentHit._type + "/" + state.hits.currentHit._id);
    }
    if (backHistoryCounts) {
      //window.console.debug(new Date().toLocaleTimeString() + ": back history: " + backHistoryCounts);
      this.isEventFiredByAppNav = true;
      [...Array(backHistoryCounts)].forEach(() => window.history.back());
    }
    if (resumeSearchkitHistoryListening && this.searchkit.history) {
      setTimeout(() => {
        this.search && this.search.searchkit && this.search.searchkit.listenToHistory.call(this.search.searchkit);
      },0);
    }
  }
  setCurrentHitFromBrowserLocation() {
    if (this.isStarted) {
      const store = this.store;
      store.dispatch(actions.setCurrentHitFromBrowserLocation());
    }
  }
  catchBrowserNavigationChange() {
    if (this.isEventFiredByAppNav) {
      this.isEventFiredByAppNav = false;
    } else {
      //window.console.debug(new Date().toLocaleTimeString() + ": nav change");
      this.isEventFiredByBrowserNav = true;
      this.setCurrentHitFromBrowserLocation();
    }
  }
}