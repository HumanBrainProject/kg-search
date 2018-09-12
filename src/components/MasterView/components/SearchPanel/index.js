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
import { withStoreStateSubscription} from "../../../withStoreStateSubscription";
import { withFloatingScrollEventsSubscription} from "../../../withFloatingScrollEventsSubscription";
import { SearchkitComponent, SearchBox, QueryString } from "searchkit";
import { isMobile } from "../../../../Helpers/BrowserHelpers";
import "./styles.css";

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

class SearchPanelComponent extends SearchkitComponent {
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
    const {isFloating, queryFields} = this.props;
    const hanldeClick = () => {
      this.searchkit.search();
    };
    return (
      <div className={`kgs-search${isFloating?" is-fixed-position":""}`}>
        <SearchBox placeholder="Search (e.g. brain or neuroscience)" autofocus={true} searchOnChange={false} queryFields={queryFields} queryBuilder={QueryString} />
        <button className="kgs-search-button" onClick={hanldeClick}>Search</button>
        <a href="http://lucene.apache.org/core/2_9_4/queryparsersyntax.html" target="blank" className="kgs-search-help__button" title="Help">
          <i className="fa fa-info-circle fa-2x"></i>
        </a>
      </div>
    );
  }
}

export const SearchPanel = withStoreStateSubscription(
  withFloatingScrollEventsSubscription(SearchPanelComponent),
  data => ({
    queryFields: data.definition.queryFields
  })
);
