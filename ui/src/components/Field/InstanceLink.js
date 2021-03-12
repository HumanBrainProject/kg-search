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

import React from "react";
import { connect } from "react-redux";
import { history } from "../../store";
import * as actionsGroups from "../../actions/actions.groups";
import * as actionsInstances from "../../actions/actions.instances";
import { getTitle } from "../../helpers/InstanceHelper";

const InstanceLinkComponent = ({text, id, group, path, context, onClick}) => {
  if (!id) {
    return text;
  }

  const handleClick = () => {
    typeof onClick === "function" && onClick(id, group, path, context);
  };

  return (
    <button onClick={handleClick} role="link">{text}</button>
  );
};

export const InstanceLink = connect(
  (state, props) => {
    let path = null;
    if (state.router.location.pathname.startsWith("/instances/")) {
      path = "/instances/";
    } else if (state.router.location.pathname.startsWith("/live/")) {
      path = "/live/";
    }
    return {
      text: props.text?props.text:props.id,
      id: props.id,
      group: (props.group && props.group !== props.defaultGroup)?props.group:null,
      path: path,
      context: {
        title: getTitle(state.instances.currentInstance)
      }
    };
  },
  dispatch => ({
    onClick: (id, group, path, context) => {
      if (path) {
        history.push(`${path}${id}${group && group !== "public"?("?group=" + group ):""}`, context);
      } else {
        dispatch(actionsGroups.setGroup(group));
        dispatch(actionsInstances.loadInstance(group, id, true));
      }
    }
  })
)(InstanceLinkComponent);

export default InstanceLink;