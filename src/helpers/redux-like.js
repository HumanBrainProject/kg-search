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

import { hash } from "./hash";

export const createStore = reducer => {

  let state = reducer(null, {});
  let fingerprint = hash(state);

  document.addEventListener("action", e => {
    setTimeout(() => {
      const nextState = reducer(state, e.detail);
      const nextStateFingerprint = hash(nextState);
      if (nextStateFingerprint !== fingerprint) {
        state = nextState;
        fingerprint = nextStateFingerprint;
        document.dispatchEvent(new CustomEvent("state"));
      }
    });
  }, false);

  let store = {
    getState: () => {
      return state;
    }
  };

  const dispatch = payload => {
    setTimeout(() => {
      if (typeof payload === "function") {
        payload(dispatch, store.getState());
      } else {
        document.dispatchEvent(
          new CustomEvent("action", { detail: payload})
        );
      }
    });
  };

  const subscribe = subscriber => {

    if (typeof subscriber === "function") {
      //document.addEventListener("state", setTimeout(subscriber), false);
      document.addEventListener("state", subscriber, false);
    }

    const unsubscribe = () => {
      if (typeof subscriber === "function") {
        document.removeEventListener("state", subscriber);
      }
    };
    return unsubscribe;
  };

  store.dispatch = dispatch;
  store.subscribe = subscribe;
  return store;
};

export const combineReducers = reducers => {
  return (state, action) => {
    /*
    if (action.type) {
      window.console.debug("Reducer action: " + action.type);
    }
    */
    let nextState = {};
    Object.keys(reducers)
      .map(k => {
        return {p: k, reducer: reducers[k]};
      })
      .forEach(({p, reducer}) => {
        nextState[p] = reducer(state?state[p]:undefined, action);
      });
    return nextState;
  };
};