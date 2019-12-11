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

import React from "react";
import { connect } from "react-redux";

import * as actions from "../actions";
import { ImagePreviews } from "./ImagePreviews";
import { ImagePopup } from "./ImagePopup";
import { TermsShortNotice } from "./TermsShortNotice";
import { mapStateToProps } from "../helpers/InstanceHelper";
import { Instance as Component } from "../components/Instance";

import "./Instance.css";

class InstanceComponent extends React.Component {
  componentDidMount() {
    const { definitionIsReady, definitionIsLoading, loadDefinition} = this.props;
    if (!definitionIsReady && !definitionIsLoading) {
      loadDefinition();
    }
  }

  componentDidUpdate(previousProps) {
    const { definitionIsReady, instanceIsLoading, instanceError, type, id, currentInstance, fetch} = this.props;
    if (definitionIsReady && !instanceIsLoading && !instanceError &&  (previousProps.type !== type || previousProps.id !== id || !currentInstance))  {
      fetch(type, id);
    }
  }

  render() {
    return (
      <div className="kgs-instance-container" >
        <Component {...this.props} />
      </div>
    );
  }

}

export const Instance = connect(
  (state, props) => ({
    ...mapStateToProps(state, {
      data: state.instances.currentInstance
    }),
    definitionIsReady: state.definition.isReady,
    definitionIsLoading: state.definition.isLoading,
    instanceIsLoading: state.instances.isLoading,
    instanceError: state.instances.error,
    currentInstance: state.instances.currentInstance,
    ImagePreviewsComponent: ImagePreviews,
    ImagePopupComponent: ImagePopup,
    TermsShortNoticeComponent: TermsShortNotice,
    id: props.match.params.id,
    type: props.match.params.type
  }),
  dispatch => ({
    loadDefinition: () => dispatch(actions.loadDefinition()),
    fetch: (type, id) => dispatch(actions.loadReference(type, id))
  })
)(InstanceComponent);