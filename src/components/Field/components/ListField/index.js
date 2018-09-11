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

import React, { PureComponent } from "react";
import { ObjectField } from "../ObjectField";
import { ValueField } from "../ValueField";
import "./styles.css";

const DefaultList = ({children}) => {
  return (
    <ul>
      {children}
    </ul>
  );
};

const CustomList = ({className, children}) => (
  <span className={className}>
    {children}
  </span>
);

const DefaultListItem = ({children}) => (
  <li>
    {children}
  </li>
);

const CustomListItem = ({index, separator, children}) => (
  <span>
    {index?separator:null}
    {children}
  </span>
);

const ListFieldComponent = ({list, separator, showAsTag, showToggle, toggleHandler, toggleLabel, hasMore}) => {
  const isCustom = separator || showAsTag;
  const List = isCustom?CustomList:DefaultList;
  const ListItem = isCustom?CustomListItem:DefaultListItem;
  const className =  `kgs-field__list ${showAsTag?"items-as-tags":""}`;
  return (
    <span>
      <List className={className}>
        {
          list.map(({isObject, key, data, mapping, renderUserInteractions}, index) => (
            <ListItem key={key} separator={separator} index={index}>
              {isObject?
                <ObjectField show={true} data={data} mapping={mapping} renderUserInteractions={renderUserInteractions} />
                :
                <ValueField show={true} data={data} mapping={mapping} renderUserInteractions={renderUserInteractions} />
              }
            </ListItem>
          ))
        }
        {showToggle && hasMore && (
          <ListItem key={-1} className="kgs-field__more">...</ListItem>
        )}
      </List>
      {showToggle && (
        <button className="kgs-field__viewMore-button" onClick={toggleHandler} role="link">{toggleLabel}</button>
      )}
    </span>
  );
};

const LIST_SMALL_SIZE_STOP = 5;
const LIST_MIDDLE_SIZE_STOP = 10;

const VIEW_MORE_LABEL = "view more";
const VIEW_LESS_LABEL = "view less";
const VIEW_ALL_LABEL = "view all";

export class ListField extends PureComponent {
  constructor(props) {
    super(props);
    const sizeStop = this.getNextSizeStop(Number.POSITIVE_INFINITY);
    this.state = {
      sizeStop: sizeStop,
      items: this.getFilteredItems(sizeStop),
      hasShowMoreToggle: this.hasShowMoreToggle,
      showMoreLabel: this.getShowMoreLabel(sizeStop),
      hasMore: this.hasMore(sizeStop)
    };
    this.handleShowMoreClick = this.handleShowMoreClick.bind(this);
  }

  get maxSizeStop() {
    const {items, mapping, renderUserInteractions} = this.props;

    if (!Array.isArray(items)) {
      return 0;
    }

    if (!renderUserInteractions && mapping && mapping.overview_max_display && mapping.overview_max_display < items.length) {
      return mapping.overview_max_display;
    }
    return items.length;
  }

  getNextSizeStop(sizeStop) {
    const {items, mapping} = this.props;

    if (!Array.isArray(items) || (mapping && mapping.separator)) {
      return Number.POSITIVE_INFINITY;
    }

    if (sizeStop === LIST_SMALL_SIZE_STOP) {
      return (items.length > LIST_MIDDLE_SIZE_STOP)?LIST_MIDDLE_SIZE_STOP:Number.POSITIVE_INFINITY;
    }

    if (sizeStop === LIST_MIDDLE_SIZE_STOP) {
      return (items && items.length > LIST_MIDDLE_SIZE_STOP)?Number.POSITIVE_INFINITY:LIST_SMALL_SIZE_STOP;
    }

    return LIST_SMALL_SIZE_STOP;
  }

  getFilteredItems(sizeStop) {
    const {items, mapping, renderUserInteractions} = this.props;

    if (!Array.isArray(items)) {
      return [];
    }

    const nbToDisplay = Math.min(this.maxSizeStop, sizeStop);
    return items
      .filter((item, index) => {
        return index < nbToDisplay;
      })
      .map((item, index) => ({
        isObject: !!item.children,
        key: item.reference?item.reference:item.value?item.value:index,
        show: true,
        data: item.children?item.children:item,
        mapping: mapping,
        renderUserInteractions: renderUserInteractions
      }));
  }

  getShowMoreLabel(sizeStop) {
    const {items, mapping} = this.props;
    if (!Array.isArray(items) || (mapping && mapping.separator)) {
      return null;
    }

    if (sizeStop === LIST_SMALL_SIZE_STOP) {
      return (this.maxSizeStop > LIST_MIDDLE_SIZE_STOP)?VIEW_MORE_LABEL:VIEW_ALL_LABEL;
    }

    if (sizeStop === LIST_MIDDLE_SIZE_STOP) {
      return (this.maxSizeStop > LIST_MIDDLE_SIZE_STOP)?VIEW_ALL_LABEL:VIEW_LESS_LABEL;
    }

    return VIEW_LESS_LABEL;
  }

  get hasShowMoreToggle() {
    const {items, mapping, renderUserInteractions} = this.props;
    if (!Array.isArray(items) || (mapping && mapping.separator) || !renderUserInteractions) {
      return false;
    }

    return this.maxSizeStop > LIST_SMALL_SIZE_STOP;
  }

  hasMore(sizeStop) {
    const {items, mapping} = this.props;
    if (!Array.isArray(items) || (mapping && mapping.separator)) {
      return false;
    }
    const maxSizeStop = this.maxSizeStop;
    const nbToDisplay = Math.min(maxSizeStop, sizeStop);

    return maxSizeStop > nbToDisplay;
  }

  handleShowMoreClick() {
    const nextSizeStop = this.getNextSizeStop(this.state.sizeStop);
    this.setState({
      sizeStop: nextSizeStop,
      items: this.getFilteredItems(nextSizeStop),
      hasShowMoreToggle: this.hasShowMoreToggle,
      showMoreLabel: this.getShowMoreLabel(nextSizeStop),
      hasMore: this.hasMore(nextSizeStop)
    });
  }

  render() {
    const {show, mapping} = this.props;
    if (!show) {
      return null;
    }

    return (
      <ListFieldComponent list={this.state.items} separator={mapping && !mapping.tag_icon && mapping.separator} showAsTag={mapping && !!mapping.tag_icon} showToggle={this.state.hasShowMoreToggle} toggleHandler={this.handleShowMoreClick} toggleLabel={this.state.showMoreLabel} hasMore={this.state.hasMore}/>
    );
  }
}