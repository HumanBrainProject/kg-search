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

import React, { PureComponent } from "react";
import "./ImagePopup.css";

export class ImagePopup extends PureComponent {
  onClick = e => {
    const { onClick } = this.props;
    if (e.target === this.labelRef) {
      e.preventDefault();
      return;
    }
    typeof onClick === "function" && onClick();
  };
  render() {
    const { className, src, label } = this.props;
    const show = typeof src === "string";
    return (
      <div className={`kgs-image_popup ${show?"show":""} ${className?className:""}`} onClick={this.onClick}>
        {show && (
          <div className="fa-stack fa-1x kgs-image_popup-content">
            <img src={src} alt={label?label:""}/>
            {label && (
              <p className="kgs-image_popup-label" ref={ref=>this.labelRef = ref}>{label}</p>
            )}
            <i className="fa fa-close"></i>
          </div>
        )}
      </div>
    );
  }
}

export default ImagePopup;