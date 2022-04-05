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
import { termsOfUse } from "../../data/termsOfUse.js";
import { Details } from "../Details/Details";
import { Text } from "../Text/Text";
import { CollapsibleText } from "../CollapsibleText/CollapsibleText";
import { Link } from "../Link/Link";
import { Tag } from "../Tag/Tag";
import { Thumbnail } from "../../containers/Thumbnail/Thumbnail";
import { InstanceLink } from "./InstanceLink";
import "./ValueField.css";

const getUrlLocation = url => {
  let path = url.split("/");
  let protocol = path[0];
  let host = path[2];
  return `${protocol}//${host}`;
};

const ValueFieldBase = (renderUserInteractions = true) => {

  const ValueField = ({ data, mapping, group }) => {
    if (!data || !mapping || !mapping.visible) {
      return null;
    }

    /*
    if (!data.previewUrl && Math.round(Math.random() * 10) % 2 === 0) {
      if (Math.round(Math.random() * 10) % 2 === 0) {
        data.previewUrl = {
          url: "https://cdn2.thecatapi.com/images/18f.gif",
          isAnimated: true
        };
      } else {
        data.previewUrl = {
          url: "http://lorempixel.com/output/cats-q-c-640-480-3.jpg",
          isAnimated: false
        };
      }
    }
    if (!data.thumbnailUrl && Math.round(Math.random() * 10) % 2 === 0) {
      data.thumbnailUrl = {
        url: "https://dogtowndogtraining.com/wp-content/uploads/2012/06/300x300-clicker.jpg",
        isAnimated: false
      };
    }
    */
    const regInstanceLink = /^(.+)$/;
    const [, instanceIdLink] = (!!renderUserInteractions && !!data.reference && regInstanceLink.test(data.reference))?data.reference.match(regInstanceLink):[null, null];
    const hasInstanceLink = !!instanceIdLink;
    const hasLink = !!renderUserInteractions && !!data.url;
    const hasMailToLink = !!renderUserInteractions && typeof data.url === "string" && data.url.substr(0, 7).toLowerCase() === "mailto:";
    const isAFileLink = typeof data.url === "string" && data.url.startsWith("https://object.cscs.ch");
    const hasExternalLink = data.url && !isAFileLink && getUrlLocation(data.url) !== window.location.origin;
    const hasAnyLink = hasInstanceLink || hasMailToLink || hasLink;
    const isLinkWithIcon = mapping.linkIcon && data.url ? true : false;
    const isTag = !hasAnyLink && !!mapping.tagIcon;
    const isMarkdown = !!renderUserInteractions && !hasAnyLink && !isTag && !!mapping.markdown;
    const isCollapsible = !!renderUserInteractions && !hasAnyLink && !isTag && mapping.collapsible && typeof data.value === "string" && data.value.length >= 1600;
    const showPreview = !!renderUserInteractions && data.previewUrl && (typeof data.previewUrl === "string" || typeof data.previewUrl.url === "string");

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
        id: instanceIdLink,
        group: group,
        text: value ? value : instanceIdLink
      };
    } else if (hasLink || isLinkWithIcon) {
      ValueComponent = Link;
      valueProps = {
        url: data.url,
        label: value,
        isAFileLink: isAFileLink,
        isExternalLink: hasMailToLink || hasExternalLink,
        icon: mapping.linkIcon
      };
    } else if (isTag) {
      ValueComponent = Tag;
      valueProps = {
        icon: mapping.tagIcon,
        value: value
      };
    } else if (isCollapsible) {
      ValueComponent = CollapsibleText;
      valueProps = {
        content: value,
        isMarkdown: isMarkdown
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
        {!!mapping.termsOfUse && (
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