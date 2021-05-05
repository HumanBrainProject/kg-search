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

import { connect } from "react-redux";
import { ShareButtons as Component } from "../../components/ShareButtons/ShareButtons";

const getShareEmailToLink = url => {
  const to = "";
  const subject = "Knowledge Graph Search Request";
  const body = "Please have a look to the following Knowledge Graph search request";
  return `mailto:${to}?subject=${subject}&body=${body} ${escape(url)}.`;
};

const getClipboardContent = (location, currentInstance, group, defaultGroup) => {
  if (location.pathname === "/" && currentInstance) {
    const id = currentInstance._id;
    if (id) {
      const rootPath = window.location.pathname.substr(0, window.location.pathname.length - location.pathname.length);
      return `${window.location.protocol}//${window.location.host}${rootPath}/instances/${id}${group !== defaultGroup?("?group=" + group ):""}`;
    }
  }
  return window.location.href;
};

export const ShareButtons = connect(
  state => {
    const href = getClipboardContent(state.router.location, state.instances.currentInstance, state.groups.group, state.groups.defaultGroup);
    return {
      clipboardContent: href,
      emailToLink: getShareEmailToLink(href)
    };
  }
)(Component);