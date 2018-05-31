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
import { reducer as configurationReducer} from "./configuration.reducer";
describe('confirguration reducer', () => {
    describe('unknown action', () => {
        it('should return same state', () => {
            const state = {a: {c: 1, d: 2}, b: [{e:3}, {e:4}]};
            const action = {type: "ABCDEFGH"};
            const newState = configurationReducer(state, action);
            expect(JSON.stringify(newState)).toBe(JSON.stringify(state));
        });
    });
    describe('load config request', () => {
        it('should set is ready to false', () => {
            const state = null;
            const action = actions.loadConfigRequest();
            const newState = configurationReducer(state, action);
            expect(newState.isConfigReady).toBe(false);
        });
    });
    describe('load config success', () => {
        it('should set current config', () => {
            const state = null;
            const config = {foo: "bar"};
            const action = actions.loadConfigSuccess(config);
            const newState = configurationReducer(state, action);
            expect(newState.foo).toBe(config.foo);
        });
        it('should set is ready to true', () => {
            const state = {isConfigReady: false};
            const action = actions.loadConfigSuccess(null);
            const newState = configurationReducer(state, action);
            expect(newState.isConfigReady).toBe(true);
        });
    });
    describe('load config failure', () => {
        it('should set ready to false', () => {
            const state = {isConfigReady: true};
            const action = actions.loadConfigFailure("error");
            const newState = configurationReducer(state, action);
            expect(newState.isConfigReady).toBe(false);
        });
    });
});