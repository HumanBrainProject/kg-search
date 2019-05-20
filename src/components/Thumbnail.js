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
import "./Thumbnail.css";

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

export class Thumbnail extends PureComponent {
  constructor(props) {
    super(props);
    this.state = { src: null, show: false, fetched: false, error: false };
  }
  async loadImage() {
    if (typeof this.props.previewUrl === "string") {
      if (this.props.previewUrl !== this.state.src || this.state.error) {
        this.setState({ src: this.props.previewUrl, fetched: false, error: false });
        try {
          await getImage(this.props.previewUrl);
          this.setState({ fetched: true });
        } catch (e) {
          this.setState({ fetched: true, error: true });
        }
      }
    } else if (this.state.fetched || this.state.src) {
      this.setState({ src: null, fetched: false, error: false });
    }
  }
  handleToggle = () => {
    if (!this.state.show) {
      this.setState({ show: true });
      this.loadImage();
      this.listenClickOutHandler();
    } else {
      this.setState({ show: false });
      this.unlistenClickOutHandler();
    }
  }

  clickOutHandler = e => {
    if (this.wrapperRef  && this.wrapperRef.contains(e.target)) {
      e && e.preventDefault();
    } else {
      this.unlistenClickOutHandler();
      this.setState({previewUrl: null});
    }
  };

  listenClickOutHandler(){
    this.clickOutHandlerRef = this.clickOutHandler.bind(this);
    window.addEventListener("mouseup", this.clickOutHandlerRef, false);
    window.addEventListener("touchend", this.clickOutHandlerRef, false);
    window.addEventListener("keyup", this.clickOutHandlerRef, false);
  }

  unlistenClickOutHandler(){
    window.removeEventListener("mouseup", this.clickOutHandlerRef, false);
    window.removeEventListener("touchend", this.clickOutHandlerRef, false);
    window.removeEventListener("keyup", this.clickOutHandlerRef, false);
  }

  componentWillUnmount(){
    this.unlistenClickOutHandler();
  }

  render() {
    const {thumbnailUrl, showPreview, previewUrl, isAnimated, alt} = this.props;

    if (!showPreview || typeof previewUrl !== "string") {
      if (typeof thumbnailUrl === "string") {
        return (
          <span className="kgs-thumbnail--panel">
            <div><img src={thumbnailUrl} alt={alt} /></div>
          </span>
        );
      }
      return (
        <span className="fa-stack fa-1x kgs-thumbnail--panel">
          <i className="fa fa-file-o fa-stack-1x"></i>
        </span>
      );
    }

    return (
      <div className="fa-stack fa-1x kgs-thumbnail--container">
        <button className="kgs-thumbnail--button" onClick={this.handleToggle} >
          {typeof thumbnailUrl === "string"?
            <span className="kgs-thumbnail--panel">
              <div className="kgs-thumbnail--image">
                <img src={thumbnailUrl} alt={alt} />
                {isAnimated?
                  <i className="fa fa-play kgs-thumbnail--zoom-dynamic"></i>
                  :
                  <i className="fa fa-search kgs-thumbnail--zoom-static"></i>
                }
              </div>
            </span>
            :
            <span className="fa-stack fa-1x kgs-thumbnail--panel">
              <i className="fa fa-file-image-o fa-stack-1x"></i>
              {isAnimated?
                <i className="fa fa-play fa-stack-1x kgs-thumbnail--zoom-dynamic"></i>
                :
                <i className="fa fa-search fa-stack-1x kgs-thumbnail--zoom-static"></i>
              }
            </span>
          }
        </button>
        {this.state.show && (
          <div className="kgs-thumbnail--preview" ref={ref=>this.wrapperRef = ref}>
            {!this.state.fetched?
              <div className="kgs-thumbnail--preview-fetching">
                <span className="kgs-spinner">
                  <div className="kgs-spinner-logo"></div>
                </span>
                <span className="kgs-spinner-label">{alt?`loading image "${alt}" ...`:"loading image..."}</span>
              </div>
              :
              this.state.error?
                <div className="kgs-thumbnail--preview-error">
                  <i className="fa fa-ban"></i>
                  <span>{alt?`failed to fetch image "${alt}" ...`:"failed to fetch image..."}</span>
                </div>
                :
                <React.Fragment>
                  <img src={this.state.src} alt={alt?alt:""} />
                  {alt && (
                    <p className="kgs-thumbnail--preview-label">{alt}</p>
                  )}
                </React.Fragment>
            }
            <i className="fa fa-close kgs-thumbnail--preview-close-btn" onClick={this.handleToggle}></i>
            <div className="kgs-thumbnail--preview-arrow"></div>
          </div>
        )}
      </div>
    );
  }
}

export default Thumbnail;