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

import * as actionsGroups from "../actions/actions.groups";
import * as actionsInstances from "../actions/actions.instances";
import * as actionsDefinition from "../actions/actions.definition";
import { history } from "../store";
import { ImagePreviews } from "./ImagePreviews";
import { ImagePopup } from "./ImagePopup";
import { TermsShortNotice } from "./TermsShortNotice";
import { mapStateToProps } from "../helpers/InstanceHelper";
import { InstanceContainer } from "./InstanceContainer";

export const Preview = connect(
  (state, props) => {
    const type = `${props.match.params.org}/${props.match.params.domain}/${props.match.params.schema}/${props.match.params.version}`;
    const instanceProps = state.instances.currentInstance?
      {
        ...mapStateToProps(state, {
          data: state.instances.currentInstance
        }),
        path: "/previews/",
        defaultGroup: state.groups.defaultGroup,
        ImagePreviewsComponent: ImagePreviews,
        ImagePopupComponent: ImagePopup,
        TermsShortNoticeComponent: TermsShortNotice
      }
      :
      null;
    return {
      instanceProps: instanceProps,
      showInstance: state.instances.currentInstance,
      definitionIsReady: state.definition.isReady,
      definitionIsLoading: state.definition.isLoading,
      definitionHasError: !!state.definition.error,
      groupsHasError: !!state.groups.error,
      isGroupsReady: state.groups.isReady,
      isGroupLoading: state.groups.isLoading,
      shouldLoadGroups: !!state.auth.accessToken,
      instanceIsLoading: state.instances.isLoading,
      shouldLoadInstance: !state.instances.currentInstance || state.instances.currentInstance._type !==  type || state.instances.currentInstance._id !==  props.match.params.id,
      instanceHasError: !!state.instances.error,
      currentInstance: state.instances.currentInstance,
      previousInstance: state.instances.previousInstances.length?state.instances.previousInstances[state.instances.previousInstances.length-1]:null,
      group: state.groups.group,
      defaultGroup: state.groups.defaultGroup,
      id: props.match.params.id,
      type: type,
      location: state.router.location
    };
  },
  dispatch => ({
    setInitialGroup: group => dispatch(actionsGroups.setInitialGroup(group)),
    loadDefinition: () => dispatch(actionsDefinition.loadDefinition()),
    loadGroups: () => dispatch(actionsGroups.loadGroups()),
    fetch: (type, id) => dispatch(actionsInstances.loadPreview(type, id, "/previews/")),
    setPreviousInstance: () => dispatch(actionsInstances.setPreviousInstance()),
    onGoHome: path => {
      dispatch(actionsInstances.clearAllInstances());
      history.push(path);
    }
  })
)(InstanceContainer);