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

import {faBan} from "@fortawesome/free-solid-svg-icons/faBan";
import {faTimes} from "@fortawesome/free-solid-svg-icons/faTimes";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React, { useState, useEffect, useRef } from "react";
import showdown from "showdown";
import xssFilter from "showdown-xss-filter";

import "./ImagePopup.css";

const converter = new showdown.Converter({ extensions: [xssFilter] });

const Media = ({label, srcState}) => {
  const isVideo = typeof srcState === "string" && srcState.endsWith(".mp4");
  const alt = label ? label:"";
  if(isVideo) {
    return(
      <video alt={alt} width="750" height="250" autoPlay loop>
        <source src={srcState} type="video/mp4" />
      </video>
    );
  }
  return <img src={srcState} alt={alt} />;
};

const ImagePopup = ({ className, src, label, link, onClick }) => {
  const [srcState, setSrc] = useState();
  const [error, setError] = useState(false);

  const wrapperRef = useRef();
  const closeBtnRef = useRef();

  // eslint-disable-next-line react-hooks/exhaustive-deps
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
                <FontAwesomeIcon icon={faBan} />
                <span>{`failed to fetch image "${label}" ...`}</span>
              </div>
              :
              <React.Fragment>
                <Media label={label} srcState={srcState}/>
                {label && (
                  <div className="kgs-image_popup-label-wrapper">
                    <p className="kgs-image_popup-label">{label}</p>
                  </div>)}
                {link && <div className="kgs-image_popup-link" dangerouslySetInnerHTML={{ __html: converter.makeHtml(link) }} />}
              </React.Fragment>
          }
          <div className="kgs-image_popup-close" ref={closeBtnRef}>
            <FontAwesomeIcon icon={faTimes} />
          </div>
        </div>
      )}
    </div>
  );

};

export default ImagePopup;