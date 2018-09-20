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

const createStore = reducer => {

  let state = reducer(null, {});

  document.addEventListener("action", e => {
    setTimeout(() => {
      const nextState = reducer(state, e.detail);
      if (JSON.stringify(nextState) !== JSON.stringify(state)) {
        state = nextState;
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
      this.state = {
        props: this.getProps()
      };
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
    getProps() {
      const storeState = store.getState();
      const data = typeof mapStateToProps === "function" && mapStateToProps(storeState, this.props);
      return data;
    }
    handleChange = () => {
      this.shouldHandleChange && this.setState({
        props: this.getProps()
      });
    }
    render() {
      return <WrappedComponent {...this.state.props} {...this.dispatchProps} />;
    }
  }
  return withStoreState;
};