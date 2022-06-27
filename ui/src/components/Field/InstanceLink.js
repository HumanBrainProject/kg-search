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
import { useLocation, useNavigate } from "react-router-dom";

import * as actionsInstances from "../../actions/actions.instances";
import { getTitle } from "../../helpers/InstanceHelper";
import Count from "./Count";


const InstanceLinkComponent = ({text, id, group, defaultGroup, context, onClick, count}) => {
  const navigate = useNavigate();
  const location = useLocation();
  if (!id) {
    return text;
  }

  let path = null;
  if (location.pathname.startsWith("/instances/")) {
    path = "/instances/";
  } else if (location.pathname.startsWith("/live/")) {
    path = "/live/";
  }

  const handleClick = () => {
    if(path) {
      navigate(`${path}${id}${group && group !== defaultGroup?("?group=" + group ):""}`, context);
    } else {
      onClick(id, group?group:defaultGroup, navigate);
    }
  };

  return (
    <span>
      <button onClick={handleClick} role="link">{text}</button>
      <Count count={count} />
    </span>
  );
};

export const InstanceLink = connect(
  (state, props) => {
    return {
      text: props.text?props.text:props.id,
      count: props.count,
      id: props.id,
      group: props.group,
      defaultGroup: state.groups.defaultGroup,
      context: {
        title: getTitle(state.instances.currentInstance)
      }
    };
  },
  dispatch => ({
    onClick: (id, group, navigate) => {
      dispatch(actionsInstances.loadInstance(group, id, () => {
        navigate(`/${window.location.search}#${id}`);
      }));
    }
  })
)(InstanceLinkComponent);

export default InstanceLink;