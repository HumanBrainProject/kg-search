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
import { rootReducer } from "./reducer/root.reducer";

const isNativeObject = obj => obj !== null && typeof obj === "object" && (!obj.constructor || (obj.constructor && (!obj.constructor.name || obj.constructor.name === "Object")));

const hash = (obj) => {
  if (Array.isArray(obj)) {
    return "[" + obj.reduce((res, e) => {
      return res + (res === ""?"":",") + hash(e);
    }, "") + "]";
  } else if (typeof obj === "object") {
    if (isNativeObject(obj))  {
      return "{" + Object.entries(obj).reduce((res, [key, value]) => {
        if (key === "__proto__") {
          return res;
        }
        return res + (res === ""?"":",") + key + ":" + hash(value);
      }, "") + "}";
    }
    return "<object>";
  } else if (typeof obj === "function") {
    return "<function>";
  } else if (obj === null) {
    return "null";
  } else if (obj === undefined) {
    return "undefined";
  }
  return obj.toString();
};

const createStore = reducer => {

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

export const store = createStore(rootReducer);

export const connect = (mapStateToProps, mapDispatchToProps) => WrappedComponent => {
  class withStoreState extends PureComponent {
    constructor(props) {
      super(props);
      this.shouldHandleChange = false;
      this.unsubscribe = null;
      const nextProps = this.getMapStateToProps(props);
      this.state = {
        props: nextProps
      };
      this.fingerprint = hash(nextProps);
      this.dispatchProps = typeof mapDispatchToProps === "function" && mapDispatchToProps(store.dispatch);
    }
    componentDidMount() {
      this.shouldHandleChange = true;
      this.unsubscribe = store.subscribe(() => {this.handleChange();});
      this.handleChange();
    }
    componentWillUnmount() {
      this.shouldHandleChange = false;
      this.unsubscribe && this.unsubscribe();
    }
    componentDidUpdate() {
      this.handleChange();
    }
    getMapStateToProps(props) {
      const storeState = store.getState();
      const data = typeof mapStateToProps === "function" && mapStateToProps(storeState, props);
      return data;
    }
    handleChange = () => {
      this.shouldHandleChange && this.setState((state, props) => {
        const nextProps = this.getMapStateToProps(props);
        const nextPropFingerprint = hash(nextProps);
        if (nextPropFingerprint !== this.fingerprint) {
          this.fingerprint = nextPropFingerprint;
          return {
            props: nextProps
          };
        }
        return null;
      });
    }
    render() {
      return <WrappedComponent {...this.state.props} {...this.dispatchProps} />;
    }
  }
  return withStoreState;
};