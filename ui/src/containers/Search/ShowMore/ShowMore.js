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

import * as actionsSearch from "../../../actions/actions.search";
import "./ShowMore.css";

const ShowMoreComponent = ({ next, totalPages, onClick }) => {

  if (next > totalPages) {
    return null;
  }

  return (
    <div className="kgs-show-more">
      <button type="button" className="kgs-show-more-btn" onClick={() => onClick(next)}>Show more results</button>
    </div>
  );
};

export const ShowMore = connect(
  state => ({
    totalPages: state.search.totalPages,
    next: state.search.page + 1
  }),
  dispatch => ({
    onClick: value => dispatch(actionsSearch.setPage(value))
  })
)(ShowMoreComponent);

