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

let srvSettings = null;

const initialize = settings => {
    srvSettings = settings;
};

const getConfig = () => {
    if (!srvSettings)
        throw new Error("search service has not been initalized");

    //return fetch(srvSettings.host + '/search/kg_labels/labels/labels')
    return fetch(srvSettings.host + '/proxy/kg_labels/labels/labels')
    .then(response => {
        if (!response.ok) 
            return Promise.reject(response.statusText);

        return response.json();
    })
    .then(data => {
        const source = data._source;
        const shapeMappings = source;

        const initQueryFieldsRec = (queryFields, shapeFields, parent) => {
            Object.keys(shapeFields).forEach(fieldName => {
                const field = shapeFields[fieldName];
                const fullFieldName = parent+fieldName;
                let queryField = queryFields[fullFieldName];
                if (!queryField) {
                    queryField = {boost: 1};
                    queryFields[fullFieldName] = queryField;
                }
    
                if (field && field.boost && field.boost > queryField.boost)
                    queryField.boost = field.boost;
    
                if (field["children"]!==undefined){
                    initQueryFieldsRec(queryFields, field["children"], fullFieldName+".children.");
                }
            });
        };

        let queryFields = {};
        Object.keys(source).forEach(shape => {
            const shapeFields = source[shape] && source[shape].fields;
            initQueryFieldsRec(queryFields, shapeFields, "");
        });
        queryFields = Object.keys(queryFields).map(fieldName => {
            const boost = queryFields[fieldName].boost;
            if (boost)
                return fieldName + ".value^" + boost;
            return fieldName + ".value"; 
        });
        let facetFields = {};
        let sortFields = {_score:{label:"Relevance", field:"_score", order:"desc", defaultOption:true}};
        Object.keys(source).forEach(type => {
            facetFields[type] = {};
            Object.keys(source[type].fields).forEach(fieldName => {
                const field = source[type].fields[fieldName];
                if(field.facet){
                    facetFields[type][fieldName] = {
                        filterType: field.facet,
                        filterOrder:field.facet_order,
                        exclusiveSelection: field.facet_exclusive_selection,
                        fieldType:  field.type,
                        fieldLabel: field.value,
                        isChild: false
                    }
                }
                if(field.children){
                    Object.keys(field.children).forEach(childName => {
                        const child = source[type].fields[fieldName].children[childName];
                        if(child.facet){
                            facetFields[type][fieldName+".children."+childName] = {
                                filterType: child.facet,
                                filterOrder:child.facet_order,
                                exclusiveSelection: field.facet_exclusive_selection,
                                fieldType:  child.type,
                                fieldLabel: child.value,
                                isChild: true,
                                path: fieldName+".children"
                            }
                        }
                    });
                }
                if(field.sort && sortFields[fieldName] === undefined){
                    sortFields[fieldName] = {label:field.value, field:fieldName+".value.keyword", order:"asc"}
                }
            });
        });

        sortFields = Object.values(sortFields);
        
        return {
            shapeMappings: shapeMappings,
            queryFields: queryFields,
            facetFields: facetFields,
            sortFields: sortFields
        };
    })
    .catch(error => { 
        if (Array.isArray(error))
            error.forEach(e => console.log(e));
        else 
            console.log(error);
        throw error;
    });
};

const getIndexes = () => {
    if (!srvSettings)
        throw new Error("search service has not been initalized");

    let options = null;
    if (srvSettings.accessToken) {
        options = { 
            method: 'get', 
            headers: new Headers({
            'Authorization': 'Bearer ' + srvSettings.accessToken
            })
        };
    }
    return fetch(srvSettings.host + '/auth/groups', options)
    .then(response => {
        if (!response.ok)
            throw response.statusText;

        return response.json();
    })
    .catch(error => { 
        if (Array.isArray(error))
            error.forEach(e => console.log(e));
        else 
            console.log(error);
        throw error;
    });
};

const getHitByReference = (reference, index) => {
    if (!srvSettings)
        throw new Error("search service has not been initalized");

    let options = null;
    if (srvSettings.accessToken && index) {
        options = { 
            method: 'get', 
            headers: new Headers({
              'Authorization': 'Bearer ' + srvSettings.accessToken,
              'index-hint': index
            })
        };
    }

    return fetch(srvSettings.host + '/proxy/kg/' + reference, options)
    .then(response => {
        if (!response.ok)
            throw response.statusText;

        return response.json();
    })
    .catch(error => { 
        if (Array.isArray(error))
            error.forEach(e => console.log(e));
        else 
            console.log(error);
        throw error;
    });
};

export const searchService = {
    initialize,
    getConfig,
    getIndexes,
    getHitByReference,
};
