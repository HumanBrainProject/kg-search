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

import * as parser from "lucene-query-parser";
//import { store } from "../store";

export class SearchKitHelpers {
    /*# sanitize a search query for Lucene. Useful if the original
    # query raises an exception, due to bad adherence to DSL.
    # Taken from here:
    #
    # http://stackoverflow.com/questions/16205341/symbols-in-query-string-for-elasticsearch
    #*/
    static sanitizeString(q, enableAutoWildcardAndFuzzySearch) {

        function getTerms(node) {
            function addTermsFromExpression(node, terms) {

                function addTermsFromNodeExpresion(node, terms) {
                    /*
                    { 
                        'left' : dictionary,     // field expression or node
                        'operator': string,      // operator value
                        'right': dictionary,     // field expression OR node 
                        'field': string          // field name (for field group syntax) [OPTIONAL]
                    }
                    */
                    addTermsFromExpression(node.left, terms);
                    addTermsFromExpression(node.right, terms);
                }

                function addTermsFromFieldExpression(node, terms) {
                    /*
                    {
                        'field': string,         // field name
                        'term': string,          // term value
                        'prefix': string         // prefix operator (+/-) [OPTIONAL]
                        'boost': float           // boost value, (value > 1 must be integer) [OPTIONAL]
                        'similarity': float      // similarity value, (value must be > 0 and < 1) [OPTIONAL]
                        'proximity': integer     // proximity value [OPTIONAL]
                    }
                    */
                    if (!node)
                        return;

                    if (node.boost || node.similarity || node.proximity)
                        return;

                    if (node.term && Number.isNaN(Number(node.term)) && !/.+\*$/.test(node.term) && !/.+\?$/.test(node.term) && !/\s/.test(node.term))
                        terms.push(node.term); 
                }

                if (!node)
                    return;

                if (node.operator || node.left)
                    addTermsFromNodeExpresion(node, terms);
                else if (node.term)
                    addTermsFromFieldExpression(node, terms);
            }

            const terms = [];
            addTermsFromExpression(node, terms);
            return terms;
        }

        let str = q.trim().replace(/\s+/g, ' ');
        
        // Capitalize operator
        ['AND', 'OR', 'NOT'].forEach(op => {
            const re = new RegExp('([ "\\[\\]{}()])' + op + '([ "\\[\\]{}()])', "gi");
            str = str.replace(re, "$1" + op + "$2");
        });

        try {
            const tree = parser.parse(str);

            //Adds wildcard to the input box search term
            if (enableAutoWildcardAndFuzzySearch) {          
                const terms = getTerms(tree);
                terms.forEach(term => {
                    const re1 = new RegExp('([ "\\[\\]{}()][+\\-]?)' + term + '([ "\\[\\]{}()])', "g");
                    const re2 = new RegExp('^([+\\-]?)' + term + '([ "\\[\\]{}()])', "g");
                    const re3 = new RegExp('([ "\\[\\]{}()][+\\-]?)' + term + '$', "g");
                    const re4 = new RegExp('^([+\\-]?)' + term + '$', "g");
                    str = str.replace(re1, "$1(" + term + "* OR " + term + "~)$2");
                    str = str.replace(re2, "$1(" + term + "* OR " + term + "~)$2");
                    str = str.replace(re3, "$1(" + term + "* OR " + term + "~)");
                    str = str.replace(re4, "$1(" + term + "* OR " + term + "~)");
                });   
            } 
        } catch (e) {

            //Special character is not supported in parser
            // try minimal replacements:

            // Escape special characters
            // http://lucene.apache.org/core/old_versioned_docs/versions/2_9_1/queryparsersyntax.html#Escaping Special Characters
            const spectialChars = "\\+-&|!(){}[]^~*?:/";
            const re = new RegExp("([" + spectialChars.split('').map(c => "\\" + c).join('') + "])", "g");
            str = str.replace(re, '\\$1');

            //When odd quotes escape last one
            if (((str.match(/"/g) || []).length - (str.match(/\\"/g) || []).length) %2 === 1) 
                str = str.match(/(.*)"([^"]*)$/).reduce((r, s, i) => i===0?'':i===1?r+s+'\\"':r+s, '');
        }
        return str;
    }
    static getQueryProcessor(searchkit, enableAutoWildcardAndFuzzySearch, onBeforeQuerySearchEventHandler) {

        /*
        function buildTypeBoostsQuery() {
            const state = store.getState();
            return (Object.keys(state.configuration.shapeMappings) || []).map(shape => ({
                term: {
                    _type: {
                        value: shape,
                        boost: state.configuration.shapeMappings[shape].boost
                    }
                }
            }));
        }
        */
        
        return plainQueryObject => {

            onBeforeQuerySearchEventHandler && onBeforeQuerySearchEventHandler();

            //if not type is selected we make sure no other filters are active.
            const selectedType = searchkit.state.facet_type && searchkit.state.facet_type.length > 0 ? searchkit.state.facet_type[0]: "";
        
            if(!selectedType){
                let activeFilter = false;
                const activeAccessors = searchkit.accessors.getActiveAccessors();
                for(let i = 0; i < activeAccessors.length; i++){
                    const accessor = activeAccessors[i];
                    if(accessor.key !== undefined && accessor.state !== undefined){
                        let hasValue = false;
                        if(accessor.state.value !== null){
                            if(typeof accessor.state.value === "object"){
                                if(Object.keys(accessor.state.value).length > 0){
                                    hasValue = true;
                                }
                            } else {
                                hasValue = true;
                            }
                        }
                        activeFilter = (accessor.key).match(/^facet_/g) && hasValue;
                            if(activeFilter){
                                break;
                            }
                    }
                }
                
                if(activeFilter){
                    searchkit.getQueryAccessor().keepOnlyQueryState();
                    searchkit.query = searchkit.buildQuery();
                    plainQueryObject = searchkit.query.getJSON();
                }
            }
            if (plainQueryObject) {
                if (plainQueryObject.query) {
                    if (plainQueryObject.query.simple_query_string && plainQueryObject.query.simple_query_string.query) {
                        plainQueryObject.query.simple_query_string.query = SearchKitHelpers.sanitizeString(plainQueryObject.query.simple_query_string.query, enableAutoWildcardAndFuzzySearch);
                    }
                    if (plainQueryObject.query.query_string && plainQueryObject.query.query_string.query) {
                        plainQueryObject.query.query_string.query = SearchKitHelpers.sanitizeString(plainQueryObject.query.query_string.query, enableAutoWildcardAndFuzzySearch);
                        plainQueryObject.query.query_string.lenient = true; //Makes ES ignore search on non text fields
                    }
                } 
                /*
                const typeBoosts = buildTypeBoostsQuery();
                if (typeBoosts.length) {
                    plainQueryObject.query = {
                        bool: {
                            should: plainQueryObject.query?[plainQueryObject.query, ...typeBoosts]:typeBoosts
                        }
                    };
                } 
                */
            }

            return plainQueryObject;
        };
    }    
}
 