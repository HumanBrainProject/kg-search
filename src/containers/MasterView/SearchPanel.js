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
import { SearchBox, QueryString } from "searchkit";
import { connect } from "react-redux";
import * as actions from "../../actions";
import { help } from "../../data/help.js";
import { withFloatingScrollEventsSubscription } from "../../helpers/withFloatingScrollEventsSubscription";
import { SearchkitComponent } from "searchkit";
import { isMobile } from "../../helpers/BrowserHelpers";
import "./SearchPanel.css";

class SearchInput {
  constructor(querySelector) {
    this.timestamp = null;
    this.querySelector = querySelector;
  }
  get element() {
    return document.querySelector(this.querySelector);
  }
  blur() {
    const input = this.element;
    if (input && document.activeElement === input && (!this.timestamp || ((new Date() - this.timestamp) > 500))) {
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

class SearchkitSeachPanelContainer extends SearchkitComponent {
  componentDidMount() {
    if (isMobile) {
      window.addEventListener("mousedown", this.handleMouseDownEvent, false);
    }
    window.addEventListener("scroll", this.handleScrollEvent);
  }
  componentWillUnmount() {
    if (isMobile) {
      window.removeEventListener("mousedown", this.handleMouseDownEvent);
    }
  }
  handleMouseDownEvent() {
    searchInput.focus();
  }
  handleScrollEvent() {
    searchInput.blur();
  }
  render() {
    const { isFloating, onHelp } = this.props;
    const handleSearch = () => {
      this.searchkit.search();
    };

    return (
      <div className={`kgs-search ${isFloating ? " is-fixed-position" : ""}`}>
        <SearchBox placeholder="Search (e.g. brain or neuroscience)" autofocus={true} searchOnChange={false} queryBuilder={QueryString} />
        <button className="kgs-search-button" onClick={handleSearch}>Search</button>
        <button type="button" className="kgs-search-help__button" title="Help" onClick={onHelp}>
          <i className="fa fa-info-circle fa-2x"></i>
        </button>
      </div>
    );
  }
}

const SearchPanelContainer = connect(
  (state, props) => ({
    isFloating: props.isFloating,
    relatedElements: props.relatedElements
  }),
  dispatch => ({
    onHelp: () => dispatch(actions.setInfo(help))
  })
)(SearchkitSeachPanelContainer);

export const SearchPanel = withFloatingScrollEventsSubscription(
  "top",
  [
    { querySelector: "header.site-navigation" },
    { querySelector: "#CookielawBanner", cookieKey: "cookielaw_accepted" }
  ]
)(SearchPanelContainer);
