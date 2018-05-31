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

import React, { Component } from 'react';
import { isMobile } from '../../Helpers/BrowserHelpers';
 
export class MobileKeyboardHandler extends Component {
    _scrollHandler(e) {
      const searchInput = document.querySelector(this.props.inputSelector);
      if (document.activeElement === searchInput && (!this._searchInputClickedTimestamp ||  ((new Date() - this._searchInputClickedTimestamp) > 500))) {
        searchInput.blur();
      }
    }
    _searchInputClickHandler(e) {
      const searchInput = document.querySelector(this.props.inputSelector);
      if (document.activeElement !== searchInput) {
        searchInput.focus();
      }
      this._searchInputClickedTimestamp = new Date();
    }
    componentDidMount() {
      if (isMobile) {
        window.addEventListener("scroll", this._scrollHandler.bind(this), false);
        window.addEventListener("mousedown", this._searchInputClickHandler.bind(this), false);
      }
    }
    componentWillUnmount() {
      if (isMobile) {
        window.removeEventListener("scroll", this._scrollHandler);
        window.removeEventListener("mousedown", this._searchInputClickHandler);
      }
    }
    render() {
      return (
        <span>{this.props.children}</span>
      );
    }
  }