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
import { store, dispatch } from "../../store";
import * as actions from "../../actions";
import "./styles.css";

export class SignInButton extends PureComponent {
  constructor(props) {
    super(props);
    this.state = this.getState();
    this.buttonRef = null;
    this.setButtonRef = element => {
      this.buttonRef = element;
    };
  }
  getState() {
    const globalState = store.getState();
    return {
      isAuthenticated: globalState.auth.isAuthenticated
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
    const login = () => {
      dispatch(actions.requestAuthentication());
    };
    const logout = () => {
      dispatch(actions.logout());
    };
    return (
      <div className="kgs-sign-in">
        {this.state.isAuthenticated?
          <button onClick={logout}>Log out</button>
          :
          <button onClick={login}>Log in</button>
        }
      </div>
    );
  }
}