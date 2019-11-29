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

const getImage = url => {
  return new Promise((resolve, reject) => {
    if (typeof url !== "string") {
      reject(url);
    }
    const img = new Image();
    img.onload = () => {
      resolve(url);
    };
    img.onerror = () => {
      reject(url);
    };
    img.src = url;
  });
};

export class ImagePopup extends Component {
  constructor(props) {
    super(props);
    this.state = { src: null, fetched: false, error: false };
  }
  onClick = e => {
    if ((!this.closeBtnRef || (this.closeBtnRef && this.closeBtnRef !== e.target)) && this.wrapperRef && this.wrapperRef.contains(e.target)) {
      e && e.preventDefault();
    } else {
      const { onClick } = this.props;
      typeof onClick === "function" && onClick();
    }
  };
  async loadImage() {
    if (typeof this.props.src === "string") {
      if (this.props.src !== this.state.src || this.state.error) {
        this.setState({ src: this.props.src, fetched: false, error: false });
        try {
          await getImage(this.props.src);
          this.setState({ fetched: true });
        } catch (e) {
          this.setState({ fetched: true, error: true });
        }
      }
    } else if (this.state.fetched || this.state.src) {
      this.setState({ src: null, fetched: false, error: false });
    }
  }
  componentDidMount() {
    this.loadImage();
  }
  componentDidUpdate(prevProps) {
    if (this.props.src !== prevProps.src) {
      this.loadImage();
    }
  }
  render() {
    const { className, src, label } = this.props;
    const show = typeof src === "string";
    return (
      <div className={`kgs-image_popup ${show ? "show" : ""} ${className ? className : ""}`} onClick={this.onClick}>
        {show && (
          <div className="fa-stack fa-1x kgs-image_popup-content" ref={ref => this.wrapperRef = ref} >
            {!this.state.fetched ?
              <div>
                <span className="kgs-spinner">
                  <div className="kgs-spinner-logo"></div>
                </span>
                <span className="kgs-spinner-label">{`loading image "${label}" ...`}</span>
              </div>
              :
              this.state.error ?
                <div className="kgs-image_popup-error">
                  <i className="fa fa-ban"></i>
                  <span>{`failed to fetch image "${label}" ...`}</span>
                </div>
                :
                <React.Fragment>
                  <video alt={label ? label : ""} width="750" height="250" autoplay>
                    <source src={this.state.src} type="video/webm" />
                  </video>
                  {label && (
                    <p className="kgs-image_popup-label">{label}</p>
                  )}
                </React.Fragment>
            }
            <i className="fa fa-close" ref={ref => this.closeBtnRef = ref} ></i>
          </div>
        )}
      </div>
    );
  }
}

export default ImagePopup;