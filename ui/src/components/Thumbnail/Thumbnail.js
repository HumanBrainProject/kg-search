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
import "./Thumbnail.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

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

export class Thumbnail extends React.Component {
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
  };

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
    const {url, alt, isAnimated, onClick} = this.props;

    if (typeof onClick !== "function") {
      if (typeof url === "string") {
        return (
          <span className="kgs-thumbnail--panel">
            <div><img src={url} alt={alt} /></div>
          </span>
        );
      }
      return (
        <span className="fa-stack fa-1x kgs-thumbnail--panel">
          <FontAwesomeIcon icon="file" />
        </span>
      );
    }

    return (
      <div className="fa-stack fa-1x kgs-thumbnail--container">
        <button className="kgs-thumbnail--button" onClick={onClick} >
          {typeof url === "string"?
            <span className="kgs-thumbnail--panel">
              <div className="kgs-thumbnail--image">
                <img src={url} alt={alt} />
                {isAnimated?
                  <FontAwesomeIcon icon="play" className="kgs-thumbnail--zoom-dynamic" />
                  :
                  <FontAwesomeIcon icon="search" className="kgs-thumbnail--zoom-static" />
                }
              </div>
            </span>
            :
            <span className="fa-stack fa-1x kgs-thumbnail--panel">
              <FontAwesomeIcon icon="fileImage"/>
              {isAnimated?
                <FontAwesomeIcon icon="play" className="kgs-thumbnail--zoom-dynamic"/>
                :
                <FontAwesomeIcon icon="search" className="kgs-thumbnail--zoom-static"/>
              }
            </span>
          }
        </button>
      </div>
    );
  }
}

export default Thumbnail;