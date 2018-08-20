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
import { store } from "../../../../store";
import { SearchkitComponent, SearchBox, QueryString } from "searchkit";
import { isMobile } from "../../../../Helpers/BrowserHelpers";
import "./styles.css";

const SearchPanelComponent = ({isFloating, queryFields, onSearch}) => {
  return (
    <div className={`kgs-search${isFloating?" is-fixed-position":""}`}>
      <SearchBox placeholder="Search (e.g. brain or neuroscience)" autofocus={true} searchOnChange={false} queryFields={queryFields} queryBuilder={QueryString} />
      <button className="kgs-search-button" onClick={onSearch}>Search</button>
      <a href="http://lucene.apache.org/core/2_9_4/queryparsersyntax.html" target="blank" className="kgs-search-help__button" title="Help">
        <i className="fa fa-info-circle fa-2x"></i>
      </a>
    </div>
  );
};

class SearchInput {
  constructor(querySelector) {
    this.timestamp = null;
    this.querySelector = querySelector;
  }
  get element() {
    return  document.querySelector(this.querySelector);
  }
  blur() {
    const input = this.element;
    if (input && document.activeElement === input && (!this.timestamp ||  ((new Date() - this.timestamp) > 500))) {
      input.blur();
    }
  }
  focus() {
    const input = this.element;
    if (input && document.activeElement !== input) {
      input.focus();
    }
    this.timestamp = new Date();
  }
}

const searchInput = new SearchInput(".kgs-search .sk-search-box .sk-top-bar__content .sk-search-box__text");

export class SearchPanel extends SearchkitComponent {
  constructor(props) {
    super(props);
    this.state = {
      isFloating: false,
      queryFields: null
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
    this.handleMouseDownEvent = this.handleMouseDownEvent.bind(this);
    this.handleScrollEvent = this.handleScrollEvent.bind(this);
    this.handleResizeEvent = this.handleResizeEvent.bind(this);
    this.handleOrientationChangeEvent = this.handleOrientationChangeEvent.bind(this);
    this.handleMutationEvent = this.handleMutationEvent.bind(this);
  }
  handleStateChange() {
    setTimeout(() => {
      const globalState = store.getState();
      this.setState({queryFields: globalState.definition.queryFields});
    });
  }
  componentDidMount() {
    document.addEventListener("state", this.handleStateChange.bind(this), false);
    this.handleStateChange();
    if (isMobile) {
      window.addEventListener("mousedown", this.handleMouseDownEvent, false);
    }
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
    document.removeEventListener("state", this.handleStateChange);
    if (isMobile) {
      window.removeEventListener("mousedown", this.handleMouseDownEvent);
    }
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
  handleMouseDownEvent() {
    searchInput.focus();
  }
  handleScrollEvent() {
    if (isMobile) {
      searchInput.blur();
    }
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
          [].forEach.call(e.nodes, n => {
            e.height += n.offsetHeight;
          });
        }
      }
      height += e.height;
    });
    if (cookieChange || eventWasTriggered) {
      if (document.documentElement.scrollTop < height) {
        this.setState({isFloating: false});
      } else {
        this.setState({isFloating: true});
      }
    }
  }
  render() {
    const searchHandler = () => {
      this.searchkit.search();
    };
    return (
      <SearchPanelComponent isFloating={this.state.isFloating} queryFields={this.state.queryFields} onSearch={searchHandler} />
    );
  }
}