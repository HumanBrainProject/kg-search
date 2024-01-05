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

import { faCircle } from '@fortawesome/free-solid-svg-icons/faCircle';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import _ from 'lodash-uuid';
import React, { Suspense, useState } from 'react';
import { useSelector } from 'react-redux';

import Matomo from '../../../services/Matomo';

import LinkedInstance from '../../Instance/LinkedInstance';

import './HierarchicalTree.css';

const Tree = React.lazy(() => import('./Tree.jsx'));

const Icon = ({ color }) => (
  <FontAwesomeIcon icon={faCircle} style={{ color: color ? color : 'gray' }} />
);

const Node = ({ node }) => (
  <div className="kgs-hierarchical-tree__details">
    <div className="kgs-hierarchical-tree__info">
      {node && (
        <div>
          <div className="kgs-hierarchical-tree__info-title">
            <Icon color={node.color} />
              &nbsp;{node.title}
          </div>
          {node.data && (
            <LinkedInstance
              data={node.data}
              type={node.data.type?.value}
            />
          )}
        </div>
      )}
    </div>
  </div>
);

const Loading = () => (
  <>
    <div className="spinner-border spinner-border-sm" role="status" />
      &nbsp;Loading hierarchy...
  </>
);

const HierarchicalTreeComponent = ({ tree, defaultExpandAll, defaultExpandedKeys, initialSelectedNode }) => {

  const [node, setNode] = useState(initialSelectedNode);

  const onSelect = (_selectedKeys, info) => {
    Matomo.trackEvent('Specimen', 'Clicked', info.node.title);
    setNode(info.node);
  };

  return (
    <div className="kgs-hierarchical-tree">
      <i className="kgs-hierarchical-tree__advise">
        Select the items of the tree to get more details about the individual elements.
      </i>
      <div className="kgs-hierarchical-tree__body">
        <div className="kgs-hierarchical-tree__content">
          <Suspense fallback={<Loading />}>
            <Tree data={tree} onSelect={onSelect} defaultExpandAll={defaultExpandAll} defaultExpandedKeys={defaultExpandedKeys} defaultSelectedKey={node.key} />
          </Suspense>
        </div>
        <Node node={node} />
      </div>
    </div>
  );
};

const HierarchicalTree = ({ data }) => {

  const targetId = useSelector(state => state.instance.context?.targetId);

  const { data: tree, expandAll, expandedKeys, selectedNode } = splitChildren(data, targetId);

  return (
    <HierarchicalTreeComponent tree={tree} defaultExpandAll={expandAll} defaultExpandedKeys={expandedKeys} initialSelectedNode={selectedNode || tree} />
  );
};

const regTitle = /^(.+\s)\d+$/;
const getTo = (from , to) => {
  let next = true;
  let until = 0;
  for (let i=0; next && i<from.length && i<to.length; i++) {
    next = from.charAt(i) === to.charAt(i);
    until = i;
  }
  const common = from.slice(0, until);
  if (regTitle.test(common)) {
    const [ , start] = common.match(regTitle);
    return to.slice(start.length);
  } else {
    return to.slice(until);
  }
};

const splitNoChildren = (node, targetId) => {
  if (targetId && targetId === node?.data?.id) {
    return {
      data: node,
      expandAll: true,
      expandedKeys: [],
      selectedNode: node
    };
  }
  return {
    data: node,
    expandAll: true,
    expandedKeys: []
  };
};

const splitChildrenDefault = (node, targetId) => {
  const selectedNodeIsCurrentNode = targetId && targetId === node?.data?.id;
  const { children, expandAll, expandedKeys, selectedNode } = node.children.reduce((acc, c) => {
    const targetIdToFind = (selectedNodeIsCurrentNode || acc.selectedNode)?null:targetId;
    const { data, expandAll, expandedKeys, selectedNode } = splitChildren(c, targetIdToFind);
    acc.children.push(data);
    acc.expandAll = acc.expandAll && expandAll;
    expandedKeys.forEach(key => acc.expandedKeys.push(key));
    if (selectedNode) {
      acc.selectedNode = selectedNode;
    }
    return acc;
  }, {children: [], expandAll: false, expandedKeys: []});
  expandedKeys.push(node.key);
  const currentNode = {
    ...node,
    children: children
  };
  return {
    data: currentNode,
    expandAll: expandAll,
    expandedKeys: expandedKeys,
    selectedNode: selectedNodeIsCurrentNode?currentNode:selectedNode
  };
};

const splitChildrenForHugeList = (node, targetId) => {
  const children = [];
  const selectedNodeIsCurrentNode = targetId && targetId === node?.data?.id;
  let childSelectedNode = null;
  const keys = [];
  const childrenByType = Object.values(node.children.reduce((acc, child) => {
    if (!acc[child.color]) {
      acc[child.color] = [];
    }
    acc[child.color].push(child);
    return acc;
  }, {}));
  childrenByType.forEach(list => {
    if (list.length === 1) {
      const { expandedKeys, selectedNode } = splitChildren(list[0], (selectedNodeIsCurrentNode || childSelectedNode)?null:targetId);
      expandedKeys.forEach(key => keys.push(key));
      if (selectedNode) {
        childSelectedNode = selectedNode;
      }
      children.push(list[0]);
    } else {
      for (let i = 0; i <  list.length; i += chunkSize) {
        const chunk =  list.slice(i, i + chunkSize);
        const chunkKey = _.uuid();
        chunk.forEach(c => {
          const { expandedKeys, selectedNode } = splitChildren(c, (selectedNodeIsCurrentNode || childSelectedNode)?null:targetId);
          expandedKeys.forEach(key => keys.push(key));
          if (selectedNode) {
            childSelectedNode = selectedNode;
            keys.push(chunkKey);
          }
        });
        const from = chunk[0].title;
        const to = getTo(from, chunk[chunk.length-1].title);
        children.push({
          key: chunkKey,
          title: `from ${from} to ${to}`,
          color: chunk[0]?.color,
          children: chunk
        });
      }
    }
  });
  if (childSelectedNode || children.length < chunkSize) {
    keys.push(node.key);
  }
  const currentNode = {
    ...node,
    children: children
  };
  return {
    data: currentNode,
    expandAll: false,
    expandedKeys: keys,
    selectedNode: selectedNodeIsCurrentNode?currentNode:childSelectedNode
  };
};

const chunkSize = 100;
const chunkThresold = chunkSize * 5;
const splitChildren = (data, targetId) => {
  if (!Array.isArray(data.children)) {
    return splitNoChildren(data, targetId);
  }
  if (data.children.length < chunkThresold) {
    return splitChildrenDefault(data, targetId);
  }
  return splitChildrenForHugeList(data, targetId);
};

export default HierarchicalTree;