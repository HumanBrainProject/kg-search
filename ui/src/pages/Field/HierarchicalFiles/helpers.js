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


export const matcher = (filterText, node) =>  node.title.toLowerCase().indexOf(filterText.toLowerCase()) !== -1;

export const findNode = (node, filter) => matcher(filter, node) ||
        (node.children &&
            node.children.length &&
            !!node.children.find(child => findNode(child, filter)));

export const filterTree = (node, filter) => {
  if (matcher(filter, node) || !node.children) {
    return node;
  }
  const filtered = node.children.filter(child => findNode(child, filter)).map(child => filterTree(child, filter));
  return {...node, children: filtered};
};

export const expandFilteredNodes = (node, filter) => {
  let children = node.children;
  if (!children || children.length === 0) {
    return {...node, expanded: false};
  }
  const childrenWithMatches = node.children.filter(child => findNode(child, filter));
  const shouldExpand = childrenWithMatches.length > 0;
  if (shouldExpand) {
    children = childrenWithMatches.map(child => expandFilteredNodes(child, filter));
  }
  return {...node, children: children, expanded: shouldExpand};
};
