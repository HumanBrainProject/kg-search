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
import React, { useState } from "react";
import { faCheck } from "@fortawesome/free-solid-svg-icons/faCheck";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useSelector } from "react-redux";
import { trackEvent } from "../../../app/services/api";
import Hint from "../../../components/Hint/Hint";
import "./Notification.css";

const Notification = () => {
  const isAuthenticated = useSelector(state => state.auth.isAuthenticated);
  const userId = useSelector(state => state.auth.userId);
  const [receiveNotifications, setReceiveNotifications] = useState(false);

  const handleClick = () => {
    trackEvent("Notifications", "Clicked", userId);
    setReceiveNotifications(true);
    setTimeout(() => {
      setReceiveNotifications(false);
    }, 2000);
  };

  return (
    <span className="kgs-search__notification">
      <Hint value="You need to login in order to be able to enable notifications" />
      &nbsp;
      {isAuthenticated ? (
        <>
          <a className="kgs-search__notification-link" onClick={handleClick}>
            Receive notifications
          </a> for new releases
        </>
      ) : (
        "Receive notifications for new releases"
      )}
      {receiveNotifications && (
        <>
          &nbsp;
          <FontAwesomeIcon
            className="kgs-search__notification-check"
            icon={faCheck}
          />
        </>
      )}
    </span>
  );
};

export default Notification;
