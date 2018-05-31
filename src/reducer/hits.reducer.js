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

import * as types from "../actions.types";

const initialState = {
    currentHit: null,
    previousHits: [],
    nextHitReference: null
};

const loadHitRequest = (state, action) => {
    return Object.assign({}, state, {
        nextHitReference: action.reference
    });
};

const loadHitSuccess = (state, action) => {
    let previousHits = (state && state.previousHits instanceof Array)?state.previousHits:[];
    previousHits = (state && state.currentHit)?[...previousHits,state.currentHit]:[...previousHits];
    return Object.assign({}, state, {
        currentHit: action.data,
        previousHits: previousHits,
        nextHitReference: null
    });
};

const cancelHitLoading = (state, action) => {
    return Object.assign({}, state, {
        nextHitReference: null
    });
};

const setHit = (state, action) => {
    if (action.searchkit)
        action.searchkit.unlistenHistory();
    let previousHits = (state && state.previousHits instanceof Array)?state.previousHits:[];
    previousHits = (state && state.currentHit)?[...previousHits,state.currentHit]:[...previousHits];
    return Object.assign({}, state, {
        currentHit: action.data,
        previousHits: previousHits,
        nextHitReference: null
    });
};

const setPreviousHit = (state, action) => {
    if (state.currentHit) {
        const previousHits = (state && state.previousHits instanceof Array)?[...state.previousHits]:[];
        const currentHit = previousHits.pop() || null;
        return Object.assign({}, state, {
            currentHit: currentHit,
            previousHits: previousHits,
            nextHitReference: null
        });
    }

    return state;
};

const clearAllHits = (state, action) => {
    return Object.assign({}, state, {
        currentHit: null,
        previousHits: [],
        nextHitReference: null
    });
};

const setCurrentHitFromBrowserLocation = (state, action) => {
    let hit = state && state.currentHit;

    // if no current hit
    if (!hit)
      return state;

    const [,,hitType, hitId] = window.location.href.match(/#((.+)\/(.+))$/) || [];

    // no hit reference available in url, unset current hit
    if (!hitType || !hitId) {
        return Object.assign({}, state, {
            previousHits: [],
            currentHit: null
        });
    }

    // hit reference url is already matching current hit, do notthing
    if (hit && hit._type === hitType && hit._id === hitId) 
        return state;

    // no previous hits available, unset current hit
    if (!state || !state.previousHits.length) {
        return Object.assign({}, state, {
            previousHits: [],
            currentHit: null
        });
    }

    const previousHits = (state && state.previousHits instanceof Array)?[...state.previousHits]:[];
    hit = previousHits.pop() || null;
    while(previousHits.length && hit && !(hit._type === hitType && hit._id === hitId))  {
      hit = previousHits.pop();
    }
    if (hit && hit._type === hitType && hit._id === hitId) {
        return Object.assign({}, state, {
            previousHits: previousHits,
            currentHit: hit
        });
    }
    
    return Object.assign({}, state, {
        previousHits: [],
        currentHit: null
    });
}

export function reducer(state = initialState, action) {
    switch (action.type) {
        case types.LOAD_HIT_REQUEST:
            return loadHitRequest(state, action);
        case types.LOAD_HIT_SUCCESS:
            return loadHitSuccess(state, action);
        case types.CANCEL_HIT_LOADING:
            return cancelHitLoading(state, action);
        case types.SET_HIT:
            return setHit(state, action);
        case types.SET_PREVIOUS_HIT:
            return setPreviousHit(state, action);
        case types.CLEAR_ALL_HITS:
            return clearAllHits(state, action);
        case types.SET_CURRENT_HIT_FROM_BROWSER_LOCATION:
            return setCurrentHitFromBrowserLocation(state, action);
        default:
            return state;
      }
};