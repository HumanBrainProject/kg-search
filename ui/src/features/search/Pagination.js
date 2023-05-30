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

import {faChevronLeft} from '@fortawesome/free-solid-svg-icons/faChevronLeft';
import {faChevronRight} from '@fortawesome/free-solid-svg-icons/faChevronRight';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import React from 'react';
import { connect } from 'react-redux';

import { windowWidth } from '../../helpers/BrowserHelpers';
import { setPage } from './searchSlice';

import './Pagination.css';

const getAriaLabel = (isPrevious, isNext, title) => {
  if (isPrevious) {
    return 'previous';
  }
  if (isNext) {
    return 'next';
  }
  return title;
};

class PageLinkButton extends React.PureComponent {

  onClick = () => {
    const { page: { value },  onClick} = this.props;
    onClick(value);
  };

  render() {
    const { page: { name, title, active, readOnly}} = this.props;
    const isPrevious = name === 'previous';
    const isNext = name === 'next';
    const ellipsis = name === 'ellipsis';
    const ariaLabel = getAriaLabel(isPrevious, isNext, title);
    return (
      <button className={`kgs-page-link ${ellipsis?' is-ellipsis':''}${active?' is-active':''}`} onClick={this.onClick} disabled={readOnly} title={ariaLabel}>
        {isPrevious && <FontAwesomeIcon icon={faChevronLeft} className="is-previous" />}
        {!isPrevious && !isNext && title}
        {isNext && <FontAwesomeIcon icon={faChevronRight} className="is-next" />}
      </button>
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
    window.addEventListener('resize', this.handleResizeEvent, false);
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.handleResizeEvent);
  }

  handleResizeEvent() {
    clearTimeout(this.timer);
    this.timer = setTimeout(() => this.updatePageScope(), 250);
  }

  get pageScope() {
    const width = windowWidth();
    if(width >= 1400) {
      return 3;
    }
    if(width >= 1200) {
      return 2;
    }
    if(width >= 1050) {
      return 1;
    }
    if(width >= 992) {
      return 0;
    }
    if(width >= 750) {
      return 3;
    }
    if(width >= 650) {
      return 2;
    }
    if(width >= 460) {
      return 1;
    }
    return 0;
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
      name: 'previous',
      title: null,
      value:  page -1,
      active: false,
      readOnly: !hasPrevious
    });

    if (hasFirst) {
      pages.push({
        name: 'page',
        title: 1,
        value:  1,
        active: false,
        readOnly: false
      });
    }

    if (hasFirstEllipsis) {
      pages.push({
        name: 'ellipsis',
        title: '...',
        value:  null,
        active: false,
        readOnly: true
      });
    }

    for (let p = page - pageScope; p <= page + pageScope; p++) {
      if (p > 0 && p < totalPages) {
        pages.push({
          name: 'page',
          title: p,
          value:  p,
          active: p === page,
          readOnly: false
        });
      }
    }

    if (hasLastEllipsis) {
      pages.push({
        name: 'ellipsis',
        title: '...',
        value:  null,
        active: false,
        readOnly: true
      });
    }

    pages.push({
      name: 'page',
      title: totalPages,
      value:  totalPages,
      active: page === totalPages,
      readOnly: false
    });

    pages.push({
      name: 'next',
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

const Pagination = connect(
  state => ({
    show: state.search.totalPages > 0,
    totalPages: state.search.totalPages,
    page: state.search.page
  }),
  dispatch => ({
    onClick: value => {
      dispatch(setPage(value));
      window.scrollTo(0, 0); // Scroll page to top after paginating
    }
  })
)(PaginationComponent);

export default Pagination;