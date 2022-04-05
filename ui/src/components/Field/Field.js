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

import React, { useMemo } from "react";
import { FieldLabel } from "./FieldLabel";
import { Hint } from "../Hint/Hint";
import { ListField, PrintViewListField } from "./ListField";
import { ObjectField, PrintViewObjectField } from "./ObjectField";
import { ValueField, PrintViewValueField } from "./ValueField";
import TableField from "./TableField";
import HierarchicalFiles from "./Files/HierarchicalFiles";
import { AsyncHierarchicalFiles } from "../../containers/Files/AsyncHierarchicalFiles";
import FilePreview from "../FilePreview/FilePreview";
import MarkDownCitation from "./Citation/MarkDownCitation";
import DynamicCitation from "./Citation/DynamicCitation";

import "./Field.css";

const filesUrlRegex = /^(.+\/files)$/;
const liveFilesUrlRegex = /^(.+\/files)\/live$/;
const getFileUrlFrom = (url, type) => {
  if (!url || typeof url !== "string") {
    return null;
  }
  if (liveFilesUrlRegex.test(url)) {
    return url.replace(liveFilesUrlRegex, `$1/${type}/live`);
  }
  if (filesUrlRegex.test(url)) {
    return url.replace(filesUrlRegex, `$1/${type}`);
  }
  return null;
};

const FieldComponent = ({ name, layout, style, className, label, inlineLabel=true, labelCounter=null, hint, value, Component }) => {

  if (!Component) {
    return null;
  }

  const fieldClassName = name?`kgs-field__${name}`:"";
  const layoutClassName = layout?`kgs-field__layout-${layout}`:"";

  return (
    <span style={style} className={`kgs-field ${fieldClassName} ${layoutClassName} ${className}`}>
      <FieldLabel value={label} counter={labelCounter} inline={inlineLabel} />
      <Hint value={hint} />
      <Component {...value} />
    </span>
  );
};

const getFieldProps = (name, data, mapping, group, type, renderUserInteractions = true) => {

  if (Array.isArray(data) && data.length === 1) {
    data = data[0];
  }

  const ListFieldComponent = renderUserInteractions ? ListField : PrintViewListField;
  const ObjectFieldComponent = renderUserInteractions ? ObjectField : PrintViewObjectField;
  const ValueFieldComponent = renderUserInteractions ? ValueField : PrintViewValueField;

  let className = "";
  let labelCounter = null;
  let valueProps = null;
  let valueComponent = null;

  if (mapping.isGroupedLinks) { // Grouped Links

    const groupedLinksSize = Object.keys(data).length;
    const isSingleGroupedLinks = groupedLinksSize === 1;

    if (isSingleGroupedLinks) { // Single Grouped Links

      const singleGroupedLinksLabel = Object.keys(data)[0];
      const singleGroupedLinks = Object.values(data)[0];

      if (mapping.label) {
        mapping.label = `${mapping.label} in ${singleGroupedLinksLabel}`;
      } else {
        mapping.label = singleGroupedLinksLabel;
      }

      valueProps = {
        items: singleGroupedLinks,
        mapping: mapping,
        group: group,
        type: type
      };
      valueComponent =  ListFieldComponent;

    } else { // Multiple Grouped Links

      const multipleGroupedLinksMapping = {
        ...mapping,
        visible: true,
        enforceList: true,
        children: Object.keys(data).reduce((acc, service) => {
          acc[service] = {label: service, visible: true, enforceShowMore: true};
          return acc;
        }, {})
      };

      valueProps = {
        data: data,
        mapping: multipleGroupedLinksMapping,
        group: group,
        type: type,
      };
      valueComponent =  ObjectFieldComponent;

    }

  } else if (mapping.isFilePreview && data.url) { // File Preview

    valueProps = {
      url: data.url
    };
    valueComponent = FilePreview;

  } else if (mapping.isCitation) { // Citation

    if (name === "customCitation") { // MarkDown Citation

      valueProps = {
        text: data.value,
      };
      valueComponent = MarkDownCitation;

    } else { // Dynamic Citation

      valueProps = {
        doi: data.value
      };
      valueComponent = DynamicCitation;

    }

  } else if (mapping.isHierarchicalFiles) { // Hierarchical Files

    className = "kgs-field__hierarchical-files";

    const asyncFilesUrl = mapping.isAsync ? data : null;

    if (asyncFilesUrl) { // Async Hierarchical Files

      const asyncFileFormatsUrl = getFileUrlFrom(asyncFilesUrl, "formats");
      const asyncGroupingTypesUrl = getFileUrlFrom(asyncFilesUrl, "groupingTypes");

      valueProps = {
        mapping: mapping,
        group: group,
        type: type,
        nameFieldPath: "title.value",
        urlFieldPath: "iri.url",
        filesUrl: asyncFilesUrl,
        groupingTypesUrl: asyncGroupingTypesUrl,
        fileFormatsUrl: asyncFileFormatsUrl
      };
      valueComponent = AsyncHierarchicalFiles;

    } else { // Old Hierarchical Files

      valueProps = {
        data: data,
        mapping: mapping,
        group: group,
        type: type,
        nameFieldPath: "value",
        urlFieldPath: "url"
      };
      valueComponent = HierarchicalFiles;
    }
  } else if (mapping.isTable) { // Table

    className = "kgs-field__table";

    valueProps = {
      items: data,
      mapping: mapping,
      group: group,
      type: type
    };
    valueComponent = TableField;

  } else if (Array.isArray(data)) { // List

    if (mapping.layout === "group") {
      labelCounter = data.length;
    }

    valueProps = {
      items: data,
      mapping: mapping,
      group: group,
      type: type
    };
    valueComponent = ListFieldComponent;

  } else if (mapping.children) { // Object

    valueProps = {
      items: data,
      mapping: mapping,
      group: group,
      type: type
    };
    valueComponent = ObjectFieldComponent;

  } else { // Value

    valueProps = {
      data: data,
      mapping: mapping,
      group: group,
      type: type
    };
    valueComponent = ValueFieldComponent;

  }

  return {
    name: name,
    layout: ["header", "summary"].includes(mapping.layout)?mapping.layout:null,
    style: (mapping.order && !renderUserInteractions) ? { order: mapping.order } : null,
    className: className,
    label: (mapping.label && (!mapping.labelHidden || !renderUserInteractions))?mapping.label:null,
    inlineLabel: !mapping.tagIcon,
    labelCounter: labelCounter,
    hint: (renderUserInteractions && mapping.hint)?mapping.hint:null,
    value: valueProps,
    Component: valueComponent
  };
};

export const FieldBase = (renderUserInteractions = true) => {

  const Component = ({ name, data, mapping, group, type }) => {

    if (!mapping || !mapping.visible || !(data || mapping.showIfEmpty)) {
      return null;
    }

    const fieldProps = useMemo(() => getFieldProps(name, data, mapping, group, type, renderUserInteractions), [name, data, mapping, group, type, renderUserInteractions]);

    return (
      <FieldComponent {...fieldProps} />
    );
  };

  return Component;
};

export const Field = FieldBase(true);
Field.displayName = "Field";
export const PrintViewField = FieldBase(false);
PrintViewField.displayName = "PrintViewField";