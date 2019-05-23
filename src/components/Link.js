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
import "./Link.css";

export const Link = ({url, label, isMailToLink, icon}) => {
  if (!url) {
    return null;
  }

  const text = label?label:url;

  const props = !isMailToLink?null:{
    rel: "noopener noreferrer",
    target: "_blank"
  };

  return (
    <a href={url} {...props}>
      {icon?<span className="field-value__link_icon"  dangerouslySetInnerHTML={{__html:icon}} />:null}
      {text}
    </a>
  );
};

export default Link;