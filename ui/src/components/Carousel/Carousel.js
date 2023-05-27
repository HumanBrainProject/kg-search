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

import {faChevronLeft} from "@fortawesome/free-solid-svg-icons/faChevronLeft";
import {faTimes} from "@fortawesome/free-solid-svg-icons/faTimes";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import uniqueId from "lodash/uniqueId";
import PropTypes from "prop-types";
import React, { useEffect, useRef, useMemo } from "react";

import { isMobile } from "../../helpers/BrowserHelpers";

import "./Carousel.css";

const getNavigation = (item, showPrevious, onClose, onBack, navigationComponent) => {
  const NavigationComponent = navigationComponent;
  const Navigation = () => (
    <div className="kgs-carousel__header">
      {item.isActive && showPrevious && (
        <button className="kgs-carousel__previous-button" onClick={onBack}>
          <FontAwesomeIcon icon={faChevronLeft} /> Previous
        </button>
      )}
      <div className="kgs-carousel__navigation">
        {item.isActive && item.data && NavigationComponent && (
          <NavigationComponent />
        )}
      </div>
      {item.isActive && (
        <button className="kgs-carousel__close-button" onClick={onClose}>
          <FontAwesomeIcon icon={faTimes} />
        </button>
      )}
    </div>
  );
  Navigation.displayName = "Navigation";
  return Navigation;
};

const CarouselItem = ({ item, showPrevious, onClose, onBack, itemComponent, navigationComponent }) => {
  const ItemComponent = itemComponent;
  const NavigationComponent = getNavigation(item, showPrevious, onClose, onBack, navigationComponent);
  return (
    <div className={`kgs-carousel__item position${item.position}`}>
      <div className="kgs-carousel__content">
        {item.isActive && item.data && ItemComponent && (
          <ItemComponent data={item.data} customNavigationComponent={NavigationComponent} />
        )}
      </div>
    </div>
  );
};

const nbOfItems = 5;

const Carousel = ({ className, data, onBack, onClose, itemComponent, navigationComponent }) => {
  const wrapperRef = useRef();

  useEffect(() => {
    if (!isMobile) {
      const _keyupHandler = event => {
        if (event.keyCode === 27) {
          event.preventDefault();
          onClose();
        }
      };
      window.addEventListener("keyup", _keyupHandler, false);
      return () => window.removeEventListener("keyup", _keyupHandler);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const items = useMemo(() => Array.from(Array(nbOfItems)).map((_, idx) => ({
    id: uniqueId("kgs-carousel__item-"),
    position: idx,
    isActive: false,
    data: null
  })), []);

  items.forEach((item, idx) => {
    const currentPosition = (data.length - 1) % nbOfItems;
    const idxData = data.length - 1 - (idx <= currentPosition ? (currentPosition - idx) : (nbOfItems - (idx - currentPosition)));
    const position = (idx <= currentPosition ? (nbOfItems - (currentPosition - idx)) : (idx - currentPosition)) % 5;

    item.isActive = idx === currentPosition;
    item.position = position;
    item.data = idxData >= 0 ? data[idxData] : null;
  });

  const handleOnClose = e => {
    if (wrapperRef && !wrapperRef.current.contains(e.target)) {
      onClose();
    }
  };

  const showPrevious = data.length > 1;

  const classNames = ["kgs-carousel", className].join(" ");

  return (
    <div className={classNames} onClick={handleOnClose}>
      <div className="kgs-carousel__panel" ref={wrapperRef}>
        {items.map(item => (
          <CarouselItem key={item.id} item={item} showPrevious={showPrevious} onBack={onBack} onClose={onClose} itemComponent={itemComponent} navigationComponent={navigationComponent} />
        ))}
      </div>
    </div>
  );

};

Carousel.propTypes = {
  className: PropTypes.string,
  data: PropTypes.arrayOf(PropTypes.any),
  onBack: PropTypes.func.isRequired,
  onClose: PropTypes.func.isRequired,
  itemComponent: PropTypes.oneOfType([
    PropTypes.element,
    PropTypes.func,
    PropTypes.object
  ]).isRequired,
  navigationComponent: PropTypes.oneOfType([
    PropTypes.element,
    PropTypes.func,
    PropTypes.object
  ])
};

export default Carousel;