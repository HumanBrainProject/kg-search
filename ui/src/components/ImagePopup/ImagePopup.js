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

import React, { useState, useEffect, useRef } from "react";
import "./ImagePopup.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

export const ImagePopup = ({ className, src, label, onClick }) => {
  const [srcState, setSrc] = useState();
  const [error, setError] = useState(false);

  const wrapperRef = useRef();
  const closeBtnRef = useRef();

  useEffect(() => loadImage(), [src]);

  const handleOnClick = e => {
    if ((!closeBtnRef.current || (closeBtnRef.current && closeBtnRef.current !== e.target)) && wrapperRef.current && wrapperRef.current.contains(e.target)) {
      e && e.preventDefault();
    } else {
      typeof onClick === "function" && onClick();
    }
  };

  const loadImage = () => {
    if (typeof src === "string") {
      if (srcState !== src || error) {
        setSrc(src);
        setError(false);
      }
    } else {
      setSrc(null);
      setError(true);
    }
  };

  const show = typeof src === "string";
  return (
    <div className={`kgs-image_popup ${show ? "show" : ""} ${className ? className : ""}`} onClick={handleOnClick}>
      {show && (
        <div className="fa-stack fa-1x kgs-image_popup-content" ref={wrapperRef} >
          {
            error ?
              <div className="kgs-image_popup-error">
                <FontAwesomeIcon icon="ban"/>
                <span>{`failed to fetch image "${label}" ...`}</span>
              </div>
              :
              <React.Fragment>
                {(typeof srcState === "string" && srcState.endsWith(".mp4"))?
                  <video alt={label ? label : ""} width="750" height="250" autoPlay loop>
                    <source src={srcState} type="video/mp4" />
                  </video>
                  :
                  <img src={srcState} alt={label ? label: ""}/>
                }
                {label && (
                  <p className="kgs-image_popup-label">{label}</p>
                )}
              </React.Fragment>
          }
          <div className="kgs-image_popup-close" ref={closeBtnRef}>
            <FontAwesomeIcon icon="times"/>
          </div>
        </div>
      )}
    </div>
  );

};

export default ImagePopup;