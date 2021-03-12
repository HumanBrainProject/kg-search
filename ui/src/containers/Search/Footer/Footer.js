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
import { ShareButtons } from "../../Share/ShareButtons";
import { Pagination } from "../Pagination/Pagination";
import { TermsShortNotice } from "../../Notice/TermsShortNotice";
import "./Footer.css";

export const Footer = () => {
  return (
    <div className="kgs-footer">
      <TermsShortNotice className="kgs-footer__terms-short-notice" />
      <div className="kgs-footer-nav">
        <Pagination />
        <ShareButtons/>
        <div className="kgs-space" />
        <div className="kgs-space2" />
      </div>
    </div>
  );
};