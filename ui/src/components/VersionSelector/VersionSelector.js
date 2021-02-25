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

import { Select } from "../Select/Select";

import "./VersionSelector.css";

const getVersionValue = (versions, version) => {
  if (!Array.isArray(versions)) {
    return null;
  }
  const res = versions.find(v => v.label === version);
  if (res) {
    return res.value;
  }
  return null;
};

export const VersionSelector = ({ version, versions, onChange }) => {
  if (!version) {
    return null;
  }
  if (!Array.isArray(versions) || !versions.length) {
    return version;
  }
  const value = getVersionValue(versions, version);
  if (!value) {
    return null;
  }
  return (
    <div className="kgs-version_selector">
      <Select value={value} list={versions} onChange={onChange} />
    </div>
  );
};