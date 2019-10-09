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
import { withFloatingScrollEventsSubscription} from "../../helpers/withFloatingScrollEventsSubscription";
import { ShareButtons } from "../ShareButtons";
import { Pagination } from "./Pagination";
import { GroupSelection } from "../GroupSelection";
import { SignInButton } from "../SignInButton";
import { TermsShortNotice } from "../TermsShortNotice";
import "./Footer.css";

const FooterBase = ({isFloating}) => {
  return (
    <div className={`kgs-footer${isFloating?" is-fixed-position":""}`}>
      <TermsShortNotice className="kgs-footer__terms-short-notice" />
      <div className="kgs-footer-nav">
        <SignInButton className="kgs-sign-in" signInLabel="Log in" signOffLabel="Log out"/>
        <GroupSelection className="kgs-group-selection"/>
        <Pagination className="kgs-footer-pagination" />
        <ShareButtons/>
        <div className="kgs-space" />
        <div className="kgs-space2" />
      </div>
    </div>
  );
};

export const Footer = withFloatingScrollEventsSubscription(
  "bottom",
  [
    {querySelector: "main"},
    {querySelector: "footer.site-footer"}
  ]
)(FooterBase);
