/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

import * as parser from "lucene-query-parser";

const queryTweaking = {
  wildcard: {
    maxNbOfTerms: 2, // -1 = apply on all terms, 0 = do not apply, positive number n = apply on first n terms
    minNbOfChars: 3 // nb of character under which wildcard is not applied
  },
  fuzzySearch: {
    maxNbOfTerms: 3, // -1 = apply on all terms, 0 = do not apply, positive number n = apply on first n terms
    minNbOfChars: 4 // nb of character under which fuzzy search is not applied
  },
  maxNbOfTermsTrigger: 4 // maximum number of terms before tweaking is turned off
};

export class ElasticSearchHelpers {

  static get listFacetDefaultSize() {
    return 10;
  }

  static get listFacetAllSize() {
    return 1000000000;
  }

  /*# sanitize a search query for Lucene. Useful if the original
          # query raises an exception, due to bad adherence to DSL.
          # Taken from here:
          #
          # http://stackoverflow.com/questions/16205341/symbols-in-query-string-for-elasticsearch
          #*/
  static sanitizeString = q => {

    // Escape special characters
    // http://lucene.apache.org/core/old_versioned_docs/versions/2_9_1/queryparsersyntax.html#Escaping Special Characters
    function escapeSpecialCharacters(str) {
      const spectialChars = "\\+-&|!(){}[]^~*?:/";
      const re = new RegExp("([" + spectialChars.split("").map(c => "\\" + c).join("") + "])", "g");
      return str.replace(re, "\\$1");
    }

    function trimOperators(str) {
      let res = str;
      res = res.replace(/^\s*&&\s*(.*)$/, "$1");
      res = res.replace(/^\s*\|\|\s*(.*)$/, "$1");
      res = res.replace(/^(.*)\s*&&\s*$/, "$1");
      res = res.replace(/^(.*)\s*\|\|\s*$/, "$1");
      res = res.trim();
      if (res !== str) {
        return trimOperators(res);
      }
      return res;
    }

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
          if (!node) {
            return;
          }

          if (node.boost || node.similarity || node.proximity) {
            return;
          }

          if (node.term && Number.isNaN(Number(node.term)) && !/.+\*$/.test(node.term) && !/.+\?$/.test(node.term) && !/\s/.test(node.term)) {
            terms.push(node.term.toLowerCase());
          }
        }

        if (!node) {
          return;
        }

