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

import * as actions from "../actions";
import { reducer as definitionReducer} from "./definition.reducer";
describe('confirguration reducer', () => {
    describe('unknown action', () => {
        it('should return same state', () => {
            const state = {a: {c: 1, d: 2}, b: [{e:3}, {e:4}]};
            const action = {type: "ABCDEFGH"};
            const newState = definitionReducer(state, action);
            expect(JSON.stringify(newState)).toBe(JSON.stringify(state));
        });
    });
    describe('load definition', () => {
        it('should set has request to true', () => {
            const state = undefined;
            const action = actions.loadDefinition();
            const newState = definitionReducer(state, action);
            expect(newState.hasRequest).toBe(true);
        });
    });
    describe('load definition success', () => {
        it('should set current definition', () => {
            const state = undefined;
            const definition = {};
            const action = actions.loadDefinitionSuccess(definition);
            const newState = definitionReducer(state, action);
            expect(newState.queryFields.length).toBe(0);
        });
        it('should set is ready to true', () => {
            const state = {isReady: false};
            const action = actions.loadDefinitionSuccess(null);
            const newState = definitionReducer(state, action);
            expect(newState.isReady).toBe(true);
        });
    });
    describe('load definition failure', () => {
        it('should set ready to false', () => {
            const state = {isReady: true};
            const action = actions.loadDefinitionFailure("error");
            const newState = definitionReducer(state, action);
            expect(newState.isReady).toBe(false);
        });
    });
});