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
import { dispatch } from "../../store";
import * as actions from "../../actions";
import { withStoreStateSubscription} from "../withStoreStateSubscription";
import { isMobile } from "../../Helpers/BrowserHelpers";
import { ShareBar } from "../ShareBar";
import { Instance } from "./components/Instance";
import "./styles.css";

export class DetailViewComponent extends PureComponent {
  setPreviousHit() {
    dispatch(actions.setPreviousHit());
  }
  clearAllHits() {
    dispatch(actions.clearAllHits());
  }
  _keyupHandler(event) {
    if (this.props.show) {
      if (event.keyCode === 27) {
        event.preventDefault();
        this.clearAllHits();
      } else if (event.keyCode === 8) {
        event.preventDefault();
        this.setPreviousHit();
      }
    }
  }
  componentDidMount() {
    if (!isMobile) {
      window.addEventListener("keyup", this._keyupHandler.bind(this), false);
    }
    dispatch(actions.updateEmailToLink());
  }
  componentWillUnmount() {
    if (!isMobile) {
      window.removeEventListener("keyup", this._keyupHandler);
    }
  }
  componentDidUpdate() {
    dispatch(actions.updateEmailToLink());
  }
  render() {
    const {show, currentViewId, data, hasPrevious} = this.props;
    //window.console.debug("DetailView rendering: " + currentViewId);
    if (!show) {
      return null;
    }
    return (
      <div data-currentViewId={currentViewId} className="kgs-detailView">
        <div className="kgs-detailView__views">
          {[1,2,3,4,5]
            .map(viewId => (
              {
                show: viewId === currentViewId,
                viewId: viewId,
                data: viewId === currentViewId?data:null
              }
            ))
            .map(({show, viewId, data}) => (
              <div key={viewId} className="kgs-detailView__view" data-viewId={viewId} >
                <div className="kgs-detailView__outerPanel">
                  <div className="kgs-detailView__navigation">
                    {show && (
                      <div className="kgs-detailView__navigation-panel">
                        {hasPrevious && (
                          <button className="kgs-detailView__previousButton" onClick={this.setPreviousHit}>
                            <i className="fa fa-chevron-left" /> Previous
                          </button>
                        )}
                        <ShareBar/>
                        <button className="kgs-detailView__closeButton" onClick={this.clearAllHits}>
                          <i className="fa fa-close" />
                        </button>
                      </div>
                    )}
                  </div>
                  <div className="kgs-detailView__innerPanel">
                    {show && (
                      <Instance data={data} />
                    )}
                  </div>
                </div>
              </div>
            ))
          }
        </div>
      </div>
    );
  }
}

export const DetailView = withStoreStateSubscription(
  DetailViewComponent,
  data => ({
    show: !!data.hits.currentHit,
    currentViewId: data.hits.previousHits.length % 5 + 1,
    data: data.hits.currentHit,
    hasPrevious: !!data.hits.previousHits.length
  })
);
