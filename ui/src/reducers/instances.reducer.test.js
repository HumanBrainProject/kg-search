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

import { setTypeMappings, loadInstanceRequest, loadInstanceSuccess, setPreviousInstance, clearAllInstances} from "../actions/actions.instances";
import { reducer as instancesReducer} from "./instances.reducer";
describe("instances reducer", () => {
  describe("load settings success", () => {
    it("should set type mappings", () => {
      const state = undefined;
      const typeMappings = {a: 1, b: 2, c: 4};
      const action = setTypeMappings(typeMappings);
      const newState = instancesReducer(state, action);
      expect(newState.typeMappings).toBe(typeMappings);
    });
  });
  describe("unknown action", () => {
    it("should return same state", () => {
      const state = {a: {c: 1, d: 2}, b: [{e:3}, {e:4}]};
      const action = {type: "ABCDEFGH"};
      const newState = instancesReducer(state, action);
      expect(JSON.stringify(newState)).toBe(JSON.stringify(state));
    });
  });
  describe("load instance request", () => {
    it("should set loading reference", () => {
      const state = {isLoading: false, currentInstance: {id: 567}, previousInstances:[{id: 234}, {id: 345}, {id: 456}]};
      const action = loadInstanceRequest();
      const newState = instancesReducer(state, action);
      expect(newState.isLoading).toBe(true);
    });
  });
  describe("load instance success", () => {
    it("should set current instance", () => {
      const state = undefined;
      const action = loadInstanceSuccess({id: 123});
      const newState = instancesReducer(state, action);
      expect(newState.currentInstance).toMatchObject({id: 123});
    });
    it("should increase previous instances array length", () => {
      const state = {currentInstance: {id: 567}, previousInstances:[{id: 234}, {id: 345}, {id: 456}]};
      const action = loadInstanceSuccess({id: 123});
      const newState = instancesReducer(state, action);
      expect(newState.previousInstances.length).toBe(4);
    });
    it("should put previous instance into previous instances array", () => {
      const state = {currentInstance: {id: 567}, previousInstances:[{id: 234}, {id: 345}, {id: 456}]};
      const action = loadInstanceSuccess({id: 123});
      const newState = instancesReducer(state, action);
      expect(newState.previousInstances[3]).toMatchObject({id: 567});
    });
  });
  describe("set previous instance", () => {
    it("should set last item of previous instances array as current instance", () => {
      const state = {currentInstance: {id: 567}, previousInstances:[{id: 234}, {id: 345}, {id: 456}]};
      const action = setPreviousInstance();
      const newState = instancesReducer(state, action);
      expect(newState.currentInstance).toMatchObject({id: 456});
    });
    it("should set current instance as null when previous instances array is empty", () => {
      const state = {currentInstance: {id: 567}, previousInstances:[]};
      const action = setPreviousInstance();
      const newState = instancesReducer(state, action);
      expect(newState.currentInstance).toBe(null);
    });
  });
  describe("clear all instances", () => {
    it("should set current instance to null", () => {
      const state = {currentInstance: {id: 567}, previousInstances:[{id: 234}, {id: 345}, {id: 456}]};
      const action = clearAllInstances();
      const newState = instancesReducer(state, action);
      expect(newState.currentInstance).toBe(null);
    });
    it("should set previous instances array empty", () => {
      const state = {currentInstance: {id: 567}, previousInstances:[]};
      const action = clearAllInstances();
      const newState = instancesReducer(state, action);
      expect(newState.previousInstances.length).toBe(0);
    });
  });
});