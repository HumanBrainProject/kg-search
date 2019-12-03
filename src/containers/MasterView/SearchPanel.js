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
import { connect } from "react-redux";
import * as actions from "../../actions";
import { help } from "../../data/help.js";
import { withFloatingScrollEventsSubscription } from "../../helpers/withFloatingScrollEventsSubscription";
import { isMobile } from "../../helpers/BrowserHelpers";
import "./SearchPanel.css";
import { ElasticSearchHelpers } from "../../helpers/ElasticSearchHelpers";

class SeachPanelBaseComponent extends React.Component {
  constructor(props){
    super(props);
    this.ref = React.createRef();
    this.state = {
      value: ""
    };
  }

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

  handleMouseDownEvent = () => this.ref.current.focus();

  handleScrollEvent = () => this.ref.current.blur();

  handleChange = e => this.setState({value: e.target.value});

  handleSearch = () => {
    this.props.onQueryStringChange(this.state.value);
  }

  handleKeyDown = e => {
    if(e.key === "Enter") {
      this.props.onQueryStringChange(this.state.value);
    }
  }

  render() {
    const { isFloating, onHelp } = this.props;

    return (
      <div className={`kgs-search ${isFloating ? " is-fixed-position" : ""}`}>
        <i className="fa fa-search kg-search-bar__icon"></i>
        <input className="kg-search-bar"
          type="text"
          placeholder="Search (e.g. brain or neuroscience)"
          aria-label="Search"
          onChange={this.handleChange}
          onKeyDown={this.handleKeyDown}
          ref={this.ref}  />
        <button className="kgs-search-button" onClick={this.handleSearch}>Search</button>
        <button type="button" className="kgs-search-help__button" title="Help" onClick={onHelp}>
          <i className="fa fa-info-circle fa-2x"></i>
        </button>
      </div>
    );
  }
}

class SeachPanelComponent extends React.Component {

  componentDidUpdate(prevProps) {
    if (this.props.queryString !== prevProps.queryString) {
      this.performSearch();
    }
  }

  performSearch = () => {
    const { searchParams, onSearch, group, searchApiHost } = this.props;
    onSearch(searchParams, group, searchApiHost);
  }

  render() {
    const {isFloating, relatedElements, onQueryStringChange} = this.props;
    return (
      <SeachPanelBaseComponent isFloating={isFloating} relatedElements={relatedElements} onQueryStringChange={onQueryStringChange} />
    );
  }
}

const SearchPanelContainer = connect(
  (state, props) => {
    return {
      isFloating: props.isFloating,
      relatedElements: props.relatedElements,
      queryString: state.search.queryString,
      searchParams: ElasticSearchHelpers.getSearchParamsFromState(state),
      group: state.search.group,
      searchApiHost: state.configuration.searchApiHost
    };
  },
  dispatch => ({
    onHelp: () => dispatch(actions.setInfo(help)),
    onQueryStringChange: value => dispatch(actions.setQueryString(value)),
    onSearch: (searchParams, group, searchApiHost) => dispatch(actions.doSearch(searchParams, group, searchApiHost))
  })
)(SeachPanelComponent);

export const SearchPanel = withFloatingScrollEventsSubscription(
  "top",
  [
    { querySelector: "header.site-navigation" }
  ]
)(SearchPanelContainer);
