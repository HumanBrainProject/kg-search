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
import * as actions from "../../actions/actions";
import * as actionsSearch from "../../actions/actions.search";
import { help } from "../../data/help.js";
import { withFloatingScrollEventsSubscription } from "../../helpers/withFloatingScrollEventsSubscription";
import { isMobile } from "../../helpers/BrowserHelpers";
import "./SearchPanel.css";

class SeachPanelBaseComponent extends React.Component {
  constructor(props){
    super(props);
    this.state = {
      value: ""
    };
  }

  componentDidMount() {
    if (isMobile) {
      window.addEventListener("mousedown", this.handleMouseDownEvent, false);
    }
    window.addEventListener("scroll", this.handleScrollEvent);
    const { location } = this.props;
    const q = location.query["q"];
    if(q && q.length) {
      const queryString = decodeURIComponent(q);
      this.setState({value: queryString});
    }
  }

  componentWillUnmount() {
    if (isMobile) {
      window.removeEventListener("mousedown", this.handleMouseDownEvent);
    }
  }

  handleMouseDownEvent = () => this.ref && this.ref.focus();

  handleScrollEvent = () => this.ref && this.ref.blur();

  handleChange = e => this.setState({value: e.target.value});

  handleSearch = () => this.props.onQueryStringChange(this.state.value);

  handleKeyDown = e => {
    if(e.key === "Enter") {
      this.props.onQueryStringChange(this.state.value);
    }
  }

  render() {
    const { isFloating, onHelp } = this.props;

    return (
      <div className={`kgs-search-panel ${isFloating ? " is-fixed-position" : ""}`}>
        <div>
          <div>
            <i className="fa fa-search kg-search-bar__icon"></i>
            <input className="kg-search-bar"
              type="text"
              placeholder="Search (e.g. brain or neuroscience)"
              aria-label="Search"
              value={this.state.value}
              onChange={this.handleChange}
              onKeyDown={this.handleKeyDown}
              ref={ref => this.ref = ref} />
            <button type="button" className="kgs-search-panel-help__button" title="Help" onClick={onHelp}>
              <i className="fa fa-info-circle fa-2x"></i>
            </button>
          </div>
          <button className="kgs-search-panel-button" onClick={this.handleSearch}>Search</button>
        </div>
      </div>
    );
  }
}

const SeachPanelComponent = ({isFloating, relatedElements, onHelp, onQueryStringChange, location}) => (
  <SeachPanelBaseComponent isFloating={isFloating} relatedElements={relatedElements} onHelp={onHelp} onQueryStringChange={onQueryStringChange} location={location} />
);

const SearchPanelContainer = connect(
  (state, props) => {
    return {
      isFloating: props.isFloating,
      relatedElements: props.relatedElements,
      queryString: state.search.queryString,
      location: state.router.location
    };
  },
  dispatch => ({
    onHelp: () => dispatch(actions.setInfo(help)),
    onQueryStringChange: value => dispatch(actionsSearch.setQueryString(value))
  })
)(SeachPanelComponent);

export const SearchPanel = withFloatingScrollEventsSubscription(
  "top",
  [
    { querySelector: "header.site-navigation" }
  ]
)(SearchPanelContainer);
