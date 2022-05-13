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
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faSearch } from "@fortawesome/free-solid-svg-icons/faSearch";
import { faFile } from "@fortawesome/free-solid-svg-icons/faFile";
import { faFolder } from "@fortawesome/free-solid-svg-icons/faFolder";

import Download from "./Download";
import LinkedInstance from "../../../containers/LinkedInstance";
import AsyncLinkedInstance from "../../../containers/AsyncLinkedInstance";

import "./HierarchicalFiles.css";
import "rc-tree/assets/index.css";

import { getTreeByFolder } from "./FileTreeByFolderHelper";
import {
  getTreeByGroupingType,
  JSONPath
} from "./FileTreeByGroupingTypeHelper";
import * as filters from "./helpers";
import { debounce } from "lodash";

import Tree from "rc-tree";
// import { faMinus, faPlus } from "@fortawesome/free-solid-svg-icons";

const getFilteredTree = (tree, filter) => {
  if (!filter) {
    return tree;
  }
  let filtered = filters.filterTree(tree, filter);
  filtered = filters.expandFilteredNodes(filtered, filter);
  return filtered;
};

const Node = ({ node, isRootNode, group, type, hasFilter }) => {
  const isFile = node.type === "file";
  const icon = isFile ? faFile : faFolder;

  const getDownloadName = () => {
    if (isRootNode) {
      return `Download ${
        typeof type === "string" ? type.toLowerCase() : "Dataset"
      }`;
    }
    return `Download ${node.type}`;
  };

  return (
    <div className="kgs-hierarchical-files__details">
      <div>
        {node.thumbnail ? (
          <img height="80" src={node.thumbnail} alt={node.url} />
        ) : (
          <FontAwesomeIcon icon={icon} size="5x" />
        )}
      </div>
      <div className="kgs-hierarchical-files__info">
        <div>
          <div>
            <strong>Name:</strong> {node.title}
          </div>
          {node.url && (isFile || !hasFilter) && (
            <Download name={getDownloadName()} type={type} url={node.url} />
          )}
          {node.type === "file" && (
            <LinkedInstance
              data={node.data}
              group={group}
              type={node.data?.type?.value || "File"}
            />
          )}
          {node.type === "fileBundle" && node.reference && (
            <AsyncLinkedInstance
              id={node.reference}
              name={node.name}
              group={group}
              type="FileBundle"
            />
          )}
        </div>
      </div>
    </div>
  );
};

const addExpandedKeys = (tree, keys) => {
  if (tree.expanded) {
    keys.push(tree.key);
  }
  if (tree.children) {
    tree.children.forEach(child => addExpandedKeys(child, keys));
  }
};

const Icon = ({ type }) => {
  const isFile = type === "file";
  const icon = isFile ? faFile : faFolder;
  return <FontAwesomeIcon icon={icon} />;
};

// const SwitcherIcon = obj => {
//   console.log(obj);
//   if (!obj.isLeaf) {
//     if(obj.expanded) {
//       return <FontAwesomeIcon icon={faMinus} />;
//     }
//     return <FontAwesomeIcon icon={faPlus} />;
//   }
//   return null;
// };

class HierarchicalFiles extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      node: {},
      tree: {
        children: []
      },
      initialTree: {
        children: []
      },
      filter: "",
      expandedKeys: []
    };
  }

  setTree() {
    const { data, groupingType, hasDataFilter, nameFieldPath, urlFieldPath } =
      this.props;
    if (data && data.length === 1 && !hasDataFilter) {
      const node = {
        data: data[0],
        name: JSONPath(data[0], nameFieldPath),
        url: JSONPath(data[0], urlFieldPath),
        type: "file"
      };
      this.setState({ tree: {}, node: node, initialTree: {} });
    } else {
      const initialTree = groupingType
        ? getTreeByGroupingType(data, nameFieldPath, urlFieldPath, groupingType)
        : getTreeByFolder(data, nameFieldPath, urlFieldPath);
      const tree = getFilteredTree(initialTree, this.state.filter);
      this.setState({
        tree: tree,
        node: tree,
        initialTree: initialTree,
        expandedKeys: [tree.key]
      });
    }
  }

  componentDidMount() {
    this.setTree();
  }

  componentDidUpdate(previousProps) {
    const { data, groupingType, nameFieldPath, urlFieldPath } = this.props;
    if (
      data !== previousProps.data ||
      groupingType !== previousProps.groupingType ||
      nameFieldPath !== previousProps.nameFieldPath ||
      urlFieldPath !== previousProps.urlFieldPath
    ) {
      this.setTree();
    }
  }

  onFilterMouseUp = debounce(({ target: { value } }) => {
    const filter = value.trim();
    this.setState({ filter: filter });
    const tree = getFilteredTree(this.state.initialTree, filter);
    const expandedKeys = [tree.key];
    addExpandedKeys(tree, expandedKeys);
    this.setState({ tree: tree, expandedKeys: expandedKeys });
  }, 500);

  onSelect = (_selectedKeys, info) => {
    info.node.active = true;
    this.setState({ node: info.node });
  };

  onExpand = expandedKeys => this.setState({ expandedKeys: expandedKeys });

  render() {
    const filesLength = this.props.data && this.props.data.length;
    const hasFilter = this.props.hasDataFilter || this.state.filter !== "";

    if (filesLength === 1 && !hasFilter) {
      return (
        <div className="kgs-single-file">
          <Node
            node={this.state.node}
            isRootNode={false}
            group={this.props.group}
            type={this.props.type}
            hasFilter={false}
          />
        </div>
      );
    }

    if (this.state.tree.children.length === 0) {
      return null;
    }

    return (
      <>
        {filesLength < 5000 && (
          <div className="kgs-files-search">
            <FontAwesomeIcon
              icon={faSearch}
              className="kgs-files-search-icon"
            />
            <input
              className="form-control"
              onKeyUp={this.onFilterMouseUp}
              placeholder="Search the files..."
              type="text"
            />
          </div>
        )}
        <div className="kgs-hierarchical-files">
          <Tree
            treeData={[this.state.tree]}
            defaultSelectedKeys={[this.state.tree.key]}
            expandedKeys={this.state.expandedKeys}
            onSelect={this.onSelect}
            onExpand={this.onExpand}
            icon={Icon}
            // switcherIcon={SwitcherIcon}
          />
          {this.state.node.active && (
            <Node
              node={this.state.node}
              isRootNode={this.state.node.isRootNode}
              group={this.props.group}
              type={this.props.type}
              hasFilter={hasFilter}
            />
          )}
        </div>
      </>
    );
  }
}

export default HierarchicalFiles;
