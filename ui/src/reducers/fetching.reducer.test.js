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

import * as actionsDefinition from "../actions/actions.definition";
import * as actionsGroups from "../actions/actions.groups";
import * as actionsSearch from "../actions/actions.search";
import * as actionsInstances from "../actions/actions.instances";
import { reducer as fetchingReducer} from "./fetching.reducer";
describe("confirguration reducer", () => {
  describe("unknown action", () => {
    it("should return same state", () => {
      const state = {a: {c: 1, d: 2}, b: [{e:3}, {e:4}]};
      const action = {type: "ABCDEFGH"};
      const newState = fetchingReducer(state, action);
      expect(JSON.stringify(newState)).toBe(JSON.stringify(state));
    });
  });
  describe("load definition request", () => {
    it("should set is active to true", () => {
      const state = {active: false};
      const action = actionsDefinition.loadDefinitionRequest();
      const newState = fetchingReducer(state, action);
      expect(newState.active).toBe(true);
    });
  });
  describe("load definition failure", () => {
    it("should set is active to false", () => {
      const state = {active: true};
      const action = actionsDefinition.loadDefinitionFailure("error");
      const newState = fetchingReducer(state, action);
      expect(newState.active).toBe(false);
    });
  });

  describe("load groups request", () => {
    it("should set is active to true", () => {
      const state = {active: false};
      const action = actionsGroups.loadGroupsRequest();
      const newState = fetchingReducer(state, action);
      expect(newState.active).toBe(true);
    });
  });
  describe("load groups success", () => {
    it("should set is active to false", () => {
      const state = {active: true};
      const action = actionsGroups.loadGroupsSuccess(null);
      const newState = fetchingReducer(state, action);
      expect(newState.active).toBe(false);
    });
  });
  describe("load groups failure", () => {
    it("should set is active to false", () => {
      const state = {active: true};
      const action = actionsGroups.loadGroupsFailure("error");
      const newState = fetchingReducer(state, action);
      expect(newState.active).toBe(false);
    });
  });
  describe("load search request", () => {
    it("should set is active to true", () => {
      const state = {active: false};
      const action = actionsSearch.loadSearchRequest();
      const newState = fetchingReducer(state, action);
      expect(newState.active).toBe(true);
    });
  });
  describe("load search result", () => {
    it("should set is active to false", () => {
      const state = {active: true};
      const result = "foo";
      const action = actionsSearch.loadSearchResult(result);
      const newState = fetchingReducer(state, action);
      expect(newState.active).toBe(false);
    });
  });
  describe("load instance request", () => {
    it("should set is active to true", () => {
      const state = {active: false, requestReference: 678, currentInstance: 567, previousInstances:[234, 345, 456]};
      const action = actionsInstances.loadInstanceRequest();
      const newState = fetchingReducer(state, action);
      expect(newState.active).toBe(true);
    });
  });
  describe("load instance success", () => {
    it("should set is active to false", () => {
      const state = {active: true, currentInstance: 567, previousInstances:[234, 345, 456]};
      const action = actionsInstances.loadInstanceSuccess(123);
      const newState = fetchingReducer(state, action);
      expect(newState.active).toBe(false);
    });
  });
  describe("load instance failure", () => {
    it("should set is active to false", () => {
      const state = {active: true};
      const action = actionsInstances.loadInstanceFailure("error");
      const newState = fetchingReducer(state, action);
      expect(newState.active).toBe(false);
    });
  });
  describe("cancel instance loading", () => {
    it("should set is active to false", () => {
      const state = {active: true};
      const action = actionsInstances.cancelInstanceLoading();
      const newState = fetchingReducer(state, action);
      expect(newState.active).toBe(false);
    });
  });
});