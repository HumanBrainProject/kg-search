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
import PropTypes from "prop-types";
import uniqueId from "lodash/uniqueId";
import ReactTooltip from "react-tooltip";

export const Hint = ({className, show, value}) => {
  if (!show) {
    return null;
  }
  const classNames = ["kgs-hint", className].join(" ");
  const hint_id = encodeURI(uniqueId("kgs-hint_content-"));
  return (
    <span className={classNames}>
      <i className="fa fa-info-circle" data-tip data-for={hint_id} aria-hidden="true"></i>
      <ReactTooltip id={hint_id} place="right" type="dark" effect="solid">
        <span>{value}</span>
      </ReactTooltip>
    </span>
  );
};

Hint.propTypes = {
  className: PropTypes.string,
  show: PropTypes.bool,
  value: PropTypes.string
};

export default Hint;