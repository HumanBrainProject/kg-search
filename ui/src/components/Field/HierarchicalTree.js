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

import React, { Suspense, useState } from "react";
import { connect } from "react-redux";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCircle } from "@fortawesome/free-solid-svg-icons/faCircle";
import API from "../../services/API";
import _ from "lodash-uuid";

import LinkedInstance from "../../containers/LinkedInstance";

import "./HierarchicalTree.css";
import "rc-tree/assets/index.css";

const HierarchicalTreeData = React.lazy(() =>
  import("./HierarchicalTreeData.js")
);

const Icon = ({ color }) => (
  <FontAwesomeIcon icon={faCircle} style={{ color: color ? color : "gray" }} />
);

const Node = ({ node, group }) => {
  return (
    <div className="kgs-hierarchical-details">
      <div className="kgs-hierarchical-info">
        {node && (
          <div>
            <div className="kgs-hierarchical-info-title">
              <Icon color={node.color} />
              &nbsp;{node.title}
            </div>
            {node.data && (
              <LinkedInstance
                data={node.data}
                group={group}
                type={node.data.type?.value}
              />
            )}
          </div>
        )}
      </div>
    </div>
  );
};

const Loading = () => {
  return (
    <>
      <div className="spinner-border spinner-border-sm" role="status"></div>
      &nbsp;Loading hierarchy...
    </>
  );
};

const HierarchicalTreeComponent = ({ data, group, defaultExpandAll, defaultExpandedKeys }) => {
  const [node, setNode] = useState(data);

  const onSelect = (_selectedKeys, info) => {
    API.trackEvent("Specimen", "Clicked", info.node.title);
    setNode(info.node);
  };

  return (
    <div>
      <i className="kgs-hierarchical-advise">
        Select the items of the tree to get more details about the individual
        elements.
      </i>
      <div className="kgs-hierarchical">
        <div className="kgs-hierarchical-tree">
          <Suspense fallback={<Loading />}>
            <HierarchicalTreeData data={data} onSelect={onSelect} defaultExpandAll={defaultExpandAll} defaultExpandedKeys={defaultExpandedKeys} />
          </Suspense>
        </div>
        <Node node={node} group={group} />
      </div>
    </div>
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

const chunkSize = 100;
const chunkThresold = chunkSize * 5;
const splitChildren = data => {
  if (!Array.isArray(data.children)) {
    return {
      data: data,
      expandAll: true,
      expandedKeys: []
    };
  }
  if (data.children.length < chunkThresold) {
    const { children, expandAll, expandedKeys } = data.children.reduce((acc, c) => {
      const { data, expandAll, expandedKeys } = splitChildren(c);
      acc.children.push(data);
      acc.expandAll = acc.expandAll && expandAll;
      expandedKeys.forEach(key => acc.expandedKeys.push(key));
      return acc;
    }, {children: [], expandAll: false, expandedKeys: []});
    expandedKeys.push(data.key);
    return {
      data: {
        ...data,
        children: children
      },
      expandAll: expandAll,
      expandedKeys: expandedKeys
    };
  } else {
    const children = [];
    const keys = [];
    const childrenByType = Object.values(data.children.reduce((acc, child) => {
      if (!acc[child.color]) {
        acc[child.color] = [];
      }
      acc[child.color].push(child);
      return acc;
    }, {}));
    childrenByType.forEach(list => {
      if (list.length === 1) {
        const { expandedKeys } = splitChildren(list[0]);
        expandedKeys.forEach(key => keys.push(key));
        children.push(list[0]);
      } else {
        for (let i = 0; i <  list.length; i += chunkSize) {
          const chunk =  list.slice(i, i + chunkSize);
          chunk.forEach(c => {
            const { expandedKeys } = splitChildren(c);
            expandedKeys.forEach(key => keys.push(key));
          });
          const from = chunk[0].title;
          const to = getTo(from, chunk[chunk.length-1].title);
          children.push({
            key: _.uuid(),
            title: `from ${from} to ${to}`,
            color: chunk[0]?.color,
            children: chunk
          });
        }
      }
    });
    if (children.length < chunkSize) {
      keys.push(data.key);
    }
    return {
      data: {
        ...data,
        children: children
      },
      expandAll: false,
      expandedKeys: keys
    };
  }
};

const HierarchicalTree = connect(
  (state, props) => {
    const { data, expandAll, expandedKeys } = splitChildren(props.data);
    return {
      data: data,
      group: props.group,
      defaultExpandAll: expandAll,
      defaultExpandedKeys: expandAll?undefined:expandedKeys
    };
  }
)(HierarchicalTreeComponent);

export default HierarchicalTree;