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

export class Thumbnail extends PureComponent {
  constructor(props) {
    super(props);
    this.state = {
      previewUrl: null
    };
  }
  handleToggle() {
    const { previewUrl } = this.props;
    this.setState(state => {
      const newValue = state.previewUrl?null: previewUrl;
      if (newValue) {
        this.listenClickOutHandler();
      } else {
        this.unlistenClickOutHandler();
      }
      return {
        previewUrl: newValue
      };
    });
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
        <button className="kgs-thumbnail--button" onClick={this.handleToggle.bind(this)} >
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
        {!!this.state.previewUrl && (
          <div className="kgs-thumbnail--preview" ref={ref=>this.wrapperRef = ref}>
            <img src={this.state.previewUrl} alt={alt} />
            {alt && (
              <p className="kgs-thumbnail--preview-label">{alt}</p>
            )}
            <i className="fa fa-close" onClick={this.handleToggle.bind(this)}></i>
            <div className="kgs-thumbnail--preview-arrow"></div>
          </div>
        )}
      </div>
    );
  }
}

export default Thumbnail;