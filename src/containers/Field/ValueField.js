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
import { connect } from "react-redux";
import * as actions from "../../actions";
import { termsOfUse } from "../../data/termsOfUse.js";
import { Icon } from "../../components/Icon";
import { Details } from "../../components/Details";
import { Text } from "../../components/Text";
import "./ValueField.css";

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

const ReferenceComponent = ({text, reference, group, onClick}) => {
  if (!reference) {
    return null;
  }

  const handleClick = () => {
    typeof onClick === "function" && onClick(reference, group);
  };

  return (
    <button onClick={handleClick} role="link">{text}</button>
  );
};

const Reference = connect(
  (state, props) => ({
    text: props.text?props.text:props.reference,
    reference: props.reference,
    group: props.group,
  }),
  dispatch => ({
    onClick: (reference, group) => dispatch(actions.loadInstance(reference, group))
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

class Thumbnail extends PureComponent {
  constructor(props) {
    super(props);
    this.state = {
      previewUrl: null
    };
  }
  handleToggle() {
    const { previewUrl } = this.props;
    this.setState(state => {
      const newValue = state.previewUrl?null: previewUrl;
      if (newValue) {
        this.listenClickOutHandler();
      } else {
        this.unlistenClickOutHandler();
      }
      return {
        previewUrl: newValue
      };
    });
  }

  clickOutHandler = e => {
    if(!this.wrapperRef || !this.wrapperRef.contains(e.target)){
      this.unlistenClickOutHandler();
      this.setState({previewUrl: null});
    }
  };

  listenClickOutHandler(){
    window.addEventListener("mouseup", this.clickOutHandler, false);
    window.addEventListener("touchend", this.clickOutHandler, false);
    window.addEventListener("keyup", this.clickOutHandler, false);
  }

  unlistenClickOutHandler(){
    window.removeEventListener("mouseup", this.clickOutHandler, false);
    window.removeEventListener("touchend", this.clickOutHandler, false);
    window.removeEventListener("keyup", this.clickOutHandler, false);
  }

  componentWillUnmount(){
    this.unlistenClickOutHandler();
  }

  render() {
    const {thumbnailUrl, showPreview, previewUrl, isDynamic, alt} = this.props;

    if (!showPreview || typeof previewUrl !== "string") {
      if (typeof thumbnailUrl === "string") {
        return (
          <span className="kgs-thumbnail--panel">
            <div><img src={thumbnailUrl} alt={alt} /></div>
          </span>
        );
      }
      return (
        <span className="fa-stack fa-1x kgs-thumbnail--panel">
          <i className="fa fa-file-o fa-stack-1x"></i>
        </span>
      );
    }

    return (
      <div className="fa-stack fa-1x kgs-thumbnail--container" ref={ref=>this.wrapperRef = ref}>
        <button className="kgs-thumbnail--button" onClick={this.handleToggle.bind(this)} >
          {typeof thumbnailUrl === "string"?
            <span className="kgs-thumbnail--panel">
              <div className="kgs-thumbnail--image">
                <img src={thumbnailUrl} alt={alt} />
                {isDynamic?
                  <i className="fa fa-play kgs-thumbnail--zoom-dynamic"></i>
                  :
                  <i className="fa fa-search kgs-thumbnail--zoom-static"></i>
                }
              </div>
            </span>
            :
            <span className="fa-stack fa-1x kgs-thumbnail--panel">
              <i className="fa fa-file-image-o fa-stack-1x"></i>
              {isDynamic?
                <i className="fa fa-play fa-stack-1x kgs-thumbnail--zoom-dynamic"></i>
                :
                <i className="fa fa-search fa-stack-1x kgs-thumbnail--zoom-static"></i>
              }
            </span>
          }
        </button>
        {!!this.state.previewUrl && (
          <div className="fa-stack fa-1x kgs-thumbnail--preview" onClick={this.handleToggle.bind(this)}>
            <img src={this.state.previewUrl} alt={alt} />
            <i className="fa fa-close"></i>
          </div>
        )}
      </div>
    );
  }
}

const ValueFieldBase = (renderUserInteractions = true) => {

  const ValueField = ({show, data, mapping, group}) => {
    if (!show  || !data|| !mapping || !mapping.visible) {
      return null;
    }

    if (Math.round(Math.random() * 10) % 2 === 0) {
      if (Math.round(Math.random() * 10) % 2 === 0) {
        data.previewUrl = {
          src: "https://cdn2.thecatapi.com/images/18f.gif",
          isDynamic: true
        };
      } else {
        data.previewUrl = {
          src: "http://lorempixel.com/output/cats-q-c-640-480-3.jpg",
          isDynamic: false
        };
      }
      data.thumbnailUrl = "https://dogtowndogtraining.com/wp-content/uploads/2012/06/300x300-clicker.jpg";
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
    const showPreview = !!renderUserInteractions && data.previewUrl && (typeof data.previewUrl === "string" || typeof data.previewUrl.src === "string");

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
          <Thumbnail showPreview={showPreview} thumbnailUrl={data.thumbnailUrl} previewUrl={data.previewUrl && (typeof data.previewUrl === "string"?data.previewUrl:data.previewUrl.src)} isDynamic={data.previewUrl && data.previewUrl.isDynamic} alt={data.url} />
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