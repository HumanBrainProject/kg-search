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
import { useSelector, useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";

import { requestInstance, setTab, selectTypeMapping } from "../../features/instance/instanceSlice";
import { selectIsDefaultGroup, selectGroupLabel } from "../../features/groups/groupsSlice";
import Matomo from "../../services/Matomo";

import Header from "./Header/Header";
import ImagePopup from "../../features/image/ImagePopup";
import TermsShortNotice from "../../features/TermsShortNotice";
import Disclaimer from "../../components/Disclaimer/Disclaimer";
import BgError from "../../components/BgError/BgError";
import OutdatedVersionDisclaimer from "../../components/OutdatedVersionDisclaimer";
import Tabs from "./Tabs/Tabs";

import "./InstanceView.css";
import "./Fields.css";

const getField = (type, name, data, mapping) => {
  if (name === "type") {
    return {
      name: "type",
      data: { value: type },
      mapping: { },
      type: type
    };
  }
  return {
    name: name,
    data: data,
    mapping: mapping,
    type: type
  };
};

const getHeaderFields = (type, data, mapping) => {
  if (!data || !mapping) {
    return [];
  }

  return Object.entries(mapping.fields || {})
    .filter(
      ([name, fieldsMapping]) =>
        fieldsMapping &&
        data?.[name] &&
        fieldsMapping.layout === "header" &&
        name !== "title"
    )
    .map(([name, fieldsMapping]) =>
      getField(type, name, data[name], fieldsMapping)
    );
};

const getFieldsByTabs = (type, data, typeMapping, previews) => {
  if (!data || !typeMapping) {
    return [];
  }

  const overviewFields = [];
  const tabs = Object.entries(typeMapping.fields || {})
    .filter(
      ([name, mapping]) =>
        mapping &&
        data?.[name] &&
        mapping.layout !== "header" &&
        name !== "title" // title is displayed in the header
    )
    .reduce((acc, [name, mapping]) => {
      const groupName =
        !mapping.layout || mapping.layout === "summary" ? null : mapping.layout;
      const field = getField(type, name, data[name], mapping);
      if (!groupName) {
        overviewFields.push(field);
      } else {
        if (!acc[groupName]) {
          acc[groupName] = {
            name: groupName,
            fields: []
          };
        }
        acc[groupName].fields.push(field);
      }
      return acc;
    }, {});

  if (overviewFields.length) {
    return [
      {
        name: "Overview",
        fields: overviewFields,
        previews: previews
      },
      ...Object.values(tabs)
    ];
  }
  return Object.values(tabs);
};

const getVersions = versions => (Array.isArray(versions)?versions:[])
  .map(v => ({
    label: v.value ? v.value : "Current",
    value: v.reference
  }));

const getTags = (groupLabel, isDefaultGroup, category) => {
  const tags = [];
  if (!isDefaultGroup && groupLabel) {
    tags.push(groupLabel);
  }
  if (category) {
    tags.push(category);
  }
  return tags;
};

const InstanceView = ({ data, path, isSearch, customNavigationComponent }) => {

  const navigate = useNavigate();

  const dispatch = useDispatch();

  const type = data?.type;
  const fields = data?.fields;
  const mapping =  useSelector(state => selectTypeMapping(state, type));

  const hasNoData = !fields;
  const hasUnknownData = !mapping;

  const group = useSelector(state => state.groups.group);
  const defaultGroup = useSelector(state => state.groups.defaultGroup);
  const isDefaultGroup = useSelector(state => selectIsDefaultGroup(state));
  const groupLabel = useSelector(state => selectGroupLabel(state, group));

  const headerFields = getHeaderFields(type, fields, mapping);

  const selectedTab = useSelector(state => state.instance.tab);
  const tabs = getFieldsByTabs(type, data?.fields, mapping, data?.previews);

  const version = (data?.version)?data.version:"Current";
  const versions = getVersions(data?.versions);

  const tags = getTags(groupLabel, isDefaultGroup, data?.category);

  const onVersionChange = version => {
    const context = {
      tab: selectedTab
    };
    if(isSearch) {
      dispatch(requestInstance({
        instanceId: version,
        context: context
      }));
    } else {
      navigate(`${path}${version}${(group && group !== defaultGroup)?("?group=" + group ):""}`, { state: context});
    }
  };

  const handleTabClick = tab => {
    if(tab !== selectedTab) {
      Matomo.trackEvent("Tab", "Clicked", tab);
      dispatch(setTab(tab));
    }
  };

  if (hasNoData) {
    return(
      <BgError message="This data is currently not available." />
    );
  }

  if (hasUnknownData) {
    return(
      <BgError message="This type of data is currently not supported." />
    );
  }

  return (
    <div className="kgs-instance" data-type={type}>
      <Header title={data?.title} version={version} tags={tags} fields={headerFields} versions={versions} customNavigationComponent={customNavigationComponent} onVersionChange={onVersionChange} />
      <OutdatedVersionDisclaimer type={type} version={version} versions={versions} overviewVersion={data?.allVersionRef} onVersionChange={onVersionChange} />
      <Tabs tabs={tabs} selectedTab={selectedTab} onTabClick={handleTabClick} />
      <Disclaimer content={data?.disclaimer} />
      <TermsShortNotice />
      <ImagePopup className="kgs-instance__image_popup" />
    </div>
  );
};

export default InstanceView;