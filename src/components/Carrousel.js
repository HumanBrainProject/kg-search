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
import { isMobile } from "../helpers/BrowserHelpers";
import "./Carrousel.css";


const CarrouselItem = ({data, showPrevious, onPrevious, onClose, itemComponent, navigationComponent}) => {
  const ItemComponent =  itemComponent;
  const NavigationComponent = navigationComponent;
  return (
    <div className="kgs-carrousel__panel">
      <div className="kgs-carrousel__header">
        {showPrevious && (
          <button className="kgs-carrousel__previous-button" onClick={onPrevious}>
            <i className="fa fa-chevron-left" /> Previous
          </button>
        )}
        <div className="kgs-carrousel__navigation">
          {data && (
            <NavigationComponent/>
          )}
        </div>
        <button className="kgs-carrousel__close-button" onClick={onClose}>
          <i className="fa fa-close" />
        </button>
      </div>
      <div className="kgs-carrousel__body">
        {data && (
          <ItemComponent data={data} />
        )}
      </div>
    </div>
  );
};

const nbOfViews = 5;

export class Carrousel extends PureComponent {
  constructor(props) {
    super(props);
    this.views =  Array.from(Array(nbOfViews)).map((x, i) => ({
      id: i + 1,
      data: null
    }));
  }
  componentDidMount() {
    if (!isMobile) {
      window.addEventListener("keyup", this._keyupHandler.bind(this), false);
    }
  }
  componentWillUnmount() {
    if (!isMobile) {
      window.removeEventListener("keyup", this._keyupHandler);
    }
  }
  _keyupHandler(event) {
    const {onPrevious, onClose} = this.props;
    if (this.props.show) {
      if (event.keyCode === 27) {
        event.preventDefault();
        typeof onClose === "function" && onClose();
      } else if (event.keyCode === 8) {
        event.preventDefault();
        typeof onPrevious === "function" && onPrevious();
      }
    }
  }
  render(){
    const {className, show, data, onPrevious, onClose, itemComponent, navigationComponent} = this.props;
    if (!show || !Array.isArray(data) || !data.length) {
      return null;
    }

    //window.console.debug("Carrousel rendering...", data);

    const selectedView = (data.length -1) % nbOfViews + 1;
    const views = this.views;
    views.forEach((view, idx) => {
      const idxData = (idx <= selectedView - 1)?data.length - (selectedView - idx):data.length - nbOfViews  +  (idx - selectedView);
      view.data = idxData >= 0?data[idxData]:null;
    });
    const showPrevious = data.length > 1;

    const classNames = ["kgs-carrousel", className].join(" ");

    return(
      <div data-selected={selectedView} className={classNames}>
        <div className="kgs-carrousel__views">
          {views.map(view => (
            <div key={view.id} className="kgs-carrousel__view" data-view={view.id} >
              <CarrouselItem data={view.data} showPrevious={showPrevious} onPrevious={onPrevious} onClose={onClose} itemComponent={itemComponent} navigationComponent={navigationComponent} />
            </div>
          ))}
        </div>
      </div>
    );
  }
}