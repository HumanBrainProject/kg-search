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
import { FieldIcon } from "../../../../../Field/components/FieldIcon";
import { Field } from "../../../../../Field";
import { FieldsPanel } from "../FieldsPanel";
import { FieldsTabs } from "../FieldsTabs";
import "./styles.css";

export function InstancePanel({type, hasNoData, hasUnknownData, header, main, summary, groups}) {
  return (
    <div className="kgs-instance" data-type={type}>
      <div className="kgs-instance__content">
        <div className="kgs-instance__header">
          <div>
            <FieldIcon {...header.icon} />
            <Field {...header.type} />
          </div>
          <div>
            <Field {...header.title} />
          </div>
        </div>
        <div className="kgs-instance__body">
          <FieldsPanel className="kgs-instance__main" fields={main} />
          <FieldsPanel className="kgs-instance__summary" fields={summary} />
        </div>
        <FieldsTabs className="kgs-instance__groups" fields={groups} />
      </div>
      {hasNoData && (
        <div className="kgs-instance__no-data">This data is currently not available.</div>
      )}
      {hasUnknownData && (
        <div className="kgs-instance__no-data">This type of data is currently not supported.</div>
      )}
    </div>
  );
}
