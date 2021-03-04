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

import { getTags } from "../../helpers/InstanceHelper";
import { Tags } from "../Tags/Tags";
import { Field } from "../Field/Field";
import { FieldsPanel } from "../Field/FieldsPanel";
import { FieldsTabs } from "../Field/FieldsTabs";
import { FieldsButtons } from "../Field/FieldsButtons";
import { VersionSelector } from "../VersionSelector/VersionSelector";
import { history } from "../../store";
import { BgError } from "../BgError/BgError";

import "./Instance.css";

export class Instance extends React.PureComponent {

  componentDidMount() {
    this.trackEvent();
  }

  componentDidUpdate(previousProps) {
    const { id, type, group } = this.props;
    if (id !== previousProps.id || type !== previousProps.type || group !== previousProps.group) {
      this.trackEvent();
    }
  }

  trackEvent = () => {
    const { id, type, group, path, defaultGroup } = this.props;
    const relativeUrl = `${path}${type}/${id}${(group && group !== defaultGroup)?("?group=" + group):""}`;
    ReactPiwik.push(["trackEvent", "Card", "Opened", relativeUrl]);
  }

  onVersionChange = version => {
    const { searchPage, group, fetch, path } = this.props;
    if(searchPage) {
      fetch(group, version, true);
    } else {
      history.push(`${path}${version}${group && group !== "public"?("?group=" + group ):""}`);
    }
  }

  render() {
    const { id, type, hasNoData, hasUnknownData, header, previews, buttons, main, summary, groups, NavigationComponent, ImagePreviewsComponent, ImagePopupComponent, TermsShortNoticeComponent } = this.props;

    if (hasNoData) {
      return(
        <BgError show={true} message="This data is currently not available." />
      );
    }

    if (hasUnknownData) {
      return(
        <BgError show={true} message="This type of data is currently not supported." />
      );
    }

    const tags = getTags(header);

    return (
      <div className="kgs-instance" data-type={type}>
        <div className="kgs-instance__header">
          <NavigationComponent />
          <div className="kgs-instance__header_fields">
            <Tags tags={tags} />
            <div className="kgs-instance__header_title">
              <Field {...header.title} />
              <VersionSelector version={header.version} versions={header.versions} onChange={this.onVersionChange} />
            </div>
            <FieldsPanel fields={header.fields} fieldComponent={Field} />
          </div>
        </div>
        <div className="kgs-instance-scroll">
          <div className="kgs-instance-scoll-content">
            <div className={`kgs-instance-content kgs-instance__grid ${(buttons && buttons.length) ? "kgs-instance__with-buttons" : ""} ${(previews && previews.length) ? "kgs-instance__with-previews" : ""}`}>
              <FieldsButtons className="kgs-instance__buttons" fields={buttons} />
              <div className="kgs-instance__highlights">
                <ImagePreviewsComponent className={`kgs-instance__previews ${(previews && previews.length > 1) ? "has-many" : ""}`} width="300px" images={previews} />
                <FieldsPanel className="kgs-instance__summary" fields={summary} fieldComponent={Field} />
              </div>
              <FieldsPanel className="kgs-instance__main" fields={main} fieldComponent={Field} />
              <FieldsTabs className="kgs-instance__groups" id={id} fields={groups} />
            </div>
            <strong className="kgs-instance-content-disclaimer">Disclaimer:
Please alert us at <a href="mailto:curation-support@ebrains.eu">curation-support@ebrains.eu</a> for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible.</strong>
            <TermsShortNoticeComponent />
          </div>
        </div>
        <ImagePopupComponent className="kgs-instance__image_popup" />
      </div>
    );
  }
}