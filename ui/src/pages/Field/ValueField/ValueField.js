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
import { termsOfUse } from "../../../data/termsOfUse.js";
import { Details } from "../../../components/Details/Details";
import { Text } from "../../../components/Text/Text";
import { Link } from "../../../components/Link/Link";
import { Tag } from "../../../components/Tag/Tag";
import Thumbnail from "../../../features/image/Thumbnail";
import InstanceLink from "../../../features/instance/InstanceLink";
import "./ValueField.css";

const getUrlLocation = url => {
  let path = url.split("/");
  let protocol = path[0];
  let host = path[2];
  return `${protocol}//${host}`;
};

const ValueFieldBase = (renderUserInteractions = true) => {

  const ValueField = ({ data, mapping }) => {
    if (!data || !mapping) {
      return null;
    }
    const instanceIdLink = (!!renderUserInteractions && !!data.reference)?data.reference:null;
    const hasInstanceLink = !!instanceIdLink;
    const hasLink = !!renderUserInteractions && !!data.url;
    const hasMailToLink = !!renderUserInteractions && typeof data.url === "string" && data.url.substring(0, 7).toLowerCase() === "mailto:";
    const isAFileLink = typeof data.url === "string" && data.url.startsWith("https://object.cscs.ch");
    const hasExternalLink = data.url && !isAFileLink && getUrlLocation(data.url) !== window.location.origin;
    const hasAnyLink = hasInstanceLink || hasMailToLink || hasLink;
    const isLinkWithIcon = mapping.linkIcon && data.url ? true : false;
    const isTag = !hasAnyLink && !!mapping.tagIcon;
    const isMarkdown = !!renderUserInteractions && !hasAnyLink && !isTag && !!mapping.isMarkdown;
    const showPreview = !!renderUserInteractions && data.previewUrl && (typeof data.previewUrl === "string" || typeof data.previewUrl.url === "string");
    const count = data.count;

    let value = data.value;

    if (data.value && mapping.type === "date") {
      const timestamp = Date.parse(data.value);
      if (timestamp && !isNaN(timestamp)) {
        value = new Date(timestamp).toLocaleDateString();
      }
    }

    let ValueComponent = null;
    let valueProps = null;
    if (hasInstanceLink) {
      ValueComponent = InstanceLink;
      valueProps = {
        instanceId: instanceIdLink,
        text: value ? value : instanceIdLink,
        count: count,
        context: data.context
      };
    } else if (hasLink || isLinkWithIcon) {
      ValueComponent = Link;
      valueProps = {
        url: data.url,
        label: value,
        isAFileLink: isAFileLink,
        isExternalLink: hasMailToLink || hasExternalLink,
        icon: mapping.linkIcon,
        count: count
      };
    } else if (isTag) {
      ValueComponent = Tag;
      valueProps = {
        icon: mapping.tagIcon,
        value: value
      };
    } else {
      ValueComponent = Text;
      valueProps = {
        content: value,
        isMarkdown: isMarkdown
      };
    }

    return (
      <div className="field-value">
        {isAFileLink && (
          <Thumbnail showPreview={showPreview}
            thumbnailUrl={data.thumbnailUrl && (typeof data.thumbnailUrl === "string" ? data.thumbnailUrl : data.thumbnailUrl.url)}
            previewUrl={data.previewUrl && (typeof data.previewUrl === "string" ? data.previewUrl : data.previewUrl.url)}
            isAnimated={data.previewUrl && data.previewUrl.isAnimated}
            alt={typeof data.value === "string" ? data.value : ""} />
        )}
        <ValueComponent {...valueProps} />
        {isAFileLink && data.fileSize ?
          <span className="field-filesize">({data.fileSize})</span>
          : null}
        {!!mapping.showTermsOfUse && (
          <Details toggleLabel="Terms of use" content={termsOfUse} />
        )}
      </div>
    );
  };

  return ValueField;
};

export const ValueField = ValueFieldBase(true);
ValueField.displayName = "ValueField";
export const PrintViewValueField = ValueFieldBase(false);
PrintViewValueField.displayName = "PrintViewValueField";