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
import { Select, SortingSelector} from "searchkit";
import { store } from "../../../../store";
import "./styles.css";

export function SortByPanel() {
  const state = store.getState();
  const sortFields = state.definition.sortFields;

  if (sortFields.length > 0) {
    return <span className="kgs-sortby-panel"><SortingSelector key="sortingSelector" listComponent={Select} options={sortFields} /></span>;
  }
  return null;
}
