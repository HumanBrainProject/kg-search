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

import { reducer as applicationReducer} from "./application.reducer";
import { loadSettingsFailure } from "../actions/actions.application";
describe("application reducer", () => {
  describe("unknown action", () => {
    it("should return same state", () => {
      const state = {a: {c: 1, d: 2}, b: [{e:3}, {e:4}]};
      const action = {type: "ABCDEFGH"};
      const newState = applicationReducer(state, action);
      expect(JSON.stringify(newState)).toBe(JSON.stringify(state));
    });
  });
  describe("load settings failure", () => {
    it("should set ready to false", () => {
      const state = {error: null};
      const action = loadSettingsFailure("error");
      const newState = applicationReducer(state, action);
      expect(newState.error).toBe("error");
    });
  });
});