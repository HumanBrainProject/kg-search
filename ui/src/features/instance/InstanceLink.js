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
import { useLocation, useNavigate } from "react-router-dom";

import { setInstanceId } from "./instanceSlice";

import Count from "../../components/Field/Count";

const InstanceLink = ({instanceId, text, count, context}) => {

  const navigate = useNavigate();
  const location = useLocation();

  const dispatch = useDispatch();

  const group = useSelector(state => state.groups.group);
  const defaultGroup = useSelector(state => state.groups.defaultGroup);
  const title = useSelector(state => state.instance.title);

  if (!instanceId) {
    return text;
  }

  let path = null;
  if (location.pathname.startsWith("/instances/")) {
    path = "/instances/";
  } else if (location.pathname.startsWith("/live/")) {
    path = "/live/";
  }

  const handleClick = () => {
    if (path) {
      const url = `${path}${instanceId}${group && group !== defaultGroup?("?group=" + group ):""}`;
      const options = {
        title: title, // TODO: check if not deprecated
        state: context
      };
      navigate(url, options);
    } else {
      dispatch(setInstanceId({
        instanceId: instanceId,
        context: context
      }));
    }
  };

  return (
    <span>
      <button onClick={handleClick} role="link">{text?text:instanceId}</button>
      <Count count={count} />
    </span>
  );
};

export default InstanceLink;