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

import "./Tree.css";

const Node = props => {
  const { item } = props;
  if (item && Array.isArray(item.children) && item.children.length) {
    return (
      <CollapisbleNode {...props} />
    );
  }
  return (
    <NodeItem {...props} />
  );
};

const NodeList = ({ items, ItemComponent, itemUniqKeyAttribute, onItemClick, readOnly }) => (
  <div className="kgs-tree__nodesList">
    {items.map(item => (
      <Node key={item[itemUniqKeyAttribute]} item={item} ItemComponent={ItemComponent} itemUniqKeyAttribute={itemUniqKeyAttribute} onItemClick={onItemClick} readOnly={readOnly} />
    ))}
  </div>
);

class NodeItem extends React.PureComponent {

  handleClick = e => {
    const { item, onClick, readOnly } = this.props;
    if (!readOnly && typeof onClick === "function") {
      e.stopPropagation();
      onClick(item);
    }
  }

  handleKeyDown = e => {
    const { item, onClick, readOnly } = this.props;
    if (!readOnly && e.keyCode === 13 && typeof onClick === "function") {
      e.stopPropagation();
      onClick(item);
    }
  }

  render() {
    const { item, ItemComponent, readOnly } = this.props;

    const props = {};
    if (!readOnly) {
      props.tabIndex = "0";
      props.onKeyDown = this.handleKeyDown;
      props.onClick = this.handleClick;
    }

    return (
      <div className="kgs-tree-node" {...props} >
        <ItemComponent item={item} />
      </div>
    );
  }
}

class CollapisbleNode extends React.Component {

  constructor(props) {
    super(props);
    this.childrenRef = React.createRef();
    this.state = { isCollapsed: true };
  }

  handleCollapseToggle = () => this.setState(state => ({ isCollapsed: !state.isCollapsed }));

  render() {
    const { item, ItemComponent, itemUniqKeyAttribute, onClick, readOnly } = this.props;

    const maxHeight = (!this.state.isCollapsed && this.childrenRef && this.childrenRef.current) ? this.childrenRef.current.scrollHeight + "px" : null;

    return (
      <div className={`kgs-tree-collapsible-node ${this.state.isCollapsed ? "is-collapsed" : ""}`}>
        <div className="kgs-tree-collapsible-node__header">
          <button className="kgs-tree-collapsible-node__button" title={`${this.state.isCollapsed ? "expand" : "collapse"}`} tabIndex={readOnly ? -1 : 0} onClick={readOnly ? undefined : this.handleCollapseToggle} style={readOnly ? { pointerEvents: "none" } : {}}><i className="fa fa-chevron-down"></i></button>
          <NodeItem ItemComponent={ItemComponent} item={item} onClick={onClick} readOnly={readOnly} />
        </div>
        <div className="kgs-tree-collapsible-node__children" ref={this.childrenRef} style={{ maxHeight: maxHeight }}>
          <NodeList items={item.children} ItemComponent={ItemComponent} itemUniqKeyAttribute={itemUniqKeyAttribute} onItemClick={onClick} readOnly={readOnly || this.state.isCollapsed} />
        </div>
      </div>
    );
  }
}

export const Tree = ({ tree, ItemComponent, itemUniqKeyAttribute, onItemClick }) => (
  <div className="kgs-tree">
    {Array.isArray(tree) ?
      <NodeList items={tree} ItemComponent={ItemComponent} itemUniqKeyAttribute={itemUniqKeyAttribute} onItemClick={onItemClick} readOnly={false} />
      :
      <Node item={tree} ItemComponent={ItemComponent} itemUniqKeyAttribute={itemUniqKeyAttribute} onClick={onItemClick} readOnly={false}  />
    }
  </div>
);

export default Tree;