/*
*   Copyright (c) 2021, EPFL/Human Brain Project PCO
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

import React from "react";
import { connect } from "react-redux";
import * as actions from "../../actions/actions";
import * as actionsGroups from "../../actions/actions.groups";
import * as actionsSearch from "../../actions/actions.search";
import "./SignIn.css";

const SignInComponent = ({ isAuthenticated, group, groups, login, logout, onGroupChange }) => {
  if(isAuthenticated) {
    return (
      <div className="dropdown">
        <a className="btn btn-default dropdown-toggle kgs-sign__in" type="button" id="dropdownMenu1" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
          <i className="fa fa-2x fa-user" aria-hidden="true"></i>
        </a>
        <ul className="dropdown-menu" aria-labelledby="dropdownMenu1">
          {groups.map(g => (
            <li key={g.value}>
              <a onClick={() => onGroupChange(g.value)}>
                {group === g.value?
                  <i className="fa fa-check" aria-hidden="true" style={{marginRight: "4px"}}></i>
                  :
                  <span className="kgs-sign__in__space"/>
                }
                {g.label}
              </a>
            </li>
          ))}
          <li role="separator" className="divider"></li>
          <li><a className="mobile-link" onClick={logout}>Logout</a></li>
        </ul>
      </div>
    );
  }
  return (
    <a className="mobile-link kgs-login" onClick={login}>Login</a>
  );
};

export const SignIn = connect(
  state => ({
    isAuthenticated: state.auth.isAuthenticated,
    group: state.groups.group,
    groups: state.groups.groups?state.groups.groups:[]
  }),
  dispatch => ({
    logout: () => dispatch(actions.logout()),
    login: () => dispatch(actions.authenticate()),
    onGroupChange: value => {
      dispatch(actionsGroups.setGroup(value));
      dispatch(actionsGroups.resetTypeForGroup(value));
      dispatch(actionsSearch.setPage(1));
    }
  })
)(SignInComponent);


