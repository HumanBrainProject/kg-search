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

import { connect } from "react-redux";
import * as actions from "../actions/actions";
import { ImageCarousel } from "../components/Carousel/ImageCarousel";

export const ImagePreviews = connect(
  (state, props) => {
    const images = props.images && props.images.length && props.images
      .map(image => ({
        src: image && image.staticImageUrl && (typeof image.staticImageUrl === "string"?image.staticImageUrl:image.staticImageUrl.url),
        label: image && image.label,
        target: image && image.previewUrl && (typeof image.previewUrl === "string"?image.previewUrl:image.previewUrl.url),
        hasTarget: !!image && !!image.previewUrl && (typeof image.previewUrl === "string" || typeof image.previewUrl.url === "string"),
        isTargetAnimated: !!image && !!image.previewUrl && !!image.previewUrl.isAnimated
      }))
      .filter(image => image.src);
    return {
      className: props.className,
      width: props.width,
      images: images
    };
  },
  dispatch => ({
    onClick: image => image && image.target && typeof image.target === "string" && dispatch(actions.showImage(image.target, image.label))
  })
)(ImageCarousel);

export default ImagePreviews;