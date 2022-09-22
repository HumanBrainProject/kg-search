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
import { createSlice } from "@reduxjs/toolkit";

import { api } from "../../app/services/api";


const getTitle = data => {
  if (data?.id) {
    if (data?.title) {
      if (data?.version) {
        return `${data.title} ${data.version}`;
      }
      return `${data.title}`;
    }
    if (data?.type) {
      return `${data.type} ${data.id}`;
    }
    return data.id;
  }
  return null;
};

const updateInstance = (state, data) => {
  if (data.id === state.instanceId) {
    if (state.data?.id && state.data.id !== data.id) {
      state.history.push({
        id: state.data.id,
        title: state.title,
        tab: state.tab
      });
    }
    const tab = state.data?.tab;
    state.data = data;
    state.title = getTitle(data);
    state.tab = tab;
  } else {
    state.instanceId = null;
    state.data = null;
    state.title = null;
    state.tab = null;
  }
};

const initialState = {
  typeMappings: {},
  instanceId: null,
  data: null,
  title: null,
  tab: null,
  history: [],
  image: null
};

const instanceSlice = createSlice({
  name: "instances",
  initialState,
  reducers: {
    setInstanceId(state, action) {
      state.instanceId = action.payload;
      // if (Math.round(Math.random()* 10)%2 === 0) {
      //   state.instanceId = "a8478880-596d-4a0d-a14c-199f20ace75a";
      // }
    },
    reset(state) {
      state.instanceId = null;
      state.data = null;
      state.title = null;
      state.tab = null;
      state.history = [];
      state.image = null;
    },
    syncHistory(state, action) {
      const instanceId = action.payload;
      if (instanceId) {
        let entry = null;
        if (state.history.length) {
          entry = state.history.pop();
          while (state.history.length && entry && entry.id !== instanceId) {
            entry = state.history.pop();
          }
          if (entry) {
            if (state.instanceId !== instanceId) {
              state.instanceId = instanceId;
              state.data = { tab: entry.tab }; // detail should stay open
            }
            state.tab = entry.tab;
          }
        } else {
          state.instanceId = instanceId;
        }
      } else {
        state.instanceId = null;
        state.data = null;
        state.title = null;
        state.tab = null;
        state.history = [];
        state.image = null;
      }
    },
    showImage(state, action) {
      state.image = (typeof action.payload?.url === "string")?action.payload:null;
    },
    setTab(state, action) {
      state.tab = action.payload;
    },
    setInstanceFromCache: (state, { payload }) => {
      updateInstance(state, payload);
    }
  },
  extraReducers(builder) {
    builder
      .addMatcher(
        api.endpoints.getSettings.matchFulfilled,
        (state, { payload }) => {
          state.typeMappings = (payload?.typeMappings instanceof Object)?payload.typeMappings:{};
        }
      )
      .addMatcher(
        api.endpoints.getInstance.matchFulfilled,
        (state, { payload }) => {
          updateInstance(state, payload);
        }
      )
      .addMatcher(
        api.endpoints.getInstance.matchPending,
        state => {
          state.image = null;
        }
      )
      .addMatcher(
        api.endpoints.getPreview.matchFulfilled,
        (state, { payload }) => {
          updateInstance(state, payload);
        }
      )
      .addMatcher(
        api.endpoints.getPreview.matchPending,
        state => {
          state.image = null;
        }
      );
  }
});

export const selectTypeMapping = (state, name) => state.instance.typeMappings[name];

export const selectPreviousInstance = state => state.instance.history.length?state.instance.history[state.instance.history.length-1]:null;

export const { setInstanceId, reset, syncHistory, showImage, setTab } = instanceSlice.actions;

export const instancesCacheActions = {
  "getInstance": instanceSlice.actions.setInstanceFromCache,
  "getPreview": instanceSlice.actions.setInstanceFromCache
};

export default instanceSlice.reducer;