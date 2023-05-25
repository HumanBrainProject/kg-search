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
import { useSelector, useDispatch } from "react-redux";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {faUser} from "@fortawesome/free-solid-svg-icons/faUser";
import {faCheck} from "@fortawesome/free-solid-svg-icons/faCheck";
import {faSignOutAlt} from "@fortawesome/free-solid-svg-icons/faSignOutAlt";

import useAuth from "../../hooks/useAuth";
import Matomo from "../../services/Matomo";
import { setGroup } from "../groups/groupsSlice";
import { setPage } from "../search/searchSlice";
import { reset } from "../instance/instanceSlice";

import "./SignIn.css";

const Group = ({ group }) => {

  const dispatch = useDispatch();

  const current = useSelector(state => state.groups.group);

  const handleGroupClick = () => {
    Matomo.trackEvent("Group", "Select", group.value);
    dispatch(setGroup(group.value));
    dispatch(reset());
    dispatch(setPage(1));
  };

  return (
    <div className="dropdown-item">
      <button  onClick={handleGroupClick}>
        {group.value === current?
          <FontAwesomeIcon icon={faCheck} style={{marginRight: "4px"}} />
          :
          <span className="kgs-sign__in__space"/>
        }
        {group.label}
      </button>
    </div>
  );
};

const SignIn = ({ className, Tag }) => {

  const { isUninitialized, isAuthenticating, isAuthenticated, isLogingOut, login, logout } = useAuth();

  const groups = useSelector(state => state.groups.groups);

  const handleLogoutClick = () => {
    Matomo.trackEvent("User", "Logout");
    logout();
  };

  const handleLoginClick = () => {
    Matomo.trackEvent("User", "Login");
    login();
  };

  if (isUninitialized) {
    return null;
  }
  if(isAuthenticated) {
    return (
      <Tag className={`${className} dropdown`}>
        <a className="dropdown-toggle kgs-sign__in kgs_signed__in" id="navbarDropdown" role="button" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
          <FontAwesomeIcon icon={faUser} size="2x" />
        </a>
        <div className="dropdown-menu" aria-labelledby="navbarDropdown">
          {groups.map(g => (
            <Group key={g.value} group={g} />
          ))}
          {!isLogingOut && (
            <>
              {!!groups.length && (
                <div className="dropdown-divider"></div>
              )}
              <div className="dropdown-item">
                <button onClick={handleLogoutClick}><FontAwesomeIcon icon={faSignOutAlt} style={{marginRight: "4px"}} />Logout</button>
              </div>
            </>
          )}
        </div>
      </Tag>
    );
  }

  if (!isAuthenticating) {
    return (
      <Tag className={className}>
        <button className="mobile-link kgs-login" onClick={handleLoginClick}>Login</button>
      </Tag>
    );
  }

  return null;
};

export default SignIn;