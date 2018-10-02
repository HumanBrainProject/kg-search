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
import { connect } from "react-redux";
import * as actions from "../../actions";
import { termsOfUse } from "../../data/termsOfUse.js";
import { Icon } from "../../components/Icon";
import "./ValueField.css";

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
    this.setState(state => ({ collapsed: !state.collapsed }));
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

const ReferenceComponent = ({text, reference, index, onClick}) => {
  if (!reference) {
    return null;
  }

  const handleClick = () => {
    typeof onClick === "function" && onClick(reference, index);
  };

  return (
    <button onClick={handleClick} role="link">{text}</button>
  );
};

const Reference = connect(
  (state, props) => ({
    text: props.text?props.text:props.reference,
    reference: props.reference,
    index: props.index,
  }),
  dispatch => ({
    onClick: (reference, index) => dispatch(actions.loadInstance(reference, index))
  })
)(ReferenceComponent);

const Link = ({url, label, isMailToLink}) => {
  if (!url) {
    return null;
  }

  const text = label?label:url;

  const props = !isMailToLink?null:{
    rel: "noopener noreferrer",
    target: "_blank"
  };

  return (
    <a href={url} {...props}>{text}</a>
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
    this.setState(state => ({ collapsed: !state.collapsed }));
  }
  handleClose() {
    this.setState(() => ({ collapsed: true }));
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
          {!this.state.collapsed && (
            <div className="field-details__panel">
              <Text content={content} isMarkdown={true} />
              <button className="field-details__close-button" onClick={this.handleClose} title="close"><i className="fa fa-2x fa-close"></i></button>
            </div>
          )}
        </div>
      </span>
    );
  }
}

const ValueFieldBase = (renderUserInteractions = true) => {

  const ValueField = ({show, data, mapping, index}) => {
    if (!show  || !data|| !mapping || !mapping.visible) {
      return null;
    }

    const hasReference = !!renderUserInteractions && !!data.reference;
    const hasLink =  !!renderUserInteractions && !!data.url;
    const hasMailToLink = !!renderUserInteractions && data.url === "string" &&  data.url.substr(0,7).toLowerCase() === "mailto:";
    const hasAnyLink = hasReference || hasMailToLink || hasLink;
    const isIcon = mapping.type === "icon" && ((data.image && data.image.url) || mapping.icon);
    const isTag = !hasAnyLink && !isIcon && !!mapping.tag_icon;
    const isMarkdown = !!renderUserInteractions && !hasAnyLink && !isIcon && !isTag && !!mapping.markdown;
    const isCollapsible = !!renderUserInteractions && !hasAnyLink && !isIcon && !isTag && mapping.collapsible && typeof data.value === "string" && data.value.length >= 1600;

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
        index: index,
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

    /*
    const detailsProps = {
      toggleLabel: mapping.detail_label,
      content: data.detail
    };
    */
    const detailsProps = {
      toggleLabel: "Terms of use",
      content: termsOfUse
    };

    return (
      <div className="field-value">
        <ValueComponent {...valueProps} />
        {(data.detail || data.terms_of_use) && (
          <Details {...detailsProps} />
        )}
      </div>
    );
  };

  return ValueField;
};

export const ValueField = ValueFieldBase(true);
export const PrintViewValueField = ValueFieldBase(false);