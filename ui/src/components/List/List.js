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

import "./List.css";
class Item extends React.PureComponent {

  handleClick = e => {
    const { item, onClick } = this.props;
    if (typeof onClick === "function") {
      e.stopPropagation();
      onClick(item);
    }
  }

  handleKeyDown = e => {
    const { item, onClick } = this.props;
    if (e.keyCode === 13 && typeof onClick === "function") {
      e.stopPropagation();
      onClick(item);
    }
  }

  setFocus = () => {
    const { hasFocus } = this.props;
    if (hasFocus) {
      this.ref.focus();
    }
  }

  componentDidMount() {
    this.setFocus();
  }

  componentDidUpdate() {
    this.setFocus();
  }

  render() {
    const { ItemComponent, item } = this.props;
    return (
      <div className="kgs-list-item" tabIndex="0" ref={ref => this.ref = ref} onClick={this.handleClick} onKeyDown={this.handleKeyDown} >
        <ItemComponent item={item} />
      </div>
    );
  }
}

export const List = ({ items, currentItem, ItemComponent, itemUniqKeyAttribute, onItemClick }) => {
  if (!Array.isArray(items) || !items.length) {
    return null;
  }
  return (
    <div className="kgs-list" >
      {items.map(item => <Item key={item[itemUniqKeyAttribute]} ItemComponent={ItemComponent} item={item} hasFocus={item === currentItem} onClick={onItemClick} />)}
    </div>
  );
};

export default List;