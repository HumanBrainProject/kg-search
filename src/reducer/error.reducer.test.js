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
    describe('load config request', () => {
        it('should set message to null', () => {
            const state = {message: "test"};
            const action = actions.loadConfigRequest();
            const newState = errorReducer(state, action);
            expect(newState.message).toBe(null);
        });
    });
    describe('load config success', () => {
        it('should set message to null', () => {
            const state = {message: "test"};
            const action = actions.loadConfigSuccess(null);
            const newState = errorReducer(state, action);
            expect(newState.message).toBe(null);
        });
    });
    describe('load config failure', () => {
        it('should set message to non null', () => {
            const state = {message: null};
            const action = actions.loadConfigFailure("error");
            const newState = errorReducer(state, action);
            expect(newState.message).not.toBe(null);
        });
    });

    describe('load indexes request', () => {
        it('should set message to null', () => {
            const state = {message: "test"};
            const action = actions.loadIndexesRequest();
            const newState = errorReducer(state, action);
            expect(newState.message).toBe(null);
        });
    });
    describe('load indexes success', () => {
        it('should set message to null', () => {
            const state = {message: "test"};
            const action = actions.loadIndexesSuccess(null);
            const newState = errorReducer(state, action);
            expect(newState.message).toBe(null);
        });
    });
    describe('load indexes failure', () => {
        it('should set message to non null', () => {
            const state = {message: null};
            const action = actions.loadIndexesFailure("error");
            const newState = errorReducer(state, action);
            expect(newState.message).not.toBe(null);
        });
    });
    describe('load hit request', () => {
        it('should set message to null', () => {
            const state = {message: "test", currentHit: 567, previousHits:[234, 345, 456]};
            const action = actions.loadHitRequest(678);
            const newState = errorReducer(state, action);
            expect(newState.message).toBe(null);
        });
    });
    describe('load hit success', () => {
        it('should set message to null', () => {
            const state = {currentHit: 567, previousHits:[234, 345, 456]};
            const action = actions.loadHitSuccess(123);
            const newState = errorReducer(state, action);
            expect(newState.message).toBe(null);
        });
    });
    describe('load hit no data', () => {
        it('should set message to null', () => {
            const state = null;
            const action = actions.loadHitNoData(123);
            const newState = errorReducer(state, action);
            expect(newState.message).not.toBe(null);
        });
    });
    describe('load hit failure', () => {
        it('should set message to true', () => {
            const state = null;
            const action = actions.loadHitFailure("error");
            const newState = errorReducer(state, action);
            expect(newState.message).not.toBe(null);
        });
    });
    describe('cancel hit loading', () => {
        it('should set message to null', () => {
            const state = null;
            const action = actions.cancelHitLoading();
            const newState = errorReducer(state, action);
            expect(newState.message).toBe(null);
        });
    });
});