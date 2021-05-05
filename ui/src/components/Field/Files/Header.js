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
import PropTypes from "prop-types";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

const Header = ({node}) => (
  <div className="kgs-hierarchical-files__node_wrapper">
    <div className="kgs-hierarchical-files__node">
      {node.thumbnail ?
        <img height="14" width="12" src={node.thumbnail} alt={node.url} style={{marginRight: "5px"}} />:
        node.type === "file" ? <FontAwesomeIcon icon="file" style={{marginRight: "5px"}} />:null
      }
      <span className="kgs-hierarchical-files__node_name">{node.name}</span>
    </div>
  </div>
);

Header.propTypes = {
  style: PropTypes.object,
  node: PropTypes.object.isRequired
};

export default Header;