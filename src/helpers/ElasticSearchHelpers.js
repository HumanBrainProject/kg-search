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

const defaultCustomHighlight = {
  "fields": {
    "title.value": {},
    "description.value": {},
    "contributors.value": {},
    "owners.value": {},
    "component.value": {},
    "created_at.value": {},
    "releasedate.value": {},
    "activities.value": {}
  },
  "encoder": "html"
};

export class ElasticSearchHelpers {

  static get listFacetDefaultSize() {
    return 10;
  }

  /*# sanitize a search query for Lucene. Useful if the original
          # query raises an exception, due to bad adherence to DSL.
          # Taken from here:
          #
          # http://stackoverflow.com/questions/16205341/symbols-in-query-string-for-elasticsearch
          #*/
  static sanitizeString = q => {

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
      const filteredTerms = terms.length === 1 ? terms : (terms.filter(term => {
        return (term.length >= queryTweaking.wildcard.minNbOfChars || term.length >= queryTweaking.fuzzySearch.minNbOfChars) &&
                    !term.includes(".") &&
                    !["a", "above", "all", "an", "are", "as", "any", "because", "below", "besides", "but", "by", "eg", "either", "for", "hence", "how", "which", "where", "who", "ie", "in", "instead", "is", "none", "of", "one", "other", "over", "same", "that", "the", "then", "thereby", "therefore", "this", "though", "thus", "to", "under", "until", "when", "why"].includes(term);
      }));
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

      // Escape special characters
      // http://lucene.apache.org/core/old_versioned_docs/versions/2_9_1/queryparsersyntax.html#Escaping Special Characters
      const spectialChars = "\\+-&|!(){}[]^~*?:/";
      const re = new RegExp("([" + spectialChars.split("").map(c => "\\" + c).join("") + "])", "g");
      str = str.replace(re, "\\$1");

      //When odd quotes escape last one
      if (((str.match(/"/g) || []).length - (str.match(/\\"/g) || []).length) % 2 === 1) {
        str = str.match(/(.*)"([^"]*)$/).reduce((r, s, i) => i === 0 ? "" : i === 1 ? r + s + "\\\"" : r + s, "");
      }
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
              count: 0,
              value: null,
              keywords: [],
              size: ElasticSearchHelpers.listFacetDefaultSize
            });
          }
          if (field.children) {
            Object.entries(field.children).forEach(([childName, child]) => {
              if (child.facet) {
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
                  path: name + ".children",
                  childName: childName,
                  count: 0,
                  value: null,
                  keywords: [],
                  size: ElasticSearchHelpers.listFacetDefaultSize
                });
              }
            });
          }
        });
      });
      return facets;
    };

    static getQueryValuesBoost = definition => {

      const initQueryFieldsRec = (shapeFields, parent) => {
        let key = Object.keys(shapeFields)[0];
        let queryFields = {};
        let newQueryfields = {};
        Object.values(shapeFields).forEach(el => {
          Object.keys(el).forEach(fieldName => {
            const field = shapeFields[key][fieldName];
            const fullFieldName = parent + fieldName;
            let queryField = queryFields[fullFieldName];
            if (!queryField) {
              queryField = { boost: 1 };
              queryFields[fullFieldName] = queryField;
            }

            if (field && field.boost && field.boost > queryField.boost) {
              queryField.boost = field.boost;
            }

            newQueryfields[key] = queryFields;

            if (field["children"] !== undefined) {
              let children = initQueryFieldsChildren(field["children"], fullFieldName + ".children.");
              newQueryfields[key] = Object.assign(queryFields, children);
            }
          });
        });
        return newQueryfields;
      };

      const initQueryFieldsChildren = (shapeFields, parent) => {
        let childrenQueryFields = {};
        Object.keys(shapeFields).forEach(fieldName => {
          const field = shapeFields[fieldName];
          const fullFieldName = parent + fieldName;
          let queryField = childrenQueryFields[fullFieldName];
          if (!queryField) {
            queryField = { boost: 1 };
            childrenQueryFields[fullFieldName] = queryField;
          }

          if (field && field.boost && field.boost > queryField.boost) {
            queryField.boost = field.boost;
          }
        });
        return childrenQueryFields;
      };

      const filterShapeFields = (shape, shapeFields) => {
        const filteredShapedFields = {};
        const result = {};
        for (let [key, value] of Object.entries(shapeFields)) {
          if (value.ignoreForSearch !== true) {
            filteredShapedFields[key] = value;
          }
        }
        result[shape] = filteredShapedFields;
        return result;
      };

      const queryValuesBoost = [];
      if (definition) {
        Object.keys(definition).forEach(shape => {
          const shapeFields = definition[shape] && definition[shape].fields;
          const filteredShapeFields = filterShapeFields(shape, shapeFields);
          const initFields = initQueryFieldsRec(filteredShapeFields, "");
          queryValuesBoost.push(initFields);
        });
      }

      queryValuesBoost.map(field => {
        let key = Object.keys(field)[0];
        Object.values(field).forEach(fieldObj => {
          const lastRes = [];
          Object.keys(fieldObj).forEach(f => {
            const boost = fieldObj[f].boost;
            if (boost) {
              lastRes.push(f + ".value^" + boost);
            } else {
              lastRes.push(f + ".value");
            }
          });
          field[key] = lastRes;
        });
        return field;
      });
      return queryValuesBoost;
    };

    static getBoostedTypes = definition => {
      return Object.entries(definition).map(([type, typeDefinition]) => ({
        name: type,
        boost: typeDefinition.boost
      }));
    };

    static getSearchParamsFromState = state => {
      return {
        queryString: state.search.queryString,
        queryFields: state.search.queryFields,
        selectedType: state.search.selectedType,
        facets: state.search.facets,
        sort: state.search.sort,
        from: state.search.from,
        size: state.search.hitsPerPage,
        boostedTypes: [], // ElasticSearchHelpers.getBoostedTypes(state.definition),
        customHighlight: defaultCustomHighlight
        /*
                customHighlight: state.search.queryFields.reduce((result, field) => {
                  result[field.replace(/^(.*?)\^.*$/g,"$1")] = {};
                  return result;
                },[]),
                */
      };
    };

    static buildRequest({ queryString = "", queryFields = [], selectedType, facets = [], sort, from = 0, size = 20, customHighlight, boostedTypes = [] }) {

      const typeFacet = {
        id: "facet_type",
        name: "_type",
        filterType: "_type",
        value: selectedType
      };

      const queryFacets = [...facets, typeFacet];

      const getAllFilters = facets => {
        const filters = {};

        facets.forEach(facet => {
          let filter = null;
          const facetKey = facet.isChild ? `${facet.name}.${facet.childName}.value.keyword` : `${facet.name}.value.keyword`;
          switch (facet.filterType) {
          case "_type":
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
                    const term = {};
                    term[facetKey] = v;
                    filter.bool.should.push({
                      term: term
                    });
                  });
                } else {
                  const term = {};
                  term[facetKey] = facet.value[0];
                  filter = {
                    term: term
                  };
                }
              } else { // AND
                filter = [];
                facet.value.forEach(v => {
                  const term = {};
                  term[facetKey] = v;
                  filter.push({
                    term: term
                  });
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
              filterType: facet.filterType,
              active: !!facet.value,
              filter: filter
            };
          }
        });
        return filters;
      };

      const setFilters = (filters, key) => {
        const filtered = Object.entries(filters).filter(([id, { filterType, active }]) => {
          switch (filterType) {
          case "exists":
            if (id === key) {
              return true;
            }
            return active && id !== key;
          case "_type":
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
          case "_type":
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

      const fields = selectedType ? queryFields.filter(field => field[selectedType])[0][selectedType] : [];
      const query = queryString ? {
        query_string: {
          fields: fields,
          query: ElasticSearchHelpers.sanitizeString(queryString),
          lenient: true
        }
      } : null;

      const allFilters = getAllFilters(queryFacets, selectedType);

      const payload = {
        aggs: getAggs(queryFacets, allFilters),
        from: from,
        highlight: customHighlight,
        post_filter: setFilters(allFilters),
        size: size
      };

      if (sort && Array.isArray(sort.fields)) {
        payload.sort = sort.fields;
      }

      if (query) {
        if (boostedTypes.length) {
          const boostedTypesTerms = boostedTypes.map(type => ({
            term: {
              _type: {
                value: type.name,
                boost: type.boost
              }
            }
          }));
          payload.query = {
            bool: {
              should: [query, ...boostedTypesTerms]
            }
          };
        } else {
          payload.query = query;
        }
      }
      return payload;
    }
}