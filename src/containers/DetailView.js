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

import { connect } from "../store";
import * as actions from "../actions";
import { Carrousel } from "../components/Carrousel";
import { ShareButtons } from "./ShareButtons";
import { Instance } from "./Instance";
import "./DetailView.css";

const mapStateToProps = state => {
  return {
    className: "kgs-detailView",
    show: !!state.hits.currentHit,
    data: state.hits.currentHit?[...state.hits.previousHits, state.hits.currentHit]:[],
    itemComponent: Instance,
    navigationComponent: ShareButtons
  };
};

const mapDispatchToProps = dispatch => ({
  onPrevious: () => dispatch(actions.setPreviousHit()),
  onClose: () => dispatch(actions.clearAllHits())
});

export const DetailView = connect(
  mapStateToProps,
  mapDispatchToProps
)(Carrousel);
