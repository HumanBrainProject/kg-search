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
import ReactPiwik from "react-piwik";

import { Field } from "./Field";
import { FieldsPanel } from "./FieldsPanel";
import { FieldsTabs } from "./FieldsTabs";
import "./Instance.css";


export const Instance = ({ type, hasNoData, hasUnknownData, header, previews, main, summary, groups, ImagePreviewsComponent, ImagePopupComponent, TermsShortNoticeComponent}) => {
  if (hasNoData) {
    return (
      <div className="kgs-instance" data-type={type}>
        <div className="kgs-instance__no-data">This data is currently not available.</div>
      </div>
    );
  }
  if (hasUnknownData) {
    return (
      <div className="kgs-instance" data-type={type}>
        <div className="kgs-instance__no-data">This type of data is currently not supported.</div>
      </div>
    );
  }
  ReactPiwik.push(["trackEvent", "Card", "Opened", `${window.location.search}${window.location.hash}`]);

  return (
    <div className="kgs-instance" data-type={type}>
      <div className="kgs-instance-scroll">
        <TermsShortNoticeComponent />
        <div className={`kgs-instance-content kgs-instance__grid ${(previews && previews.length) ? "kgs-instance__with-previews" : ""}`}>
          <div className="kgs-instance__header">
            <h3 className={`kgs-instance__group ${header.group? "show" : ""}`}>Group: <strong>{header.group}</strong></h3>
            <div>
              <Field {...header.icon} />
              <Field {...header.type} />
            </div>
            <div>
              <Field {...header.title} />
            </div>
          </div>
          <ImagePreviewsComponent className={`kgs-instance__previews ${(previews && previews.length > 1) ? "has-many" : ""}`} width="300px" images={previews} />
          <FieldsPanel className="kgs-instance__main" fields={main} fieldComponent={Field} />
          <FieldsPanel className="kgs-instance__summary" fields={summary} fieldComponent={Field} />
          <FieldsTabs className="kgs-instance__groups" fields={groups} />
        </div>
      </div>
      <ImagePopupComponent className="kgs-instance__image_popup" />
    </div>
  );
};