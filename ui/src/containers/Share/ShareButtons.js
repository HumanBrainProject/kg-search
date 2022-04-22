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
import { useLocation } from "react-router-dom";

import { ShareButtons as ShareButtonsComponent } from "../../components/ShareButtons/ShareButtons";

const getUrlToShare = (location, currentInstance, group, defaultGroup) => {
  if (location.pathname === "/" && currentInstance) {
    const id = currentInstance._id;
    if (id) {
      const rootPath = window.location.pathname.substr(0, window.location.pathname.length - location.pathname.length);
      return `${window.location.protocol}//${window.location.host}${rootPath}/instances/${id}${group !== defaultGroup ? ("?group=" + group) : ""}`;
    }
  }
  return window.location.href;
};

const ShareButtonsContainer = ({ currentInstance, group, defaultGroup}) => {
  const location = useLocation();

  const url = getUrlToShare(location, currentInstance, group, defaultGroup);

  return (
    <ShareButtonsComponent url={url} />
  );
};

export const ShareButtons = connect(
  state => {
    return {
      currentInstance: state.instances.currentInstance,
      group: state.groups.group,
      defaultGroup: state.groups.defaultGroup
    };
  }
)(ShareButtonsContainer);