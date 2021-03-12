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

import React, { Component } from "react";
import "./ImagePopup.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
export class ImagePopup extends Component {
  constructor(props) {
    super(props);
    this.state = { src: null, error: false };
  }

  componentDidMount() {
    this.loadImage();
  }

  componentDidUpdate(prevProps) {
    if (this.props.src !== prevProps.src) {
      this.loadImage();
    }
  }

  onClick = e => {
    if ((!this.closeBtnRef || (this.closeBtnRef && this.closeBtnRef !== e.target)) && this.wrapperRef && this.wrapperRef.contains(e.target)) {
      e && e.preventDefault();
    } else {
      const { onClick } = this.props;
      typeof onClick === "function" && onClick();
    }
  };

  loadImage() {
    if (typeof this.props.src === "string") {
      if (this.props.src !== this.state.src || this.state.error) {
        this.setState({ src: this.props.src, error: false });
      }
    } else {
      this.setState({ src: null, error: true });
    }
  }

  render() {
    const { className, src, label } = this.props;
    const show = typeof src === "string";
    return (
      <div className={`kgs-image_popup ${show ? "show" : ""} ${className ? className : ""}`} onClick={this.onClick}>
        {show && (
          <div className="fa-stack fa-1x kgs-image_popup-content" ref={ref => this.wrapperRef = ref} >
            {
              this.state.error ?
                <div className="kgs-image_popup-error">
                  <FontAwesomeIcon icon="ban"/>
                  <span>{`failed to fetch image "${label}" ...`}</span>
                </div>
                :
                <React.Fragment>
                  {(typeof this.state.src === "string" && this.state.src.endsWith(".mp4"))?
                    <video alt={label ? label : ""} width="750" height="250" autoPlay loop>
                      <source src={this.state.src} type="video/mp4" />
                    </video>
                    :
                    <img src={this.state.src} alt={label ? label: ""}/>
                  }
                  {label && (
                    <p className="kgs-image_popup-label">{label}</p>
                  )}
                </React.Fragment>
            }
            <div className="kgs-image_popup-close" ref={ref => this.closeBtnRef = ref}>
              <FontAwesomeIcon icon="times"/>
            </div>
          </div>
        )}
      </div>
    );
  }
}

export default ImagePopup;