        if (node.operator || node.left) {
          addTermsFromNodeExpresion(node, terms);
        } else if (node.term) {
          addTermsFromFieldExpression(node, terms);
        }
      }

      const terms = [];
      addTermsFromExpression(node, terms);
      return terms;
    }

    let str = q.trim().replace(/\s+/g, " ");
    str = trimOperators(str).toLowerCase();

    // Capitalize operator
    ["AND", "OR", "NOT"].forEach(op => {
      const re = new RegExp("([ \"\\[\\]{}()])" + op + "([ \"\\[\\]{}()])", "gi");
      str = str.replace(re, "$1" + op + "$2");
    });

    try {
      const tree = parser.parse(str);

      //Adds wildcard to the input box search term
      let maxWildcardConfig = Number(queryTweaking.wildcard.maxNbOfTerms);
      if (isNaN(maxWildcardConfig)) {
        maxWildcardConfig = -1;
      }
      maxWildcardConfig = Math.floor(maxWildcardConfig);
      let maxFuzzySearchConfig = Number(queryTweaking.fuzzySearch.maxNbOfTerms);
      if (isNaN(maxFuzzySearchConfig)) {
        maxFuzzySearchConfig = -1;
      }
      maxFuzzySearchConfig = Math.floor(maxFuzzySearchConfig);
      const terms = getTerms(tree);
      let maxWildcard = maxWildcardConfig < 0 ? terms.length : terms.length < maxWildcardConfig ? terms.length : maxWildcardConfig;
      let maxFuzzySearch = maxFuzzySearchConfig < 0 ? terms.length : terms.length < maxFuzzySearchConfig ? terms.length : maxFuzzySearchConfig;
      const filteredTerms = (terms.length === 1 ? terms : (terms.filter(term => {
        return (term.length >= queryTweaking.wildcard.minNbOfChars || term.length >= queryTweaking.fuzzySearch.minNbOfChars) &&
          !term.includes(".") &&
          !["a", "above", "all", "an", "are", "as", "any", "because", "below", "besides", "but", "by", "eg", "either", "for", "hence", "how", "which", "where", "who", "ie", "in", "instead", "is", "none", "of", "one", "other", "over", "same", "that", "the", "then", "thereby", "therefore", "this", "though", "thus", "to", "under", "until", "when", "why"].includes(term);
      }))).filter(term => {
        const escapedTerm = escapeSpecialCharacters(term);
        if (escapedTerm !== term) {
          const reg = new RegExp(escapedTerm, "g");
          str = str.replace(reg, escapedTerm);
          return false;
        }
        return true;
      }).filter(term => {
        return !str.includes(`"${term}"`);
      });
      if (terms.length <= queryTweaking.maxNbOfTermsTrigger) {
        filteredTerms.forEach((term, idx) => {
          const wildcardCondition = terms.length === 1 || (idx < maxWildcard && term.length >= queryTweaking.wildcard.minNbOfChars);
          const fuzzySearchCondition = idx < maxFuzzySearch && term.length >= queryTweaking.fuzzySearch.minNbOfChars;
          if (wildcardCondition || fuzzySearchCondition) {
            const re1 = new RegExp("([ \"\\[\\]{}()][+\\-]?)" + term + "([ \"\\[\\]{}()])", "gi");
            const re2 = new RegExp("^([+\\-]?)" + term + "([ \"\\[\\]{}()])", "gi");
            const re3 = new RegExp("([ \"\\[\\]{}()][+\\-]?)" + term + "$", "gi");
            const re4 = new RegExp("^([+\\-]?)" + term + "$", "gi");
            if (wildcardCondition && fuzzySearchCondition) {
              str = str.replace(re1, "$1(" + term + "* OR " + term + "* OR " + term + "~)$2");
              str = str.replace(re2, "$1(" + term + "* OR " + term + "* OR " + term + "~)$2");
              str = str.replace(re3, "$1(" + term + "* OR " + term + "* OR " + term + "~)");
              str = str.replace(re4, "$1(" + term + "* OR " + term + "* OR " + term + "~)");
            } else if (wildcardCondition) {
              str = str.replace(re1, "$1" + term + "*$2");
              str = str.replace(re2, "$1" + term + "*$2");
              str = str.replace(re3, "$1" + term + "*");
              str = str.replace(re4, "$1" + term + "*");
            } else if (fuzzySearchCondition) {
              str = str.replace(re1, "$1" + term + "~$2");
              str = str.replace(re2, "$1" + term + "~$2");
              str = str.replace(re3, "$1" + term + "~");
              str = str.replace(re4, "$1" + term + "~");
            }
          }
        });
      }
    } catch (e) {
      //Special character is not supported in parser
      // try minimal replacements:
      str = escapeSpecialCharacters(str);
    }
    return str;
  };

  static getFacetTypesOrder = definition => {
    const facetTypesOrder = {};
    Object.entries(definition).forEach(([type, typeDefinition]) => {
      const order = Number(typeDefinition.order);
      if (!isNaN(order)) {
        facetTypesOrder[type] = order;
      }
    });
    return facetTypesOrder;
  };

  static getDefaultSelectedType = (definition, facetTypesOrder) => {
    let selectedType = null;
    let defaultSelectionDefined = false;
    Object.keys(definition).forEach(type => {
      const order = Number(definition[type].order);
      if (!isNaN(order)) {
        facetTypesOrder[type] = order;
        if (definition[type].defaultSelection) {
          selectedType = type;
          defaultSelectionDefined = true;
        }
        if (!defaultSelectionDefined && (!selectedType || facetTypesOrder[type] < facetTypesOrder[selectedType])) {
          selectedType = type;
        }
      }
    });
    return selectedType;
  };

  static getSortFields = definition => {
    let sortFields = {
      _score: {
        key: "newestFirst",
        param: "newestFirst",
        label: "Relevance",
        fields: [{
          _score: {
            order: "desc"
          }
        },
        {
          "first_release.value": {
            order: "desc",
            missing: "_last"
          }
        }
        ],
        defaultOption: true
      }
    };
    Object.values(definition).forEach(typeDefinition => {
      Object.entries(typeDefinition.fields).forEach(([fieldName, field]) => {
        if (field.sort && sortFields[fieldName] === undefined) {
          const key = `${fieldName}.value.keyword`;
          const res = {};
          res[key] = "asc";
          sortFields[fieldName] = {
            key: field.value,
            label: field.value,
            fields: [res],
            param: `${key}_${res[key]}`
          };
        }
      });
    });
    return Object.values(sortFields);
  };

  static constructFacets = definition => {
    const facets = [];
    Object.entries(definition).forEach(([type, typeDefinition]) => {
      Object.entries(typeDefinition.fields).forEach(([name, field]) => {
        if (field.facet) {
          facets.push({
            id: "facet_" + type + "_" + name,
            name: name,
            type: type,
            filterType: field.facet,
            filterOrder: field.facetOrder,
            exclusiveSelection: field.facetExclusiveSelection,
            fieldType: field.type,
            fieldLabel: field.value,
            isChild: false,
            isFilterable: field.isFilterableFacet,
            count: 0,
            value: null,
            keywords: [],
            size: field.isFilterableFacet?ElasticSearchHelpers.listFacetAllSize:ElasticSearchHelpers.listFacetDefaultSize,
            defaultSize: field.isFilterableFacet?ElasticSearchHelpers.listFacetAllSize:ElasticSearchHelpers.listFacetDefaultSize
          });
        }
        if (field.children) {
          Object.entries(field.children).forEach(([childName, child]) => {
            if (child.facet) {
              if (child.isHierarchicalFacet) {
                facets.push({
                  id: "facet_" + type + "_" + name + ".children." + childName,
                  name: name,
                  type: type,
                  filterType: child.facet,
                  filterOrder: child.facetOrder,
                  exclusiveSelection: field.facetExclusiveSelection,
                  fieldType: child.type,
                  fieldLabel: field.value,
                  isChild: true,
                  isHierarchical: true,
                  isFilterable: false,
                  path: name + ".children",
                  childName: childName,
                  count: 0,
                  value: null,
                  keywords: [],
                  size: ElasticSearchHelpers.listFacetAllSize,
                  defaultSize: ElasticSearchHelpers.listFacetAllSize,
                  missingTerm: field.facetMissingTerm?field.facetMissingTerm:"Others"
                });
              } else {
                facets.push({
                  id: "facet_" + type + "_" + name + ".children." + childName,
                  name: name,
                  type: type,
                  filterType: child.facet,
                  filterOrder: child.facetOrder,
                  exclusiveSelection: field.facetExclusiveSelection,
                  fieldType: child.type,
                  fieldLabel: child.value,
                  isChild: true,
                  isHierarchical: false,
                  isFilterable: false,
                  path: name + ".children",
                  childName: childName,
                  count: 0,
                  value: null,
                  keywords: [],
                  size: ElasticSearchHelpers.listFacetDefaultSize,
                  defaultSize: ElasticSearchHelpers.listFacetDefaultSize
                });
              }
            }
          });
        }
      });
    });
    return facets;
  };

  static getQueryFieldsByType = definition => {

    const addQueryFields = (queryFields, fields, parentPath) => {
      Object.entries(fields).forEach(([field, mapping]) => {
        if (!mapping.ignoreForSearch) {
          const path = `${parentPath}${field}`;
          const name = `${path}.value`;
          queryFields[name] = {
            boost: mapping.boost??1,
            highlight: [
              "title.value",
              "description.value",
              "contributors.value",
              "owners.value",
              "component.value",
              "created_at.value",
              "releasedate.value",
              "activities.value"
            ].includes(name) // temporary mapping.highlight
          };
          if (mapping.children !== undefined) {
            addQueryFields(queryFields, mapping.children, path + ".children.");
          }
        }
      });
    };

    const queryFieldsByType = {};
    if (definition) {
      Object.entries(definition).forEach(([type, typeMapping]) => {
        const queryFields = {};
        addQueryFields(queryFields, typeMapping.fields, "");
        queryFieldsByType[type] = queryFields;
      });
    }
    return queryFieldsByType;
  }

  static getQueryFields = (queryFieldsByType, selectedType) => {
    if (!queryFieldsByType) {
      return [];
    }
    let queryFields = {};
    Object.entries(queryFieldsByType).forEach(([type, fields]) => {
      if (type !== selectedType) {
        Object.assign(queryFields, fields);
      }
    });
    // selected type fields override others
    if (selectedType && queryFieldsByType[selectedType]) {
      Object.assign(queryFields, queryFieldsByType[selectedType]);
    }
    return Object.entries(queryFields).map(([name, field]) => `${name}^${field.boost}`).sort();
  };

  static getcustomHighlightFields = (queryFieldsByType, selectedType) => {
    if (selectedType && queryFieldsByType[selectedType]) {
      const highlightFields = Object.entries(queryFieldsByType[selectedType]).reduce((acc, [name, field]) => {
        if (field.highlight) {
          acc[name] = {}; // field
        }
        return acc;
      }, {});
      return Object.keys(highlightFields).length?highlightFields:null;
    }
    return null;
  }

  static buildRequest(searchState) {

    const queryString = searchState.queryString;
    const selectedType = searchState.selectedType;
    const queryFields = ElasticSearchHelpers.getQueryFields(searchState.queryFieldsByType, selectedType);
    const facets = searchState.facets;
    const sort = searchState.sort;
    const from = searchState.from;
    const size = searchState.hitsPerPage;
    const customHighlightFields = ElasticSearchHelpers.getcustomHighlightFields(searchState.queryFieldsByType, selectedType);

    const typeFacet = {
      id: "facet_type",
      name: "type.value",
      filterType: "type",
      value: selectedType
    };

    const queryFacets = [...facets, typeFacet];

    const getAllFilters = facets => {
      const filters = {};

      const buildFilter = (facet, key, value) => {
        const term = {};
        term[key] = value;
        if (facet.isChild) {
          if (facet.isHierarchical) {
            return {
              term: term
            };
          }

          return {
            nested: {
              path: `${facet.name}.children`,
              query: {
                term: term
              }
            }
          };
        }

        return {
          term: term
        };
      };

      facets.forEach(facet => {
        let filter = null;
        const facetKey = facet.isChild ?(facet.isHierarchical?`${facet.childName}.value.keyword`:`${facet.name}.children.${facet.childName}.value.keyword`):`${facet.name}.value.keyword`;
        switch (facet.filterType) {
        case "type":
        {
          filter = {
            term: {}
          };
          filter.term[facet.name] = facet.value;
          break;
        }
        case "list":
        {
          if (Array.isArray(facet.value) && facet.value.length) {
            if (facet.exclusiveSelection === false) { // OR
              if (facet.value.length > 1) {
                filter = {
                  bool: {
                    should: []
                  }
                };
                facet.value.forEach(v => {
                  filter.bool.should.push(buildFilter(facet, facetKey, v));
                });
              } else {
                filter = buildFilter(facet, facetKey, facet.value[0]);
              }
            } else { // AND
              filter = [];
              facet.value.forEach(v => {
                filter.push(buildFilter(facet, facetKey, v));
              });
            }
          }
          break;
        }
        case "exists":
        {
          filter = {
            exists: {
              field: facetKey
            }
          };
          break;
        }
        default:
          break;
        }
        if (filter) {
          filters[facet.id] = {
            facet: facet,
            filter: filter
          };
        }
      });
      return filters;
    };

    const setFilters = (filters, key) => {
      const filtered = Object.entries(filters).filter(([id, { facet }]) => {
        const active = !!facet.value;
        switch (facet.filterType) {
        case "exists":
          if (id === key) {
            return true;
          }
          return active && id !== key;
        case "type":
        case "list":
        default:
          return active && id !== key;
        }
      });
      const res = filtered.reduce((acc, [, { filter }]) => {
        if (Array.isArray(filter)) {
          acc.push(...filter);
        } else {
          acc.push(filter);
        }
        return acc;
      }, []);
      if (res.length > 1) {
        return {
          bool: {
            must: res
          }
        };
      } else if (res.length === 1) {
        return res[0];
      }
      return {
        match_all: {}
      };
    };

    const setFacetAggs = (aggs, facets) => {

      const setAggs = (key, count, orderDirection, size) => {
        const aggs = {};
        aggs[key] = {
          terms: {
            field: key,
            size: size
          }
        };
        if (orderDirection) {
          aggs[key].terms.order = {
            _count: orderDirection
          };
        }
        aggs[count] = {
          cardinality: {
            field: key
          }
        };
        return aggs;
      };

      const setListFacetAggs = (aggs, facet) => {

        const orderKey = facet.filterOrder && facet.filterOrder === "byvalue" ? "_term" : "_count";
        const orderDirection = orderKey === "_term" ? "asc" : "desc";

        if (facet.isChild) {
          if (facet.isHierarchical) {
            const key = `${facet.name}.value.keyword`;
            const count = `${facet.name}.value.keyword_count`;
            aggs[facet.id] = {
              aggs: setAggs(key, count, orderDirection, facet.size)
            };
            aggs[facet.id].aggs[key].terms.missing = facet.missingTerm;
            const subKey = `${facet.childName}.value.keyword`;
            const subCount = `${facet.childName}.value.keyword_count`;
            aggs[facet.id].aggs[key].aggs  = setAggs(subKey, subCount, orderDirection, facet.size);
          } else {
            const key = `${facet.name}.children.${facet.childName}.value.keyword`;
            const count = `${facet.name}.children.${facet.childName}.value.keyword_count`;
            aggs[facet.id] = {
              aggs: {
                inner: {
                  aggs: setAggs(key, count, orderDirection, facet.size),
                  nested: {
                    path: `${facet.name}.children`
                  }
                }
              }
            };
          }
        } else {
          const key = `${facet.name}.value.keyword`;
          const count = `${facet.name}.value.keyword_count`;
          aggs[facet.id] = {
            aggs: setAggs(key, count, orderDirection, facet.size)
          };
        }
      };

      facets.forEach(facet => {
        switch (facet.filterType) {
        case "type":
        {
          aggs[facet.id] = {
            aggs: setAggs(facet.name, `${facet.name}_count`, null, 50)
          };
          break;
        }
        case "list":
        {
          setListFacetAggs(aggs, facet);
          break;
        }
        case "exists":
        default:
          aggs[facet.id] = {};
          break;
        }
      });
    };

    const getAggs = (facets, allFilters) => {

      const setFacetFilter = (aggs, facets, facetFilters) => {
        facets.forEach(facet => {
          const filters = setFilters(facetFilters, facet.id);
          if (filters) {
            aggs[facet.id].filter = filters;
          } else {
            aggs[facet.id].filter = {
              match_all: {}
            };
          }
        });
      };

      const aggs = {};
      setFacetAggs(aggs, facets);
      setFacetFilter(aggs, facets, allFilters);

      const typeFilters = setFilters(allFilters, "facet_type");
      if (typeFilters) {
        aggs.facet_type.filter = typeFilters;
      } else {
        aggs.facet_type.filter = {
          match_all: {}
        };
      }
      return aggs;
    };

    const query = queryString ? {
      query_string: {
        query: ElasticSearchHelpers.sanitizeString(queryString),
        lenient: true,
        analyze_wildcard: true
      }
    } : null;
    if (query && query.query_string && queryFields.length) {
      query.query_string.fields = queryFields;
    }

    const allFilters = getAllFilters(queryFacets);

    const payload = {
      aggs: getAggs(queryFacets.filter(facet => facet.type === selectedType || facet.filterType === "type"), allFilters),
      from: from,
      post_filter: setFilters(allFilters),
      size: size
    };

    if (customHighlightFields) {
      payload.highlight = {
        fields: customHighlightFields,
        encoder: "html"
      };
    }

    if (sort && Array.isArray(sort.fields)) {
      payload.sort = sort.fields;
    }

    if (query) {
      payload.query = query;
    }
    return payload;
  }
}