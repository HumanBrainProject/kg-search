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
import { store } from "../store";

export function withStoreStateSubscription(WrappedComponent, selectData) {
  class withStoreState extends PureComponent {
    constructor(props) {
      super(props);
      this.shouldHandleChange = false;
      this.handleChange = this.handleChange.bind(this);
      this.state = {
        data: this.getData()
      };
    }
    componentDidMount() {
      this.shouldHandleChange = true;
      document.addEventListener("state", this.handleChange.bind(this), false);
      this.handleChange();
    }
    componentWillUnmount() {
      this.shouldHandleChange = false;
      document.removeEventListener("state", this.handleChange);
    }
    getData() {
      const storeState = store.getState();
      const data = selectData(storeState, this.props);
      return data;
    }
    handleChange() {
      //setTimeout(() => {
      this.shouldHandleChange && this.setState({
        data: this.getData()
      });
      //});
    }
    render() {
      return <WrappedComponent {...this.state.data} {...this.props} />;
    }
  }
  return withStoreState;
}