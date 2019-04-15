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
import { ShareButtons as Component } from "../components/ShareButtons";

const getShareEmailToLink = (removeHash) => {
  const to= "";
  const subject= "Knowledge Graph Search Request";
  const body = "Please have a look to the following Knowledge Graph search request";
  const url = removeHash?window.location.href.replace(window.location.hash, ""):window.location.href;
  return `mailto:${to}?subject=${subject}&body=${body} ${escape(url)}.`;
};


const getClipboardContent = (state, location, isCurrentInstance) => {
  var href = location.href;
  if(isCurrentInstance){
    href =  `instances/${location.hash.substring(1)}`; 
  }
  return `${state.configuration.searchApiHost}/${href}`;
};

export const ShareButtons = connect(
  state => ({
    clipboardContent: getClipboardContent(state, window.location, state.instances.currentInstance),
    emailToLink: getShareEmailToLink(!state.instances.currentInstance)
  })
)(Component);