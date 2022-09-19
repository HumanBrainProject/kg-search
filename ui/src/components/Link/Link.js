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

import { trackLink } from "../../app/services/api";
import Count from "../Field/Count";

import "./Link.css";

export const Link = ({ url, label, isAFileLink, isExternalLink, icon, count }) => {
  if (!url) {
    return null;
  }

  const text = label ? label : url;

  const props = isExternalLink ? {
    rel: "noopener noreferrer",
    target: "_blank"
  } : null;

  const handleClick = () => {
    if (isAFileLink) {
      trackLink(url, "download");
    } else if (isExternalLink) {
      trackLink(url, "link");
    }
  };

  return (
    <span>
      <a href={url} {...props} onClick={handleClick}>
        {icon ? <span className="field-value__link_icon" dangerouslySetInnerHTML={{ __html: icon }} /> : null}
        {text}
      </a>
      <Count count={count} />
    </span>
  );
};

export default Link;