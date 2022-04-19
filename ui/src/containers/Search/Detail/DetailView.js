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

import { connect } from "react-redux";
import * as actionsInstances from "../../../actions/actions.instances";
import { Carousel } from "../../../components/Carousel/Carousel";
import { ShareButtons } from "../../Share/ShareButtons";
import { Instance } from "../Instance";
import "./DetailView.css";


const mapStateToProps = state => {
  return {
    className: "kgs-detailView",
    show: !!state.instances.currentInstance,
    data: state.instances.currentInstance ? [...state.instances.previousInstances, state.instances.currentInstance] : [],
    itemComponent: Instance,
    navigationComponent: ShareButtons,
  };
};

const mapDispatchToProps = dispatch => ({
  onPrevious: navigate => {
    dispatch(actionsInstances.setPreviousInstance());
    dispatch(actionsInstances.updateLocation(navigate));
  },
  onClose: navigate => {
    dispatch(actionsInstances.clearAllInstances());
    dispatch(actionsInstances.updateLocation(navigate));
  }
});

export const DetailView = connect(
  mapStateToProps,
  mapDispatchToProps
)(Carousel);
