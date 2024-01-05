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

import {faPlay} from '@fortawesome/free-solid-svg-icons/faPlay';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import React, { useState } from 'react';
import { Carousel } from 'react-responsive-carousel';
import { Hint } from '../Hint/Hint';
import 'react-responsive-carousel/lib/styles/carousel.min.css';
import './ImageCarousel.css';


export const ImageCarousel = ({ className, width, images, onClick }) => {
  const [hint, setHint] = useState(!images || images.length === 0?'':images[0].label);
  const onClickItem = index => !Number.isNaN(Number(index)) && images && images.length && index < images.length && onClick(images[index]);

  const handleOnChange = index => {
    setHint(images[index].label);
  };

  if (!images || !images.length) {
    return null;
  }

  return (
    <div className={`kgs-image_carousel ${className ? className : ''}`}>
      <Carousel width={width} autoPlay interval={3000} infiniteLoop={true} showThumbs={images.length > 1} showIndicators={false} stopOnHover={true} showStatus={false} onClickItem={onClickItem} onChange={handleOnChange} >
        {images.map(({ src, label, isTargetAnimated }) => (
          <div key={src}>
            <img src={src} alt={label ? label : ''} />
            {isTargetAnimated && <div className="kgs-image_carousel-icon is-animated">
              <FontAwesomeIcon icon={faPlay} size="3x" />
            </div>}
          </div>
        ))}
      </Carousel>
      {!!hint && (
        <Hint className="kgs-image_carousel-hint" value={hint} />
      )}
    </div>
  );
};

export default ImageCarousel;