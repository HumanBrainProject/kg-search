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
import { createSlice } from '@reduxjs/toolkit';

import { api } from '../../services/api';

const DEFAULT_GROUP = 'public';

const initialState = {
  useGroups: false,
  groups: [],
  group: DEFAULT_GROUP,
  defaultGroup: DEFAULT_GROUP,
  initialGroup: DEFAULT_GROUP,
  hasInitialGroup: false
};

const groupsSlice = createSlice({
  name: 'groups',
  initialState,
  reducers: {
    setInitialGroup(state, action) {
      state.initialGroup = action.payload;
      state.hasInitialGroup = true;
    },
    setUseGroups(state) {
      state.useGroups = true;
    },
    setGroup(state, action) {
      let group = action.payload;
      if (!state.groups.some(g => g.value === group)) {
        group = state.defaultGroup;
      }
      state.group = group;
    }
  },
  extraReducers(builder) {
    builder
      .addMatcher(
        api.endpoints.listGroups.matchFulfilled,
        (state, { payload: groups }) => {
          state.groups = groups;
          state.group = state.initialGroup && groups.some(g => g.value === state.initialGroup)? state.initialGroup:state.defaultGroup;
        }
      );
  }
});

export const selectIsCurated = state => state.groups.group === 'curated';

export const selectIsDefaultGroup = state => state.groups.group === state.groups.defaultGroup;

export const selectGroupLabel = (state, name) => {
  let label = null;
  state.groups.groups.some(group => {
    if (group.value === name) {
      label = group.label;
      return true;
    }
    return false;
  });
  return label;
};


export const { setInitialGroup, setUseGroups, setGroup } = groupsSlice.actions;
export default groupsSlice.reducer;