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
import { TopBar, SearchBox, QueryString } from "searchkit";
import "./styles.css";

export class SearchPanel extends Component {
  constructor(props) {
    super(props);
    this.state = {
      className: "kgs-search-panel",
      style: {
        display: "none",
        position: "",
        top: "-99999px",
      }
    };
    this.eventState = {
      didScroll: false,
      didResize: false,
      didOrientationChange: false,
      didMute: false
    };
    this.initialized = false;
    this.observer = null;
    this.interval = null;
    this.adjustLayout = this.adjustLayout.bind(this);
    this.handleScrollEvent = this.handleScrollEvent.bind(this);
    this.handleResizeEvent = this.handleResizeEvent.bind(this);
    this.handleOrientationChangeEvent = this.handleOrientationChangeEvent.bind(this);
    this.handleMutationEvent = this.handleMutationEvent.bind(this);
  }
  componentDidMount() {
    window.addEventListener("scroll", this.handleScrollEvent);
    window.addEventListener("resize", this.handleResizeEvent);
    window.addEventListener("orientationchange", this.handleOrientationChangeEvent);
    if (window.MutationObserver) {
      this.observer = new MutationObserver(this.handleMutationEvent);
      this.observer.observe(document.body, { attributes: true, childList: true });
    }
    window.$ && window.$(".js-navbar-header").on("shown.bs.collapse.kgs-search-panel", this.handleResizeEvent);
    window.$ && window.$(".js-navbar-header").on("hidden.bs.collapse.kgs-search-panel", this.handleResizeEvent);
    this.interval = setInterval(this.adjustLayout, 250);
  }
  componentWillUnmount() {
    window.removeEventListener("scroll", this.handleScrollEvent);
    window.removeEventListener("resize", this.handleResizeEvent);
    window.removeEventListener("orientationchange", this.handleOrientationChangeEvent);
    if (this.observer) {
      this.observer.disconnect();
      delete this.observer;
      this.observer = null;
    }
    window.$ && window.$(".js-navbar-header").off("shown.bs.collapse.kgs-search-panel", this.handleResizeEvent);
    window.$ && window.$(".js-navbar-header").off("hidden.bs.collapse.kgs-search-panel", this.handleResizeEvent);
    clearInterval(this.interval);
    this.interval = null;
  }
  shouldComponentUpdate(nextProps, nextState) {
    return nextState.style.display !== this.state.style.display || nextState.style.position !== this.state.style.position || nextState.style.top !== this.state.style.top;
  }
  handleScrollEvent() {
    this.eventState.didScroll = true;
  }
  handleResizeEvent() {
    this.eventState.didResize = true;
  }
  handleOrientationChangeEvent() {
    this.eventState.didOrientationChange = true;
  }
  handleMutationEvent() {
    this.eventState.didMute = true;
  }
  adjustLayout() {
    const {relatedElements} = this.props;
    let height = 0;
    const needSizeCalculation = !this.initialized || this.eventState.didResize || this.eventState.didOrientationChange || this.eventState.didMute;
    const eventWasTriggered = this.eventState.didScroll || needSizeCalculation;
    this.eventState = {
      didScroll: false,
      didResize: false,
      didOrientationChange: false,
      didMute: false
    };
    this.initialized = true;
    let cookieChange = false;
    relatedElements.forEach(e => {
      let doCalc = e.height === undefined || needSizeCalculation;
      if (e.cookieKey && (e.cookieValue === undefined || e.cookieValue === "")) {
        let value = document.cookie;
        if (typeof e.cookieKey === "string") {
          value = "";
          const cookie = "; " + document.cookie;
          const parts = cookie.split("; " + e.cookieKey + "=");
          if (parts.length === 2) {
            value = parts.pop().split(";").shift();
          }
        }
        if (e.cookieValue !== value) {
          e.cookieValue = value;
          cookieChange = true;
          if (value !== "") {
            e.height = 0;
          } else {
            doCalc = true;
          }
        }
      }
      if (doCalc) {
        e.height = 0;
        if (!e.conditionQuerySelector || document.querySelector(e.conditionQuerySelector)) {
          if (!e.nodes || !e.nodes.length) {
            e.nodes = document.querySelectorAll(e.querySelector);
          }
          e.nodes.forEach(n => {
            e.height += n.offsetHeight;
          });
        }
      }
      height += e.height;
    });
    if (cookieChange || eventWasTriggered) {
      if (document.documentElement.scrollTop < height) {
        this.setState({className: "kgs-search", style: {position: "absolute", top: height + "px"}});
      } else {
        this.setState({className: "kgs-search kgs-search-panel", style: {position: "fixed", top: "0"}});
      }
    }
  }
  render() {
    const { searchThrottleTime, queryFields } = this.props;
    const isMobile = ( navigator.userAgent.match(/Android/i)
                    || navigator.userAgent.match(/webOS/i)
                    || navigator.userAgent.match(/iPhone/i)
                    || navigator.userAgent.match(/iPad/i)
                    || navigator.userAgent.match(/iPod/i));
    return (
      <div className={this.state.className} style={this.state.style}>
        <TopBar>
          <SearchBox placeholder="Search (e.g. brain AND hippocampus)" autofocus={true} searchThrottleTime={searchThrottleTime} searchOnChange={!isMobile} queryFields={queryFields} queryBuilder={QueryString} />
          <a href="http://lucene.apache.org/core/2_9_4/queryparsersyntax.html" target="blank" className="kgs-help__button" title="Help">
            <i className="fa fa-info-circle kgs-help__button-icon"></i>
          </a>
        </TopBar>
      </div>
    );
  }
}