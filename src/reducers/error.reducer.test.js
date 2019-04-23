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
import { reducer as errorReducer} from "./error.reducer";
describe('confirguration reducer', () => {
    describe('unknown action', () => {
        it('should return same state', () => {
            const state = {a: {c: 1, d: 2}, b: [{e:3}, {e:4}]};
            const action = {type: "ABCDEFGH"};
            const newState = errorReducer(state, action);
            expect(JSON.stringify(newState)).toBe(JSON.stringify(state));
        });
    });
    describe('load definition request', () => {
        it('should set message to null', () => {
            const state = {message: "test"};
            const action = actions.loadDefinitionRequest();
            const newState = errorReducer(state, action);
            expect(newState.message).toBe(null);
        });
    });
    describe('load definition success', () => {
        it('should set message to null', () => {
            const state = {message: "test"};
            const action = actions.loadDefinitionSuccess(null);
            const newState = errorReducer(state, action);
            expect(newState.message).toBe(null);
        });
    });
    describe('load definition failure', () => {
        it('should set message to non null', () => {
            const state = {message: null};
            const action = actions.loadDefinitionFailure("error");
            const newState = errorReducer(state, action);
            expect(newState.message).not.toBe(null);
        });
    });

    describe('load groups request', () => {
        it('should set message to null', () => {
            const state = {message: "test"};
            const action = actions.loadGroupsRequest();
            const newState = errorReducer(state, action);
            expect(newState.message).toBe(null);
        });
    });
    describe('load groups success', () => {
        it('should set message to null', () => {
            const state = {message: "test"};
            const action = actions.loadGroupsSuccess(null);
            const newState = errorReducer(state, action);
            expect(newState.message).toBe(null);
        });
    });
    describe('load groups failure', () => {
        it('should set message to non null', () => {
            const state = {message: null};
            const action = actions.loadGroupsFailure("error");
            const newState = errorReducer(state, action);
            expect(newState.message).not.toBe(null);
        });
    });
    describe('load instance request', () => {
        it('should set message to null', () => {
            const state = {message: "test", currentInstance: 567, previousInstances:[234, 345, 456]};
            const action = actions.loadInstanceRequest(678);
            const newState = errorReducer(state, action);
            expect(newState.message).toBe(null);
        });
    });
    describe('load instance success', () => {
        it('should set message to null', () => {
            const state = {currentInstance: 567, previousInstances:[234, 345, 456]};
            const action = actions.loadInstanceSuccess(123);
            const newState = errorReducer(state, action);
            expect(newState.message).toBe(null);
        });
    });
    describe('load instance no data', () => {
        it('should set message to null', () => {
            const state = null;
            const action = actions.loadInstanceNoData(123);
            const newState = errorReducer(state, action);
            expect(newState.message).not.toBe(null);
        });
    });
    describe('load instance failure', () => {
        it('should set message to true', () => {
            const state = null;
            const action = actions.loadInstanceFailure("error");
            const newState = errorReducer(state, action);
            expect(newState.message).not.toBe(null);
        });
    });
    describe('cancel instance loading', () => {
        it('should set message to null', () => {
            const state = null;
            const action = actions.cancelInstanceLoading();
            const newState = errorReducer(state, action);
            expect(newState.message).toBe(null);
        });
    });
});