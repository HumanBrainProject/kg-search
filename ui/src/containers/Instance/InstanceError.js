
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
import { useNavigate } from "react-router-dom";

import { BgError } from "../../components/BgError/BgError";
import { clearInstanceError, clearAllInstances } from "../../actions/actions.instances";
import { clearGroupError } from "../../actions/actions.groups";
import { logout } from "../../actions/actions.auth";

const BaseInstanceError  = ({ error, onRetry, onCancel, group, defaultGroup }) => {

  const navigate = useNavigate();

  const handleOnCancelClick = () => {
    onCancel(group);
    navigate(`/${(group && group !== defaultGroup)?("?group=" + group):""}`, {replace:true});
  };

  if (!error) {
    return null;
  }

  return (
    <BgError message={error} cancelLabel="Back to search" onCancelClick={handleOnCancelClick} onRetryClick={onRetry} retryVariant="primary" />
  );
};

const InstanceError = connect(
  state => ({
    group: state.groups.group,
    defaultGroup: state.groups.defaultGroup,
    error: state.instances.error
  }),
  dispatch => ({
    onCancel: (group) => {
      if (!group) {
        dispatch(clearGroupError());
        dispatch(logout());
      }
      dispatch(clearInstanceError());
      dispatch(clearAllInstances());
    },
    onRetry: () => {
      dispatch(clearInstanceError());
    }
  })
)(BaseInstanceError);

export default InstanceError;