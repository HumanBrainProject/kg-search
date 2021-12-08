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
import { Treebeard, decorators } from "react-treebeard";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

import Download from "./Download";
import File from "./File";

import "./HierarchicalFiles.css";
import theme from "./Theme";
import Header from "./Header";

import { getTreeByFolder } from "./FileTreeByFolderHelper";
import { getTreeByGroupingType } from "./FileTreeByGroupingTypeHelper";
import * as filters from "./helpers";


const Node = ({node, isRootNode, group}) => {
  return (
    <div className="kgs-hierarchical-files__details">
      <div>{node.thumbnail ? <img height="80" src={node.thumbnail} alt={node.url} />:<FontAwesomeIcon icon={node.type} size="5x"/>}</div>
      <div className="kgs-hierarchical-files__info">
        <div>
          <div><strong>Name:</strong> {node.name}</div>
          {node.size  && <div><strong>Size:</strong> {node.size}</div>}
          {node.url && (
            <Download name={`Download ${isRootNode?"dataset":node.type}`} type={node.type} url={node.url} />
          )}
          {node.type === "file" && node.details && node.details.data && node.details.mapping && (
            <File data={node.details.data} mapping={node.details.mapping} group={group} />
          )}
        </div>
      </div>
    </div>
  );
};

class HierarchicalFiles extends React.Component {
  constructor(props){
    super(props);
    this.state = {
      node: {},
      tree: {},
      initialTree: {}
    };
  }

  componentDidMount() {
    const {data, groupingType, nameField, urlField, fileMapping, allowFolderDownload} = this.props;
    const tree = groupingType?getTreeByGroupingType(data, nameField, urlField, fileMapping, groupingType):getTreeByFolder(data, urlField, fileMapping, allowFolderDownload);
    this.setState({tree: tree, node: tree, initialTree: tree });
  }

  onToggle = (node, toggled) => {
    node.active = true;
    if (node.children) {
      node.toggled = toggled;
    }
    if(node.url !== this.state.node.url) {
      const previousNode = this.state.node;
      previousNode.active = false;
    }
    this.setState({node: node});
  }

  onFilterMouseUp = ({target: {value}}) => {
    const filter = value.trim();
    if (!filter) {
      this.setState({tree: this.state.initialTree});
    }
    let filtered = filters.filterTree(this.state.initialTree, filter);
    filtered = filters.expandFilteredNodes(filtered, filter);
    this.setState({tree: filtered});
  }

  render() {
    const filesLength = this.props.data && this.props.data.length;
    return (
      <>
        {filesLength < 5000 && (
          <div className="kgs-files-search">
            <FontAwesomeIcon icon="search" className="kgs-files-search-icon" />
            <input
              className="form-control"
              onKeyUp={this.onFilterMouseUp}
              placeholder="Search the files..."
              type="text"
            />
          </div>)}
        <div className="kgs-hierarchical-files">
          <Treebeard
            data={this.state.tree}
            onToggle={this.onToggle}
            decorators={{...decorators, Header}}
            style={{...theme}}
          />
          {this.state.node.active && (
            <Node node={this.state.node} isRootNode={this.state.node === this.state.tree} group={this.props.group} />
          )}
        </div>
      </>
    );
  }
}

export default HierarchicalFiles;
