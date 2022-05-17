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

import React, { useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCircle } from "@fortawesome/free-solid-svg-icons/faCircle";
import Tree from "rc-tree";

import LinkedInstance from "../../containers/LinkedInstance";

import "./HierarchicalTree.css";
import "rc-tree/assets/index.css";

const Icon = ({color}) => (
  <FontAwesomeIcon icon={faCircle} style={{color: color?color:"gray"}} />
);

const Node = ({ node, group }) => {

  return (
    <div className="kgs-hierarchical-details">
      <div className="kgs-hierarchical-info">
        {node && (
          <div>
            <div className="kgs-hierarchical-info-title">
              <Icon color={node.color} />&nbsp;{node.title}
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

const Legend = ({legend}) => {
  if (!(legend instanceof Object)) {
    return null;
  }
  return (
    <ul className="kgs-hierarchical-legend">
      {Object.entries(legend).map(([color, label]) => (
        <li key={label}><Icon color={color} /> {label}</li>
      ))}
    </ul>
  );
};

const HierarchicalTree = ({data, group}) => {
  const [node, setNode] = useState(data);

  const onSelect = (_selectedKeys, info) => setNode(info.node);

  return (
    <>
      <div className="kgs-hierarchical">
        <div className="kgs-hierarchical-tree">
          <Tree
            treeData={[data]}
            defaultExpandAll={true}
            defaultSelectedKeys={[data.key]}
            onSelect={onSelect}
            icon={Icon}
          />
          <Legend legend={data.legend} />
        </div>
        <Node
          node={node}
          group={group}
        />
      </div>
    </>
  );
};

export default HierarchicalTree;
