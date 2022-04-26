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
import {faSearch} from "@fortawesome/free-solid-svg-icons/faSearch";
import {faFile} from "@fortawesome/free-solid-svg-icons/faFile";
import {faFolder} from "@fortawesome/free-solid-svg-icons/faFolder";

import Download from "./Download";
import LinkedInstance from "../../../containers/LinkedInstance";
import AsyncLinkedInstance from "../../../containers/AsyncLinkedInstance";

import "./HierarchicalFiles.css";
import theme from "./Theme";
import Header from "./Header";

import { getTreeByFolder } from "./FileTreeByFolderHelper";
import { getTreeByGroupingType, JSONPath } from "./FileTreeByGroupingTypeHelper";
import * as filters from "./helpers";
import { debounce } from "lodash";

const getFilteredTree = (tree, filter) => {
  if (!filter) {
    return tree;
  }
  let filtered = filters.filterTree(tree, filter);
  filtered = filters.expandFilteredNodes(filtered, filter);
  return filtered;
};

const Node = ({node, isRootNode, group, type, hasFilter}) => {
  const isFile = node.type === "file";
  const icon = isFile?faFile:faFolder;
  return (
    <div className="kgs-hierarchical-files__details">
      <div>{node.thumbnail ? <img height="80" src={node.thumbnail} alt={node.url} />:<FontAwesomeIcon icon={icon} size="5x"/>}</div>
      <div className="kgs-hierarchical-files__info">
        <div>
          <div><strong>Name:</strong> {node.name}</div>
          {node.url && (isFile || !hasFilter) && (
            <Download name={`Download ${isRootNode?(typeof type === "string"?type.toLowerCase():"Dataset"):node.type}`} type={type} url={node.url} />
          )}
          {node.type === "file" && (
            <LinkedInstance data={node.data} group={group} type={node.data?.type?.value || "File"} />
          )}
          {node.type === "fileBundle" && node.reference && (
            <AsyncLinkedInstance id={node.reference} name={node.name} group={group} type="FileBundle" />
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
      initialTree: {},
      filter: ""
    };
  }

  setTree() {
    const {data, groupingType, hasDataFilter, nameFieldPath, urlFieldPath} = this.props;
    if (data && data.length === 1 && !hasDataFilter) {
      const node = {
        data: data[0],
        name: JSONPath(data[0], nameFieldPath),
        url: JSONPath(data[0], urlFieldPath),
        type: "file"
      };
      this.setState({tree: {}, node: node, initialTree: {} });
    } else {
      const initialTree = groupingType?getTreeByGroupingType(data, nameFieldPath, urlFieldPath, groupingType):getTreeByFolder(data, nameFieldPath, urlFieldPath);
      const tree = getFilteredTree(initialTree, this.state.filter);
      this.setState({tree: tree,  node: tree, initialTree: initialTree });
    }
  }

  componentDidMount() {
    this.setTree();
  }

  componentDidUpdate(previousProps) {
    const { data, groupingType, nameFieldPath, urlFieldPath } = this.props;
    if (data !== previousProps.data
      || groupingType !== previousProps.groupingType
      || nameFieldPath !== previousProps.nameFieldPath
      || urlFieldPath !== previousProps.urlFieldPath) {
      this.setTree();
    }
  }

  onToggle = (node, toggled) => {
    node.active = true;
    if (node.children) {
      node.toggled = toggled;
    }
    if(node.url !== this.state.node.url || node.reference !== this.state.node.reference) {
      const previousNode = this.state.node;
      previousNode.active = false;
    }
    this.setState({node: node});
  };

  onFilterMouseUp = debounce(({target: {value}}) => {
    const filter = value.trim();
    this.setState({filter: filter});
    const tree = getFilteredTree(this.state.initialTree, filter);
    this.setState({tree: tree});
  }, 500);

  render() {
    const filesLength = this.props.data && this.props.data.length;
    const hasFilter = this.props.hasDataFilter || this.state.filter !== "";

    if (filesLength === 1 && !hasFilter) {
      return (
        <div className="kgs-single-file">
          <Node node={this.state.node} isRootNode={false} group={this.props.group} type={this.props.type} hasFilter={false} />
        </div>
      );
    }

    return (
      <>
        {filesLength < 5000 && (
          <div className="kgs-files-search">
            <FontAwesomeIcon icon={faSearch} className="kgs-files-search-icon" />
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
            <Node node={this.state.node} isRootNode={this.state.node.isRootNode} group={this.props.group} type={this.props.type} hasFilter={hasFilter} />
          )}
        </div>
      </>
    );
  }
}

export default HierarchicalFiles;
