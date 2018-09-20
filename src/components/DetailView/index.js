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
import { connect } from "../../store";
import * as actions from "../../actions";
import { isMobile } from "../../Helpers/BrowserHelpers";
import { ShareBar } from "../ShareBar";
import { Instance } from "./components/Instance";
import "./styles.css";

const DetailViewComponent = ({show, currentViewId, data, hasPrevious, setPreviousHit, clearAllHits}) => {
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
                        <button className="kgs-detailView__previousButton" onClick={setPreviousHit}>
                          <i className="fa fa-chevron-left" /> Previous
                        </button>
                      )}
                      <ShareBar/>
                      <button className="kgs-detailView__closeButton" onClick={clearAllHits}>
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
};

class DetailViewContainer extends PureComponent {
  _keyupHandler(event) {
    const {setPreviousHit, clearAllHits} = this.props;
    if (this.props.show) {
      if (event.keyCode === 27) {
        event.preventDefault();
        typeof clearAllHits === "function" && clearAllHits();
      } else if (event.keyCode === 8) {
        event.preventDefault();
        typeof setPreviousHit === "function" && setPreviousHit();
      }
    }
  }
  componentDidMount() {
    const {updateEmailToLink} = this.props;
    if (!isMobile) {
      window.addEventListener("keyup", this._keyupHandler.bind(this), false);
    }
    typeof updateEmailToLink === "function" && updateEmailToLink();
  }
  componentWillUnmount() {
    if (!isMobile) {
      window.removeEventListener("keyup", this._keyupHandler);
    }
  }
  componentDidUpdate() {
    const {updateEmailToLink} = this.props;
    typeof updateEmailToLink === "function" && updateEmailToLink();
  }
  render() {
    const {show, currentViewId, data, hasPrevious, setPreviousHit, clearAllHits} = this.props;
    return (
      <DetailViewComponent show={show} currentViewId={currentViewId} data={data} hasPrevious={hasPrevious} setPreviousHit={setPreviousHit} clearAllHits={clearAllHits} />
    );
  }
}

export const DetailView = connect(
  state => ({
    show: !!state.hits.currentHit,
    currentViewId: state.hits.previousHits.length % 5 + 1,
    data: state.hits.currentHit,
    hasPrevious: !!state.hits.previousHits.length
  }),
  dispatch => ({
    setPreviousHit: () => dispatch(actions.setPreviousHit()),
    clearAllHits: () => dispatch(actions.clearAllHits()),
    updateEmailToLink: () => dispatch(actions.updateEmailToLink())
  })
)(DetailViewContainer);
