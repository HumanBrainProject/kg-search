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
        isIndexesReady: true,
        indexes: [],
        isConfigReady: false,
        shapeMappings: {},
        queryFields: ["title", "description"],
        facetFields: [],
        sortFields: []
};

const loadConfigRequest = (state, action) => {
    return Object.assign({}, state, { 
        isConfigReady: false,
        isIndexesReady: !action.accessToken
    });
};

const loadConfigSuccess  = (state, action) => {
    return Object.assign({}, state, { 
        isConfigReady: true
    },
    action.config);
};

const loadConfigFailure  = (state, action) => {
    return Object.assign({}, state, { 
        isConfigReady: false
    });
};

const loadIndexesRequest = (state, action) => {
    return Object.assign({}, state, { 
        isIndexesReady: false
    });
};

const loadIndexesSuccess  = (state, action) => {

    let indexes = (action.indexes instanceof Array)?[...action.indexes.map(e => ({label: e, value: e}))]:[];

    return Object.assign({}, state, { 
        isIndexesReady: true,
        indexes: indexes
    });
};

const loadIndexesFailure  = (state, action) => {
    return Object.assign({}, state, { 
        isIndexesReady: false,
        hasIndexesError: true, // action.error
        isIndexesLoading: false,
    });
};

export function reducer(state = initialState, action) {
    switch (action.type) {
        case types.LOAD_CONFIG_REQUEST:
            return loadConfigRequest(state, action);
        case types.LOAD_CONFIG_SUCCESS:
            return loadConfigSuccess(state, action);
        case types.LOAD_CONFIG_FAILURE:
            return loadConfigFailure(state, action);
        case types.LOAD_INDEXES_REQUEST:
            return loadIndexesRequest(state, action);
        case types.LOAD_INDEXES_SUCCESS:
            return loadIndexesSuccess(state, action);
        case types.LOAD_INDEXES_FAILURE:
            return loadIndexesFailure(state, action);
        default:
            return state;
      }
};