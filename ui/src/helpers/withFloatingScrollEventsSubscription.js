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
import uniqueId from "lodash/uniqueId";
import { windowHeight } from "../helpers/BrowserHelpers";

const jQuerCollapsibleMenuQuerySelector = ".js-navbar-header";

const getStatusForElement = (e, hasSizeChanged) => {
  let cookieChange = false;
  let localStorageChange = false;
  let doCalc = e.height === undefined || hasSizeChanged;
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
  return {
    cookieChange: cookieChange,
    localStorageChange: localStorageChange,
    height: e.height
  };
};

// Return null when layout doesn't need any adjustement
const getHeight = (relatedElements, hasSizeChanged, hasChanged) => {
  let cookieChange = false;
  let localStorageChange = false;
  let height = 0;
  relatedElements.forEach(e => {
    const status = getStatusForElement(e, hasSizeChanged);
    cookieChange = cookieChange || status.cookieChange;
    localStorageChange = localStorageChange || status.localStorageChange;
    height += status.height;
  });
  if (hasChanged || localStorageChange || cookieChange) {
    return height;
  }
  return null;
};

// Return null if floating state should not be updated
const getIsFloating = (height, floatingPosition, scrollTop, newScrollTop) => {
  if (floatingPosition === "top") {
    return newScrollTop > height;
  } else if (floatingPosition === "bottom") {
    const scrollDown = (newScrollTop - scrollTop) > 0;
    const fixedLayout = document.documentElement.scrollHeight - scrollTop - windowHeight() < height;
    if (scrollDown && fixedLayout) {
      return false;
    } else if (!scrollDown && !fixedLayout) {
      return true;
    }
  }
  return null;
};

class WithEventsBase extends React.Component {
  constructor(props) {
    super(props);
    this.eventId = uniqueId("kgs-");
    this.eventState = {
      didScroll: false,
      didResize: false,
      didOrientationChange: false,
      didMute: false
    };
    this.observer = null;
    this.handleScrollEvent = this.handleScrollEvent.bind(this);
    this.handleResizeEvent = this.handleResizeEvent.bind(this);
    this.handleOrientationChangeEvent = this.handleOrientationChangeEvent.bind(this);
    this.handleMutationEvent = this.handleMutationEvent.bind(this);
    this.listenToDOMMutations = this.listenToDOMMutations.bind(this);
    this.unlistenDOMMutations = this.unlistenDOMMutations.bind(this);
    this.resetEvents = this.resetEvents.bind(this);
    this.hasChanged =  this.hasChanged.bind(this);
    this.didScroll =  this.didScroll.bind(this);
    this.hasSizeChanged = this.hasSizeChanged.bind(this);
  }
  componentDidMount() {
    window.addEventListener("scroll", this.handleScrollEvent, false);
    window.addEventListener("resize", this.handleResizeEvent, false);
    window.addEventListener("orientationchange", this.handleOrientationChangeEvent, false);
    this.listenToDOMMutations();
    window.$ && window.$(jQuerCollapsibleMenuQuerySelector).on(`shown.bs.collapse.${this.eventId}`, this.handleResizeEvent);
    window.$ && window.$(jQuerCollapsibleMenuQuerySelector).on(`hidden.bs.collapse.${this.eventId}`, this.handleResizeEvent);
  }
  componentWillUnmount() {
    window.removeEventListener("scroll", this.handleScrollEvent);
    window.removeEventListener("resize", this.handleResizeEvent);
    window.removeEventListener("orientationchange", this.handleOrientationChangeEvent);
    this.unlistenDOMMutations();
    window.$ && window.$(jQuerCollapsibleMenuQuerySelector).off(`shown.bs.collapse.${this.eventId}`, this.handleResizeEvent);
    window.$ && window.$(jQuerCollapsibleMenuQuerySelector).off(`hidden.bs.collapse.${this.eventId}`, this.handleResizeEvent);
  }
  listenToDOMMutations() {
    if (window.MutationObserver && !this.observer) {
      this.observer = new MutationObserver(this.handleMutationEvent);
      this.observer.observe(document.body, { attributes: true, childList: true });
    }
  }
  unlistenDOMMutations() {
    if (this.observer) {
      this.observer.disconnect();
      delete this.observer;
      this.observer = null;
    }
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
  resetEvents() {
    this.eventState = {
      didScroll: false,
      didResize: false,
      didOrientationChange: false,
      didMute: false
    };
  }
  hasChanged() {
    return this.eventState.didResize || this.eventState.didOrientationChange || this.eventState.didMute || this.eventState.didScroll;
  }
  didScroll() {
    return this.eventState.didScroll;
  }
  hasSizeChanged() {
    return this.eventState.didResize || this.eventState.didOrientationChange || this.eventState.didMute;
  }
}

export const withFloatingScrollEventsSubscription = (floatingPosition, relatedElements) => (WrappedComponent) => {

  floatingPosition = floatingPosition?floatingPosition.toLowerCase():null;
  relatedElements = Array.isArray(relatedElements)?relatedElements:[];

  class WithEvents extends WithEventsBase {
    constructor(props) {
      super(props);
      this.state = {
        isFloating: false
      };
      this.initialized = false;
      this.interval = null;
      this.scrollTop = 0;
      this.checkLayoutChange = this.checkLayoutChange.bind(this);
      this.adjustLayout = this.adjustLayout.bind(this);
    }
    componentDidMount() {
      super.componentDidMount();
      this.interval = setInterval(this.checkLayoutChange, 250);
    }
    componentWillUnmount() {
      clearInterval(this.interval);
      this.interval = null;
      super.componentWillUnmount();
    }
    adjustLayout(height) {
      const newScrollTop = window.scrollY || document.documentElement.scrollTop;
      const isFloating = getIsFloating(height, floatingPosition, this.scrollTop, newScrollTop);
      // new scrollTop should be set after isFloating calculation but before setting the state
      if (floatingPosition === "bottom") {
        this.scrollTop = newScrollTop;
      }
      if (isFloating !== null) {
        this.setState({isFloating: isFloating});
      }
    }
    checkLayoutChange() {
      const hasSizeChanged = !this.initialized || this.hasSizeChanged();
      const hasChanged = !this.initialized || this.hasChanged();
      this.resetEvents();
      this.initialized = true;
      const height = getHeight(relatedElements, hasSizeChanged, hasChanged);
      if (height !== null ) {
        this.adjustLayout(height);
      }
    }
    render() {
      return <WrappedComponent {...this.props} isFloating={this.state.isFloating}  />;
    }
  }
  return WithEvents;
};