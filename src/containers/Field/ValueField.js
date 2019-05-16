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
import { termsOfUse } from "../../data/termsOfUse.js";
import { Icon } from "../../components/Icon";
import { Details } from "../../components/Details";
import { Text } from "../../components/Text";
import { CollapsibleText } from "../../components/CollapsibleText";
import { Link } from "../../components/Link";
import { Tag } from "../../components/Tag";
import { Thumbnail } from "../../components/Thumbnail";
import { Reference } from "./Reference";
import "./ValueField.css";

const ValueFieldBase = (renderUserInteractions = true) => {

  const ValueField = ({show, data, mapping, group}) => {
    if (!show  || !data|| !mapping || !mapping.visible) {
      return null;
    }

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

    const hasReference = !!renderUserInteractions && !!data.reference;
    const hasLink =  !!renderUserInteractions && !!data.url;
    const hasMailToLink = !!renderUserInteractions && typeof data.url === "string" &&  data.url.substr(0,7).toLowerCase() === "mailto:";
    const isAFileLink = typeof data.url === "string" && /^https?:\/\/.+\.cscs\.ch\/.+$/.test(data.url);
    const hasAnyLink = hasReference || hasMailToLink || hasLink;
    const isIcon = mapping.type === "icon" && ((data.image && data.image.url) || mapping.icon);
    const isTag = !hasAnyLink && !isIcon && !!mapping.tagIcon;
    const isMarkdown = !!renderUserInteractions && !hasAnyLink && !isIcon && !isTag && !!mapping.markdown;
    const isCollapsible = !!renderUserInteractions && !hasAnyLink && !isIcon && !isTag && mapping.collapsible && typeof data.value === "string" && data.value.length >= 1600;
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
    if (hasReference) {
      ValueComponent = Reference;
      valueProps = {
        reference: data.reference,
        group: group,
        text: value?value:data.reference
      };
    } else if (hasLink) {
      ValueComponent = Link;
      valueProps = {
        url: data.url,
        label: value,
        isMailToLink: hasMailToLink
      };
    } else if (isIcon) {
      ValueComponent = Icon;
      valueProps = {
        title: value,
        url: data && data.image && data.image.url,
        inline: mapping && mapping.icon
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
          <Thumbnail showPreview={showPreview} thumbnailUrl={data.thumbnailUrl && (typeof data.thumbnailUrl === "string"?data.thumbnailUrl:data.thumbnailUrl.url)} previewUrl={data.previewUrl && (typeof data.previewUrl === "string"?data.previewUrl:data.previewUrl.url)} isAnimated={data.previewUrl && data.previewUrl.isAnimated} alt={data.url} />
        )}
        <ValueComponent {...valueProps} />
        {!!mapping.termsOfUse && (
          <Details toggleLabel="Terms of use" content={termsOfUse} />
        )}
      </div>
    );
  };

  return ValueField;
};

export const ValueField = ValueFieldBase(true);
export const PrintViewValueField = ValueFieldBase(false);