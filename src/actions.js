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

import * as types from "./actions.types";
import {searchService} from "./search.service";

export const loadConfigRequest = accessToken => {  
    return {
        type: types.LOAD_CONFIG_REQUEST,
        accessToken: accessToken
    };
};

export const loadConfigSuccess = config => {  
    return {
        type: types.LOAD_CONFIG_SUCCESS, 
        config: config
    };
};

export const loadConfigFailure = error => {  
    return {
        type: types.LOAD_CONFIG_FAILURE, 
        error: error
    };
};

export const loadConfig = settings => {  
    return dispatch => {
        dispatch(loadConfigRequest(settings.accessToken));
        searchService.initialize(settings);
        searchService.getConfig()
        .then(config => {
            dispatch(loadConfigSuccess(config));
        })
        .catch(error => {
            dispatch(loadConfigFailure(error));
        });
    };
};

export const loadIndexesRequest = () => {  
    return {
        type: types.LOAD_INDEXES_REQUEST 
    };
};

export const loadIndexesSuccess = indexes => {  
    return {
        type: types.LOAD_INDEXES_SUCCESS, 
        indexes: indexes
    };
};

export const loadIndexesFailure = error => {  
    return {
        type: types.LOAD_INDEXES_FAILURE, 
        error: error
    };
};

export const loadIndexes = () => {
    return dispatch => {
        dispatch(loadIndexesRequest());
        searchService.getIndexes()
        .then(indexes => {
            dispatch(loadIndexesSuccess(indexes));
        })
        .catch(error => {
            dispatch(loadIndexesFailure(error));
        });
    };
}

export const loadSearchServiceFailure = (status, index) => {  
    return {
        type: types.LOAD_SEARCH_SERVICE_FAILURE,
        status: status,
        index: index
    };
};

export const loadSearchSessionFailure = status => {  
    return {
        type: types.LOAD_SEARCH_SESSION_FAILURE,
        status: status
    };
};

export const loadSearchRequest = () => {  
    return {
        type: types.LOAD_SEARCH_REQUEST
    };
};

export const loadSearchResult = results => {  
    return {
        type: types.LOAD_SEARCH_SUCCESS,
        results: results
    };
};

export const cancelSearch = () => {  
    return {
        type: types.CANCEL_SEARCH
    };
};

export const setIndex = index => {  
    return {
        type: types.SET_INDEX,
        index: index
    };
};

export const loadHitRequest = reference => {  
    return {
        type: types.LOAD_HIT_REQUEST, 
        reference: reference
    };
};

export const loadHitSuccess = data => {  
    return {
        type: types.LOAD_HIT_SUCCESS, 
        data: data
    };
};

export const loadHitNoData = reference => {  
    return {
        type: types.LOAD_HIT_NO_DATA, 
        reference: reference
    };
};

export const loadHitFailure = error => {   
    return {
        type: types.LOAD_HIT_FAILURE, 
        error: error
    };
};

export const loadHit = (reference, index) => {  
    return dispatch => {
        dispatch(loadHitRequest(reference));
        searchService.getHitByReference(reference, index)
        .then(data => {
            if (data.found)
                dispatch(loadHitSuccess(data));
            else
                dispatch(loadHitNoData(reference));
        })
        .catch(error => {
            dispatch(loadHitFailure(error));
        });
    };
};

export const setHit = data => {  
    return {
        type: types.SET_HIT, 
        data: data
    };
};

export const cancelHitLoading = () => {  
    return {
        type: types.CANCEL_HIT_LOADING
    };
};

export const setPreviousHit = () => {  
    return {
        type: types.SET_PREVIOUS_HIT
    };
};

export const clearAllHits = () => {  
    return {
        type: types.CLEAR_ALL_HITS
    };
};

export const setCurrentHitFromBrowserLocation = () => {  
    return {
        type: types.SET_CURRENT_HIT_FROM_BROWSER_LOCATION
    };
};