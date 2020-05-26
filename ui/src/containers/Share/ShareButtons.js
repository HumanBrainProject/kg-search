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

import { connect } from "react-redux";
import { ShareButtons as Component } from "../../components/ShareButtons/ShareButtons";

const getShareEmailToLink = url => {
  const to = "";
  const subject = "Knowledge Graph Search Request";
  const body = "Please have a look to the following Knowledge Graph search request";
  return `mailto:${to}?subject=${subject}&body=${body} ${escape(url)}.`;
};

const getClipboardContent = (state, location, currentInstance, currentGroup, defaultGroup) => {
  if (location.pathname === "/" && currentInstance) {
    const indexReg = /^kg_(.*)$/;
    const group = indexReg.test(currentInstance._index) ? currentInstance._index.match(indexReg)[1] : (currentGroup ? currentGroup : null);
    const type = currentInstance._type;
    const id = currentInstance._id;
    if (type && id) {
      const rootPath = window.location.pathname.substr(0, window.location.pathname.length - location.pathname.length);
      return `${window.location.protocol}//${window.location.host}${rootPath}/instances/${type}/${id}${group !== defaultGroup?("?group=" + group ):""}`;
    }
  }
  return window.location.href;
};

export const ShareButtons = connect(
  state => {
    const href = getClipboardContent(state, state.router.location, state.instances.currentInstance, state.groups.group, state.groups.defaultGroup);
    return {
      clipboardContent: href,
      emailToLink: getShareEmailToLink(href)
    };
  }
)(Component);