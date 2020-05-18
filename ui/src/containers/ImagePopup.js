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
import { ImagePopup as Component} from "../components/ImagePopup/ImagePopup";

export const ImagePopup = connect(
  (state, props) => ({
    className: props.className,
    src: state.instances.image && typeof state.instances.image.url === "string" && state.instances.image.url,
    label: (state.instances.image && state.instances.image.label)?state.instances.image.label:""
  }),
  dispatch => ({
    onClick: () => dispatch(actions.showImage(null, null))
  })
)(Component);

export default ImagePopup;