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
import { reducer as hitsReducer} from "./hits.reducer";
describe('hits reducer', () => {
    describe('unknown action', () => {
        it('should return same state', () => {
            const state = {a: {c: 1, d: 2}, b: [{e:3}, {e:4}]};
            const action = {type: "ABCDEFGH"};
            const newState = hitsReducer(state, action);
            expect(JSON.stringify(newState)).toBe(JSON.stringify(state));
        });
    });
    describe('load hit request', () => {
        it('should set next hit reference', () => {
            const state = {nextHitReference: null, currentHit: 567, previousHits:[234, 345, 456]};
            const action = actions.loadHit(678);
            const newState = hitsReducer(state, action);
            expect(newState.nextHitReference).toBe(678);
        });
    });
    describe('load hit success', () => {
        it('should set current hit', () => {
            const state = null;
            const action = actions.loadHitSuccess(123);
            const newState = hitsReducer(state, action);
            expect(newState.currentHit).toBe(123);
        });
        it('should reset next hit reference', () => {
            const state = {nextHitReference: 678, currentHit: 567, previousHits:[234, 345, 456]};
            const action = actions.loadHitSuccess(678);
            const newState = hitsReducer(state, action);
            expect(newState.nextHitReference).toBe(null);
        });
        it('should increase previous hits array length', () => {
            const state = {currentHit: 567, previousHits:[234, 345, 456]};
            const action = actions.loadHitSuccess(123);
            const newState = hitsReducer(state, action);
            expect(newState.previousHits.length).toBe(4);
        });
        it('should put previous hit into previous hits array', () => {
            const state = {currentHit: 567, previousHits:[234, 345, 456]};
            const action = actions.loadHitSuccess(123);
            const newState = hitsReducer(state, action);
            expect(newState.previousHits[3]).toBe(567);
        });
    });
    describe('cancel hit loading', () => {
        it('should set next hit reference to null', () => {
            const state = {nextHitReference: 678, currentHit: 567, previousHits:[234, 345, 456]};
            const action = actions.cancelHitLoading();
            const newState = hitsReducer(state, action);
            expect(newState.nextHitReference).toBe(null);
        });
    });
    describe('set hit', () => {
        it('should set current hit', () => {
            const state = null;
            const action = actions.setHit(123);
            const newState = hitsReducer(state, action);
            expect(newState.currentHit).toBe(123);
        });
        it('should reset next hit reference', () => {
            const state = {nextHitReference: 678, currentHit: 567, previousHits:[234, 345, 456]};
            const action = actions.setHit(678);
            const newState = hitsReducer(state, action);
            expect(newState.nextHitReference).toBe(null);
        });
        it('should increase previous hits array length', () => {
            const state = {currentHit: 567, previousHits:[234, 345, 456]};
            const action = actions.setHit(123);
            const newState = hitsReducer(state, action);
            expect(newState.previousHits.length).toBe(4);
        });
        it('should put previous hit into previous hits array', () => {
            const state = {currentHit: 567, previousHits:[234, 345, 456]};
            const action = actions.setHit(123);
            const newState = hitsReducer(state, action);
            expect(newState.previousHits[3]).toBe(567);
        });
    });
    describe('set previous hit', () => {
        it('should set last item of previous hits array as current hit', () => {
            const state = {currentHit: 567, previousHits:[234, 345, 456]};
            const action = actions.setPreviousHit();
            const newState = hitsReducer(state, action);
            expect(newState.currentHit).toBe(456);
        });
        it('should set current hit as null when previous hits array is empty', () => {
            const state = {currentHit: 567, previousHits:[]};
            const action = actions.setPreviousHit();
            const newState = hitsReducer(state, action);
            expect(newState.currentHit).toBe(null);
        });
    });
    describe('clear all hits', () => {
        it('should set current hit to null', () => {
            const state = {currentHit: 567, previousHits:[234, 345, 456]};
            const action = actions.clearAllHits();
            const newState = hitsReducer(state, action);
            expect(newState.currentHit).toBe(null);
        });
        it('should set previous hits array empty', () => {
            const state = {currentHit: 567, previousHits:[]};
            const action = actions.clearAllHits();
            const newState = hitsReducer(state, action);
            expect(newState.previousHits.length).toBe(0);
        });
        it('should set next hit reference to null', () => {
            const state = {nextHitReference: 678, currentHit: 567, previousHits:[234, 345, 456]};
            const action = actions.clearAllHits();
            const newState = hitsReducer(state, action);
            expect(newState.nextHitReference).toBe(null);
        });
    });
});