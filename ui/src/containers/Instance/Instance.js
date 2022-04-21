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

import * as actionsGroups from "../../actions/actions.groups";
import * as actionsInstances from "../../actions/actions.instances";
import * as actionsDefinition from "../../actions/actions.definition";
import { ImagePopup } from "../Image/ImagePopup";
import { TermsShortNotice } from "../Notice/TermsShortNotice";
import { mapStateToProps } from "../../helpers/InstanceHelper";
import { InstanceContainer } from "./InstanceContainer";

const path = "/instances/";

export const Instance = connect(
  state => {
    const instanceProps = state.instances.currentInstance?
      {
        ...mapStateToProps(state, {
          data: state.instances.currentInstance
        }),
        path: path,
        defaultGroup: state.groups.defaultGroup,
        ImagePopupComponent: ImagePopup,
        TermsShortNoticeComponent: TermsShortNotice
      }
      :
      null;
    return {
      path: path,
      instanceProps: instanceProps,
      showInstance: state.instances.currentInstance && !state.instances.isLoading && !state.instances.error,
      definitionIsReady: state.definition.isReady,
      definitionIsLoading: state.definition.isLoading,
      definitionHasError: !!state.definition.error,
      groupsHasError: !!state.groups.error,
      isGroupsReady: state.groups.isReady,
      isGroupLoading: state.groups.isLoading,
      shouldLoadGroups: !!state.auth.accessToken,
      instanceIsLoading: state.instances.isLoading,
      instanceHasError: !!state.instances.error,
      currentInstance: state.instances.currentInstance,
      previousInstance: state.instances.previousInstances.length?state.instances.previousInstances[state.instances.previousInstances.length-1]:null,
      group: state.groups.group,
      defaultGroup: state.groups.defaultGroup,
      searchPage: false,
      getId: ({type, id}) => type?`${type}/${id}`:id
    };
  },
  dispatch => ({
    loadDefinition: () => dispatch(actionsDefinition.loadDefinition()),
    loadGroups: () => dispatch(actionsGroups.loadGroups()),
    fetch: (group, id) => dispatch(actionsInstances.loadInstance(group, id)),
    clearAllInstances: () => dispatch(actionsInstances.clearAllInstances()),
    goBackToInstance: id => dispatch(actionsInstances.goBackToInstance(id))
  })
)(InstanceContainer);