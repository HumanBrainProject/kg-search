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

import { getUpdatedUrl } from "../../helpers/BrowserHelpers";
import { history } from "../../store";
import { windowWidth } from "../../helpers/BrowserHelpers";
import * as actionsSearch from "../../actions/actions.search";
import "./Pagination.css";

class PageLinkButton extends React.PureComponent {

  onClick = () => {
    const { page: { value },  onClick} = this.props;
    onClick(value);
  }

  render() {
    const { page: { name, title, active, readOnly}} = this.props;
    const previous = name === "previous";
    const next = name === "next";
    const ellipsis = name === "ellipsis";
    return (
      <button className={`kgs-page-link ${previous?" is-previous":""}${next?" is-next":""}${ellipsis?" is-ellipsis":""}${active?" is-active":""}`} onClick={this.onClick} disabled={readOnly}>{title}</button>
    );
  }
}

class PaginationComponent extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      pageScope: this.pageScope
    };
    this.timer = null;
    this.handleResizeEvent = this.handleResizeEvent.bind(this);
  }

  componentDidMount() {
    window.addEventListener("resize", this.handleResizeEvent, false);
  }

  componentDidUpdate(prevProps) {
    const { page, location } = this.props;
    if (page !== prevProps.page) {
      const url = getUpdatedUrl("p", true, page, false, location);
      history.push(url);
    }
  }

  componentWillUnmount() {
    window.removeEventListener("resize", this.handleResizeEvent);
  }

  handleResizeEvent() {
    clearTimeout(this.timer);
    this.timer = setTimeout(() => this.updatePageScope(), 250);
  }

  get pageScope() {
    const width = windowWidth();
    return (width >= 1400)?3:(width >= 1200)?2:(width >= 1050)?1:(width >= 992)?0:(width >= 750)?3:(width >= 650)?2:(width >= 460)?1:0;
  }

  updatePageScope() {
    this.setState({pageScope: this.pageScope});
  }

  render() {
    const { show, page, totalPages, onClick } = this.props;

    if (!show) {
      return null;
    }

    const pages = [];
    const pageScope = this.state.pageScope;
    const hasPrevious = page > 1;
    const hasNext = page < totalPages;
    const hasFirst = hasPrevious && (page - pageScope > 1);
    const hasFirstEllipsis = hasPrevious && (page - pageScope > 2);
    const hasLastEllipsis = hasNext && (page + pageScope < totalPages);

    pages.push({
      name: "previous",
      title: null,
      value:  page -1,
      active: false,
      readOnly: !hasPrevious
    });

    if (hasFirst) {
      pages.push({
        name: "page",
        title: "1",
        value:  0,
        active: false,
        readOnly: false
      });
    }

    if (hasFirstEllipsis) {
      pages.push({
        name: "ellipsis",
        title: "...",
        value:  null,
        active: false,
        readOnly: true
      });
    }

    for (let p = page - pageScope; p <= page + pageScope; p++) {
      if (p > 0 && p < totalPages) {
        pages.push({
          name: "page",
          title: p,
          value:  p,
          active: p === page,
          readOnly: false
        });
      }
    }

    if (hasLastEllipsis) {
      pages.push({
        name: "ellipsis",
        title: "...",
        value:  null,
        active: false,
        readOnly: true
      });
    }

    pages.push({
      name: "next",
      title: null,
      value:  page + 1,
      active: false,
      readOnly: !hasNext
    });

    return (
      <div className="kgs-paging">
        {pages.map((p, index) => (
          <PageLinkButton key={index} page={p} onClick={onClick} />
        ))}
      </div>
    );
  }
}

export const Pagination = connect(
  state => ({
    show: state.search.totalPages > 0,
    totalPages: state.search.totalPages,
    page: state.search.page,
    location: state.router.location
  }),
  dispatch => ({
    onClick: value => dispatch(actionsSearch.setPage(value))
  })
)(PaginationComponent);

