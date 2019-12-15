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
import * as actions from "../../actions";

const ReferenceComponent = ({text, type, id, group, isExternal, onClick}) => {
  if (!type || !id) {
    return null;
  }

  const handleClick = () => {
    if (isExternal) {
      history.push(`/instances/${type}/${id}${group?("?group=" + group ):""}`);
    }
    typeof onClick === "function" && onClick(type, id, group);
  };

  return (
    <button onClick={handleClick} role="link">{text}</button>
  );
};

export const Reference = connect(
  (state, props) => {
    const [, type, id] = props.reference.match(/^(.+)\/(.+)$/);
    return {
      text: props.text?props.text:props.reference,
      type: type,
      id: id,
      group: (props.group !== state.groups.defaultGroup)?props.group:null,
      isExternal: state.router.location.pathname?state.router.location.pathname.startsWith("/instances/"):false
    };
  },
  dispatch => ({
    onClick: (type, id, group) => {
      dispatch(actions.setGroup(group));
      dispatch(actions.loadInstance(type, id));
    }
  })
)(ReferenceComponent);

export default Reference;