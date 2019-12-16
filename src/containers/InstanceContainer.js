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
import { Link } from "react-router-dom";

import { ShareButtons } from "./ShareButtons";
import { Instance } from "../components/Instance";

import "./InstanceContainer.css";

const getTitle = (type, id, data) => {
  if (!type || !id) {
    return "Knowledge Graph Search";
  }
  if (data && data._type && data._id) {
    if (data._source && data._source.title && data._source.title.value) {
      return data._source.title.value;
    }
    if (data._type && data._id) {
      return `${data._type} ${data._id}`;
    }
  }
  return `${type} ${id}`;
};

export class InstanceContainer extends React.Component {
  componentDidMount() {
    const { setInitialGroup, location } = this.props;
    this.setTitle();
    const group = location.query.group;
    if (group) {
      setInitialGroup(group);
    }
    this.initialize();
  }

  componentDidUpdate(previousProps) {
    const { definitionIsReady, isGroupsReady, group, type, id } = this.props;
    this.setTitle();
    if (definitionIsReady !== previousProps.definitionIsReady || isGroupsReady !== previousProps.isGroupsReady || previousProps.group !== group || previousProps.type !== type || previousProps.id !== id) {
      this.initialize();
    }
  }

  setTitle() {
    const { type, id, data } = this.props;
    document.title = getTitle(type, id, data);
  }

  initialize() {
    const { definitionIsReady, definitionIsLoading, loadDefinition, isGroupsReady, isGroupLoading, shouldLoadGroups, loadGroups, instanceIsLoading, shouldLoadInstance, type, id, fetch } = this.props;
    if (!definitionIsReady) {
      if (!definitionIsLoading) {
        loadDefinition();
      }
    } else if (shouldLoadGroups && !isGroupsReady) {
      if (!isGroupLoading) {
        loadGroups();
      }
    } else {
      if (shouldLoadInstance && !instanceIsLoading) {
        fetch(type, id);
      }
    }
  }

  render() {
    const { show, group, defaultGroup } = this.props;
    const searchPath = `/${(group && group !== defaultGroup)?("?group=" + group):""}`;
    return (
      <div className="kgs-instance-container" >
        {show && (
          <React.Fragment>
            <div className="kgs-instance-container__header">
              <span className="kgs-instance-container__left"><Link  to={searchPath}><i className="fa fa-chevron-left"></i>&nbsp;Search</Link></span>
              <ShareButtons/>
            </div>
            <Instance {...this.props.instanceProps} />
          </React.Fragment>
        )}
      </div>
    );
  }

}
