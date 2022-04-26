/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

import React from "react";
import { connect } from "react-redux";
import * as actions from "../../actions/actions";
import * as actionsGroups from "../../actions/actions.groups";
import * as actionsSearch from "../../actions/actions.search";
import * as actionsInstances from "../../actions/actions.instances";
import "./SignIn.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {faUser} from "@fortawesome/free-solid-svg-icons/faUser";
import {faCheck} from "@fortawesome/free-solid-svg-icons/faCheck";
import {faSignOutAlt} from "@fortawesome/free-solid-svg-icons/faSignOutAlt";

const SignInComponent = ({ className, Tag, isAuthenticated, group, groups, login, logout, onGroupChange }) => {
  if(isAuthenticated) {
    return (
      <Tag className={`${className} dropdown`}>
        <a className="dropdown-toggle kgs-sign__in kgs_signed__in" id="navbarDropdown" role="button" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
          <FontAwesomeIcon icon={faUser} size="2x" />
        </a>
        <div className="dropdown-menu" aria-labelledby="navbarDropdown">
          {groups.map(g => (
            <div className="dropdown-item" key={g.value}>
              <button  onClick={() => onGroupChange(g.value)}>
                {group === g.value?
                  <FontAwesomeIcon icon={faCheck} style={{marginRight: "4px"}} />
                  :
                  <span className="kgs-sign__in__space"/>
                }
                {g.label}
              </button>
            </div>
          ))}
          <div className="dropdown-divider"></div>
          <div className="dropdown-item">
            <button onClick={logout}><FontAwesomeIcon icon={faSignOutAlt} style={{marginRight: "4px"}} />Logout</button>
          </div>
        </div>
      </Tag>
    );
  }
  return (
    <Tag className={className}>
      <button className="mobile-link kgs-login" onClick={login}>Login</button>
    </Tag>
  );
};

export const SignIn = connect(
  (state, props) => ({
    isAuthenticated: state.auth.isAuthenticated,
    group: state.groups.group,
    groups: state.groups.groups?state.groups.groups:[],
    className: props.className,
    Tag: props.Tag
  }),
  dispatch => ({
    logout: () => {
      dispatch(actions.logout());
    },
    login: () => {
      dispatch(actions.authenticate());
    },
    onGroupChange: value => {
      dispatch(actionsGroups.setGroup(value));
      dispatch(actionsInstances.clearAllInstances());
      dispatch(actionsGroups.resetTypeForGroup(value));
      dispatch(actionsSearch.setPage(1));
    }
  })
)(SignInComponent);


