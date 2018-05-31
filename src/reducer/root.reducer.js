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

import { reducer as configuration } from "./configuration.reducer";
import { reducer as search } from "./search.reducer";
import { reducer as hits } from "./hits.reducer";
import { reducer as error } from "./error.reducer";
import { reducer as fetching } from "./fetching.reducer";

const combineReducers = reducers => {
    return (state, action) => {
        let nextState = {};
        Object.keys(reducers)
        .map(k => {
            return {p: k, reducer: reducers[k]};
        })
        .forEach(({p, reducer}) => {
            nextState[p] = reducer(state?state[p]:undefined, action);
        });
        return nextState;
    }
};

export const rootReducer = combineReducers({
    configuration: configuration,
    search: search,
    hits: hits,
    error: error,
    fetching: fetching
});