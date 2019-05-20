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
import { Carousel } from "react-responsive-carousel";
import "react-responsive-carousel/lib/styles/carousel.min.css";
import "./ImageCarousel.css";

export class ImageCarousel extends PureComponent {
  onClick = index => {
    const { images, onClick } = this.props;
    typeof onClick === "function" && !Number.isNaN(Number(index)) && images && images.length && index < images.length && onClick(images[index]);
  };
  render() {
    const { className, width, images, onClick } = this.props;
    if (!images || !images.length) {
      return null;
    }
    return (
      <div className={`kgs-image_carousel ${className?className:""}`}>
        <Carousel width={width} autoPlay interval={3000} infiniteLoop={true} showThumbs={true} showIndicators={false} stopOnHover={true} showStatus={false} onClickItem={this.onClick} >
          {images.map(({src, label, hasTarget, isTargetAnimated}) => (
            <div key={src}>
              <img src={src} alt={label?label:""}/>
              {label && (
                <p className="legend" ref={ref=>this.labelRef = ref}>{label}</p>
              )}
              {typeof onClick === "function" && hasTarget && (
                <div className={`kgs-image_carousel-icon ${isTargetAnimated?"is-animated":""}`}><i className={`fa fa-4x ${isTargetAnimated?"fa-play":"fa-search"}`}></i></div>
              )}
            </div>
          ))}
        </Carousel>
      </div>
    );
  }
}

export default ImageCarousel;