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
import { hash } from "./hash";

const StoreContext = React.createContext("myStore");

export const Provider = ({store, children}) => (
  <StoreContext.Provider value={store}>
    {children}
  </StoreContext.Provider>
);

export const connect = (mapStateToProps, mapDispatchToProps) => WrappedComponent => {
  class WithStore extends PureComponent {
    constructor(props) {
      super(props);
      this.shouldHandleChange = false;
      this.unsubscribe = null;
      const nextProps = this.getMapStateToProps(props);
      this.state = {
        props: nextProps
      };
      this.fingerprint = hash(nextProps);
      const { store } = props;
      this.dispatchProps = typeof mapDispatchToProps === "function" && mapDispatchToProps(store.dispatch);
    }
    componentDidMount() {
      const { store } = this.props;
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
      const { store } = this.props;
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

  const ConnectedComponent = props => (
    <StoreContext.Consumer>
      {store => (
        <WithStore store={store} {...props} />
      )}
    </StoreContext.Consumer>
  );

  return ConnectedComponent;
};