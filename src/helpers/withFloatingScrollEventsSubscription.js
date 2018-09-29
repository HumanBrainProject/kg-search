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
import { generateKey } from "../helpers/OIDCHelpers";
import { windowHeight } from "../helpers/BrowserHelpers";

const jQuerCollapsibleMenuQuerySelector = ".js-navbar-header";

export const withFloatingScrollEventsSubscription = (floatingPosition, relatedElements) => (WrappedComponent) => {

  floatingPosition = floatingPosition?floatingPosition.toLowerCase():null;
  relatedElements = Array.isArray(relatedElements)?relatedElements:[];

  class withEvents extends PureComponent {
    constructor(props) {
      super(props);
      this.eventId = "kgs-" + generateKey();
      this.state = {
        isFloating: false
      };
      this.eventState = {
        scrollTop: 0,
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
      window.addEventListener("scroll", this.handleScrollEvent, false);
      window.addEventListener("resize", this.handleResizeEvent, false);
      window.addEventListener("orientationchange", this.handleOrientationChangeEvent, false);
      if (window.MutationObserver) {
        this.observer = new MutationObserver(this.handleMutationEvent);
        this.observer.observe(document.body, { attributes: true, childList: true });
      }
      window.$ && window.$(jQuerCollapsibleMenuQuerySelector).on(`shown.bs.collapse.${this.eventId}`, this.handleResizeEvent);
      window.$ && window.$(jQuerCollapsibleMenuQuerySelector).on(`hidden.bs.collapse.${this.eventId}`, this.handleResizeEvent);
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
      window.$ && window.$(jQuerCollapsibleMenuQuerySelector).off(`shown.bs.collapse.${this.eventId}`, this.handleResizeEvent);
      window.$ && window.$(jQuerCollapsibleMenuQuerySelector).off(`hidden.bs.collapse.${this.eventId}`, this.handleResizeEvent);
      clearInterval(this.interval);
      this.interval = null;
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
      let height = 0;
      const needSizeCalculation = !this.initialized || this.eventState.didResize || this.eventState.didOrientationChange || this.eventState.didMute;
      const eventWasTriggered = this.eventState.didScroll || needSizeCalculation;
      this.eventState = {
        scrollTop: this.eventState.scrollTop,
        didScroll: false,
        didResize: false,
        didOrientationChange: false,
        didMute: false
      };
      this.initialized = true;
      let cookieChange = false;
      let localStorageChange = false;
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
        if (e.localStorageKey && (e.localStorageValue === undefined || e.localStorageValue === null)) {
          const value = localStorage.getItem(e.localStorageKey);
          if (e.localStorageValue !== value) {
            e.localStorageValue = value;
            localStorageChange = true;
            if (value !== null) {
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
            [].forEach.call(e.nodes, n => {
              e.height += n.offsetHeight;
            });
          }
        }
        height += e.height;
      });
      if (localStorageChange || cookieChange || eventWasTriggered) {
        const scrollTop = window.scrollY || document.documentElement.scrollTop;
        if (floatingPosition === "top") {
          if (scrollTop < height) {
            this.setState(() => ({isFloating: false}));
          } else {
            this.setState(() => ({isFloating: true}));
          }
        } else if (floatingPosition === "bottom") {
          const scrollDown = (scrollTop - this.eventState.scrollTop) > 0;
          const fixedLayout = document.documentElement.scrollHeight - this.eventState.scrollTop - windowHeight() < height;
          this.eventState.scrollTop = scrollTop;
          if (scrollDown && fixedLayout) {
            this.setState(() => ({isFloating: false}));
          } else if (!scrollDown && !fixedLayout) {
            this.setState(() => ({isFloating: true}));
          }
        }
      }
    }
    render() {
      return <WrappedComponent {...this.props} isFloating={this.state.isFloating}  />;
    }
  }
  return withEvents;
};