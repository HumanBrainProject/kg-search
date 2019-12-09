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
import { withTabKeyNavigation } from "../helpers/withTabKeyNavigation";
import { Route, Switch } from "react-router-dom";
import { MasterView } from "./MasterView";
import { DetailView } from "./DetailView";

const MasterDetailBase = ({show}) => {
  if (!show) {
    return null;
  }

  return (
    <React.Fragment>
      <Switch>
        <Route path="/search" exact component={MasterView} />
        <Route path="/instances/:type/:id" component={DetailView} />
        {/* <DetailView/> */}
      </Switch>
    </React.Fragment>
  );
};

const MasterDetailWithTabKeyNavigation = withTabKeyNavigation(
  "isActive",
  null,
  ".kgs-app"
)(MasterDetailBase);

export const MasterDetail = connect(
  (state, props) => ({
    isActive: !state.instances.currentInstance,
    show: state.application.isReady
  })
)(MasterDetailWithTabKeyNavigation);
