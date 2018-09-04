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

import React, { Component } from "react";
import showdown from "showdown";
/*import FilterXSS from 'xss';*/
import xssFilter from "showdown-xss-filter";
import { store, dispatch } from "../../../../store";
import * as actions from "../../../../actions";
import "./styles.css";

const converter = new showdown.Converter({extensions: [xssFilter]});

export class ValueField extends Component {
  constructor(props) {
    super(props);
    this.state = {
      showDetails: false,
      showMore: {},
    };
    this.toggleDetails = this.toggleDetails.bind(this);
    this.hideDetails = this.hideDetails.bind(this);
    this.showDetails = this.showDetails.bind(this);
    this.showMore = this.showMore.bind(this);
  }
  toggleDetails(expand) {
    this.setState({showDetails: expand===false||expand===true?expand:!this.state.showDetails});
  }
  hideDetails() {
    this.toggleDetails(false);
  }
  showDetails() {
    this.toggleDetails(true);
  }
  showMore(key) {
    const showMore = Object.assign({}, this.state.showMore);
    showMore[key] = true;
    this.setState({showMore: showMore});
  }
  render() {
    const {show, data, mapping, showSmartContent} = this.props;
    if (!show) {
      return null;
    }

    if (!mapping || !mapping.visible) {
      return null;
    }
    const handleClick = () => {
      const state = store.getState();
      dispatch(actions.loadHit(data.reference, state.search.index));
    };

    let valueTag = null;
    let detailsButton = null;
    let detailsContent = null;
    if (data) {
      if (data.reference && showSmartContent) {
        valueTag = <button onClick={handleClick} role="link">{data.value}</button>;
      } else if (data.url && showSmartContent) {
        if (data.url.substr(0,7).toLowerCase() === "mailto:") {
          valueTag = <a href={data.url}>{data.value}</a>;
        } else {
          if (data.detail) {
            valueTag = <a href={data.url} rel="noopener noreferrer" target="_blank">{data.value}</a>;
          } else {
            valueTag = <a href={data.url} rel="noopener noreferrer" target="_blank">{data.value}</a>;
          }
        }
      } else {
        const timestamp = data.value && mapping && mapping.type === "date" && Date.parse(data.value);
        if (timestamp && !isNaN(timestamp)) {
          valueTag = new Date(timestamp).toLocaleDateString();
        } else {
          if (showSmartContent && mapping && mapping.markdown) {
            const html = converter.makeHtml(data.value);
            valueTag = <span className="markdown" dangerouslySetInnerHTML={{__html:html}}></span>;
          } else {
            valueTag = data.value;
          }
          if (showSmartContent && mapping.collapsible && data.value.length >= 1600) {
            valueTag = <span className="collapsible">
              <span className={"collapse" + (this.state.showMore[mapping.value]?" in":"")}>
                {valueTag}
              </span>
              {!this.state.showMore[mapping.value] && (
                <button onClick={() => this.showMore(mapping.value)}>more...</button>
              )}
            </span>;
          }
          if(mapping && mapping.tag_icon){
            valueTag = <span className="field-value__tag"><div dangerouslySetInnerHTML={{__html:mapping.tag_icon}} /><div>{data.value}</div></span>;
          }
        }
      }
      if (data.detail) {
        const html = converter.makeHtml(data.detail);
        detailsButton = <button className="toggle-details-button" onClick={this.toggleDetails}><i className="fa fa-exclamation-circle"></i>{mapping && mapping.detail_label?<span>{mapping.detail_label}</span>:null}</button>;
        detailsContent = <div className="details-panel">
          <div className="details-inner-panel">
            <span className="details-markdown" dangerouslySetInnerHTML={{__html:html}}></span>
            <button className="collapse-details-button" onClick={this.hideDetails} title="close"><i className="fa fa-2x fa-close"></i></button>
          </div>
        </div>;
      }
    }

    return (
      <div className="field-value" data-showDetail={this.state.showDetails}>{valueTag}{detailsButton}{detailsContent}</div>
    );
  }
}
