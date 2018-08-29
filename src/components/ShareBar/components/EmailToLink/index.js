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
import { withStoreStateSubscription} from "../../../withStoreStateSubscription";
import "./styles.css";

const EmailToLinkComponent = ({title = "Send search link by email", text, icon = "fa fa-envelope-o", link}) => {
  return (
    <a className="kgs-email-link" href={link} title={title}><i className={icon}></i><span>{text}</span></a>
  );
};

export const EmailToLink = withStoreStateSubscription(
  EmailToLinkComponent,
  data => ({
    link: data.application.shareEmailToLink
  })
);
