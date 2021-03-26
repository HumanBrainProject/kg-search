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

import "./ChevronStackPagination.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

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
      <button className={`kgs-chevron-stack-page-link ${ellipsis?" is-ellipsis":""}${active?" is-active":""}`} onClick={this.onClick} disabled={readOnly}>
        {previous && <FontAwesomeIcon icon="chevron-left" className="is-previous" />}
        {!previous && !next && title}
        {next && <FontAwesomeIcon icon="chevron-right" className="is-next" />}
      </button>
    );
  }
}

export const ChevronStackPagination  = ({ page, totalPages, onClick }) => {

  const pages = [];
  const pageScope = 2;
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
    if (p > 0 && p <= totalPages) {
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
    <div className="kgs-chevron-stack-paging">
      {pages.map((p, index) => (
        <PageLinkButton key={index} page={p} onClick={onClick} />
      ))}
    </div>
  );
};

export default ChevronStackPagination;