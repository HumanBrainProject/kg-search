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
import { connect } from "react-redux";
import * as actions from "../../../actions/actions";
import * as actionsSearch from "../../../actions/actions.search";
import { help } from "../../../data/help.js";
import { withFloatingScrollEventsSubscription } from "../../../helpers/withFloatingScrollEventsSubscription";
import "./SearchPanel.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

class SeachPanelBaseComponent extends React.Component {
  constructor(props){
    super(props);
    this.textInput = React.createRef();
    this.state = {
      value: ""
    };
  }

  componentDidMount() {
    window.addEventListener("scroll", this.handleScrollEvent);
    const { location } = this.props;
    const q = location.query["q"];
    if(q && q.length) {
      const queryString = decodeURIComponent(q);
      this.setState({value: queryString});
    }
    this.textInput.current.focus();
  }

  handleMouseDownEvent = () => this.ref && this.ref.focus();

  handleScrollEvent = () => this.ref && this.ref.blur();

  handleChange = e => {
    this.textInput.current.focus();
    this.setState({value: e.target.value});
  };

  handleSearch = () => this.props.onQueryStringChange(this.state.value);

  handleKeyDown = e => {
    if(e.key === "Enter") {
      this.props.onQueryStringChange(this.state.value);
    }
  };

  render() {
    const { isFloating, onHelp } = this.props;

    return (
      <div className={`kgs-search-panel ${isFloating ? " is-fixed-position" : ""}`}>
        <div>
          <div>
            <FontAwesomeIcon icon="search" size="2x" className="kg-search-bar__icon" />
            <input className="kg-search-bar"
              type="text"
              placeholder="Search (e.g. brain or neuroscience)"
              aria-label="Search"
              value={this.state.value}
              onChange={this.handleChange}
              onKeyDown={this.handleKeyDown}
              ref={this.textInput} />
            <button type="button" className="kgs-search-panel-help__button" title="Help" onClick={onHelp}>
              <FontAwesomeIcon icon="info-circle" size="2x" />
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
    { querySelector: "nav.kgs-navbar" },
    { querySelector: ".kgs-notification" }
  ]
)(SearchPanelContainer);
