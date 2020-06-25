/*
*   Copyright (c) 2020, EPFL/Human Brain Project PCO
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
import PropTypes from "prop-types";


const Header = ({node}) => {
  const iconStyle = {marginRight: "5px"};
  return(
    <div className="kgs-hierarchical-files__node_wrapper">
      <div className="kgs-hierarchical-files__node">
        {node.thumbnail ?
          <img height="14" width="12" src={node.thumbnail} alt={node.url} style={iconStyle} />:
          node.type === "file" ? <i className={"fa fa-file-o"} style={iconStyle}/>:null
        }
        <span className="kgs-hierarchical-files__node_name">{node.name}</span>
      </div>
    </div>);
};

Header.propTypes = {
  style: PropTypes.object,
  node: PropTypes.object.isRequired
};

export default Header;