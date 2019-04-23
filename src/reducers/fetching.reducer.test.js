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
import { reducer as fetchingReducer} from "./fetching.reducer";
describe('confirguration reducer', () => {
    describe('unknown action', () => {
        it('should return same state', () => {
            const state = {a: {c: 1, d: 2}, b: [{e:3}, {e:4}]};
            const action = {type: "ABCDEFGH"};
            const newState = fetchingReducer(state, action);
            expect(JSON.stringify(newState)).toBe(JSON.stringify(state));
        });
    });
    describe('load definition request', () => {
        it('should set is active to true', () => {
            const state = {active: false};
            const action = actions.loadDefinitionRequest();
            const newState = fetchingReducer(state, action);
            expect(newState.active).toBe(true);
        });
    });
    describe('load definition success', () => {
        it('should set is active to false', () => {
            const state = {active: true};
            const action = actions.loadDefinitionSuccess(null);
            const newState = fetchingReducer(state, action);
            expect(newState.active).toBe(false);
        });
    });
    describe('load definition failure', () => {
        it('should set is active to false', () => {
            const state = {active: true};
            const action = actions.loadDefinitionFailure("error");
            const newState = fetchingReducer(state, action);
            expect(newState.active).toBe(false);
        });
    });

    describe('load groups request', () => {
        it('should set is active to true', () => {
            const state = {active: false};
            const action = actions.loadGroupsRequest();
            const newState = fetchingReducer(state, action);
            expect(newState.active).toBe(true);
        });
    });
    describe('load groups success', () => {
        it('should set is active to false', () => {
            const state = {active: true};
            const action = actions.loadGroupsSuccess(null);
            const newState = fetchingReducer(state, action);
            expect(newState.active).toBe(false);
        });
    });
    describe('load groups failure', () => {
        it('should set is active to false', () => {
            const state = {active: true};
            const action = actions.loadGroupsFailure("error");
            const newState = fetchingReducer(state, action);
            expect(newState.active).toBe(false);
        });
    });
    describe('load search request', () => {
        it('should set is active to true', () => {
            const state = {active: false};
            const action = actions.loadSearchRequest();
            const newState = fetchingReducer(state, action);
            expect(newState.active).toBe(true);
        });
    });
    describe('load search result', () => {
        it('should set is active to false', () => {
            const state = {active: true};
            const result = "foo";
            const action = actions.loadSearchResult(result);
            const newState = fetchingReducer(state, action);
            expect(newState.active).toBe(false);
        });
    });
    describe('load instance request', () => {
        it('should set is active to true', () => {
            const state = {active: false, requestReference: 678, currentInstance: 567, previousInstances:[234, 345, 456]};
            const action = actions.loadInstanceRequest();
            const newState = fetchingReducer(state, action);
            expect(newState.active).toBe(true);
        });
    });
    describe('load instance success', () => {
        it('should set is active to false', () => {
            const state = {active: true, currentInstance: 567, previousInstances:[234, 345, 456]};
            const action = actions.loadInstanceSuccess(123);
            const newState = fetchingReducer(state, action);
            expect(newState.active).toBe(false);
        });
    });
    describe('load instance no data', () => {
        it('should set is active to false', () => {
            const state = {active: true};
            const action = actions.loadInstanceNoData(123);
            const newState = fetchingReducer(state, action);
            expect(newState.active).toBe(false);
        });
    });
    describe('load instance failure', () => {
        it('should set is active to false', () => {
            const state = {active: true};
            const action = actions.loadInstanceFailure("error");
            const newState = fetchingReducer(state, action);
            expect(newState.active).toBe(false);
        });
    });
    describe('cancel instance loading', () => {
        it('should set is active to false', () => {
            const state = {active: true};
            const action = actions.cancelInstanceLoading();
            const newState = fetchingReducer(state, action);
            expect(newState.active).toBe(false);
        });
    });
});