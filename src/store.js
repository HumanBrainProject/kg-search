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

import { rootReducer } from "./reducer/root.reducer";

const createStore = reducer => {

    let state = reducer(null, {});

    document.addEventListener('action', function(e) {
        const nextState = reducer(state, e.detail);
        if (JSON.stringify(nextState) !== JSON.stringify(state)) {
            state = nextState;
            document.dispatchEvent(new CustomEvent('state'));
        }
    }, false);

    let store = {
        getState: () => {
            return state;
        }
    };

    const dispatch = (payload) => {
        setTimeout(() => {
            if (typeof payload === 'function') {
                payload(dispatch, store.getState);
            } else {
                document.dispatchEvent(
                    new CustomEvent('action', { detail: payload})
                );
            }
        });
    };

    store.dispatch = dispatch;
    return store;
}
export const store = createStore(rootReducer);

export const dispatch = store.dispatch;