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

import React, { PureComponent } from "react";
import showdown from "showdown";
/*import FilterXSS from 'xss';*/
import xssFilter from "showdown-xss-filter";
import { store, dispatch } from "../../../../store";
import * as actions from "../../../../actions";
import "./styles.css";

const converter = new showdown.Converter({extensions: [xssFilter]});

const Text = ({content, isMarkdown}) => {
  if (!content) {
    return null;
  }
  if (!isMarkdown) {
    return (
      <span>{content}</span>
    );
  }

  const html = converter.makeHtml(content);
  return (
    <span className="field-markdown" dangerouslySetInnerHTML={{__html:html}}></span>
  );
};

class CollapsibleText extends PureComponent {
  constructor(props) {
    super(props);
    this.state = {
      collapsed: true,
    };
    this.handleClick = this.handleClick.bind(this);
  }
  handleClick() {
    this.setState({collapsed: !this.state.collapsed});
  }

  render() {
    const {content, isMarkdown} = this.props;

    if (!content) {
      return null;
    }

    const className = `collapse ${this.state.collapsed?"":"in"}`;
    return (
      <span className="field-text collapsible">
        <span className={className}>
          <Text content={content} isMarkdown={isMarkdown} />
        </span>
        {this.state.collapsed && (
          <button onClick={this.handleClick}>more...</button>
        )}
      </span>
    );
  }
}

const Reference = ({reference, label}) => {
  if (!reference) {
    return null;
  }

  const text = label?label:reference;

  const handleClick = () => {
    const state = store.getState();
    dispatch(actions.loadHit(reference, state.search.index));
  };

  return (
    <button onClick={handleClick} role="link">{text}</button>
  );
};

const Link = ({url, label, isMailToLink}) => {
  if (!url) {
    return null;
  }

  const text = label?label:url;

  const aProps = !isMailToLink?null:{
    rel: "noopener noreferrer",
    target: "_blank"
  };

  return (
    <a href={url} {...aProps}>{text}</a>
  );
};

const Tag = ({icon, value}) => {
  if (!icon && !value) {
    return null;
  }

  return (
    <span className="field-value__tag">
      <div dangerouslySetInnerHTML={{__html:icon}} />
      <div>{value}</div>
    </span>
  );
};

class Details extends PureComponent {
  constructor(props) {
    super(props);
    this.state = {
      collapsed: true
    };
    this.handleToggle = this.handleToggle.bind(this);
    this.handleClose = this.handleClose.bind(this);
  }
  handleToggle() {
    this.setState({collapsed: !this.state.collapsed});
  }
  handleClose() {
    this.setState({collapsed: true});
  }
  render() {
    const {toggleLabel, content} = this.props;

    if (!content) {
      return null;
    }

    const className = `toggle ${this.state.collapsed?"":"in"}`;
    return (
      <span className="field-details">
        <button className={className} onClick={this.handleToggle}>
          <i className="fa fa-exclamation-circle"></i>
          {toggleLabel && (
            <span>{toggleLabel}</span>
          )}
        </button>
        <div className="collapsible">
          <div className="field-details__panel">
            <Text content={content} isMarkdown={true} />
            <button className="field-details__close-button" onClick={this.handleClose} title="close"><i className="fa fa-2x fa-close"></i></button>
          </div>
        </div>
      </span>
    );
  }
}

export function ValueField({show, data, mapping, showSmartContent}) {
  if (!show  || !data|| !mapping || !mapping.visible) {
    return null;
  }

  const hasReference = showSmartContent && !!data.reference;
  const hasLink =  showSmartContent && !!data.url;
  const hasMailToLink = showSmartContent && data.url === "string" &&  data.url.substr(0,7).toLowerCase() === "mailto:";
  const hasAnyLink = hasReference || hasMailToLink || hasLink;
  const isTag = !hasAnyLink && !!mapping.tag_icon;
  const isMarkdown = showSmartContent && !hasAnyLink && !isTag && !!mapping.markdown;
  const isCollapsible = showSmartContent && !hasAnyLink && !isTag && mapping.collapsible && typeof data.value === "string" && data.value.length >= 1600;

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
      label: value
    };
  } else if (hasLink) {
    ValueComponent = Link;
    valueProps = {
      url: data.url,
      label: value,
      isMailToLink: hasMailToLink
    };
  } else if (isTag) {
    ValueComponent = Tag;
    valueProps = {
      icon: mapping.tag_icon,
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

  const detailsProps = {
    toggleLabel: mapping.detail_label,
    content: data.detail
  };

  return (
    <div className="field-value">
      <ValueComponent {...valueProps} />
      {data.detail && (
        <Details {...detailsProps} />
      )}
    </div>
  );
}