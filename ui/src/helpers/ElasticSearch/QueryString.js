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
  excludedTerms: ["a", "above", "all", "an", "are", "as", "any", "because", "below", "besides", "but", "by", "eg", "either", "for", "hence", "how", "which", "where", "who", "ie", "in", "instead", "is", "none", "of", "one", "other", "over", "same", "that", "the", "then", "thereby", "therefore", "this", "though", "thus", "to", "under", "until", "when", "why"],
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

const escapeSpecialCharacters = str => {
  const spectialChars = "\\+-&|!(){}[]^~*?:/";
  const re = new RegExp("([" + spectialChars.split("").map(c => "\\" + c).join("") + "])", "g");
  return str.replace(re, "\\$1");
};

const trimOperators = str => {
  let res = str;
  res = res.replace(/^\s*&&\s*(.*)$/, "$1"); //NOSONAR
  res = res.replace(/^\s*\|\|\s*(.*)$/, "$1"); //NOSONAR
  res = res.replace(/^(.*)\s*&&\s*$/, "$1"); //NOSONAR
  res = res.replace(/^(.*)\s*\|\|\s*$/, "$1"); //NOSONAR
  res = res.trim();
  if (res !== str) {
    return trimOperators(res);
  }
  return res;
};

const addTermsFromFieldExpression = (node, terms) => {
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

  if (node.term && Number.isNaN(Number(node.term)) && !/.+\*$/.test(node.term) && !/.+\?$/.test(node.term) && !/\s/.test(node.term)) {//NOSONAR
    terms.push(node.term.toLowerCase());
  }
};

const addTermsFromNodeExpresion = (node, terms) => {
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
};

const addTermsFromExpression = (node, terms) => {

  if (!node) {
    return;
  }

  if (node.operator || node.left) {
    addTermsFromNodeExpresion(node, terms);
  } else if (node.term) {
    addTermsFromFieldExpression(node, terms);
  }
};

const getTerms = node => {
  const terms = [];
  addTermsFromExpression(node, terms);
  return terms;
};

const getMaxWildCardConfig = () => {
  const maxWildcardConfig = Number(queryTweaking.wildcard.maxNbOfTerms);
  if (isNaN(maxWildcardConfig)) {
    return -1;
  }
  return Math.floor(maxWildcardConfig);
};

const getMaxFuzzySearchConfig =  () => {
  const maxFuzzySearchConfig = Number(queryTweaking.fuzzySearch.maxNbOfTerms);
  if (isNaN(maxFuzzySearchConfig)) {
    return -1;
  }
  return Math.floor(maxFuzzySearchConfig);
};

const getMaxWildCard = (maxWildcardConfig, terms) => {
  if(maxWildcardConfig < 0 || (terms.length < maxWildcardConfig)) {
    return terms.length;
  }
  return maxWildcardConfig;
};

const getMaxFuzzySearch = (maxFuzzySearchConfig, terms) => {
  if(maxFuzzySearchConfig < 0 || (terms.length < maxFuzzySearchConfig )) {
    return terms.length;
  }
  return maxFuzzySearchConfig;
};

const filterTermsByQueryTweaking = terms => {
  if (terms.length === 1) {
    return terms;
  }
  return terms.filter(term => {
    if (term.includes(".") || queryTweaking.excludedTerms.includes(term)) {
      return false;
    }
    return (term.length >= queryTweaking.wildcard.minNbOfChars || term.length >= queryTweaking.fuzzySearch.minNbOfChars);
  });
};

const getTermsToEscape = terms => terms.reduce((acc, term) => {
  const escapedTerm = escapeSpecialCharacters(term);
  if (escapedTerm !== term) {
    acc.push({value: term, escapeValue:escapedTerm });
  }
  return acc;
}, []);

const escapeTerms = (str, terms) => {
  terms.forEach(term => {
    const reg = new RegExp(term.value, "g");
    str = str.replace(reg, term.escapeValue);
  });
  return str;
};

const removeExcludedTerms = (terms, excludedTerms) => {
  const excluded = excludedTerms.map(t => t.value);
  return terms.filter(t => !excluded.includes(t));
};

const removeTermsPartOfString = (terms, str) => terms.filter(term => !str.includes(`"${term}"`));

const wildcardsAndFuzzyWithTerms = (str, terms, forceWildcard, maxWildcard, maxFuzzySearch) => {
  terms.forEach((term, idx) => {
    const wildcardCondition = forceWildcard || (idx < maxWildcard && term.length >= queryTweaking.wildcard.minNbOfChars);
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
  return str;
};

/*# sanitize a search query for Lucene. Useful if the original
# query raises an exception, due to bad adherence to DSL.
# Taken from here:
#
# http://stackoverflow.com/questions/16205341/symbols-in-query-string-for-elasticsearch
#*/
export const sanitizeQueryString = q => {

  // Escape special characters
  // http://lucene.apache.org/core/old_versioned_docs/versions/2_9_1/queryparsersyntax.html#Escaping Special Characters

  let str = q.trim().replace(/\s+/g, " ");
  str = trimOperators(str).toLowerCase();

  // Capitalize operator
  ["AND", "OR", "NOT"].forEach(op => {
    const re = new RegExp("([ \"\\[\\]{}()])" + op + "([ \"\\[\\]{}()])", "gi");
    str = str.replace(re, "$1" + op + "$2");
  });

  try {
    const tree = parser.parse(str);
    const maxWildcardConfig = getMaxWildCardConfig();
    const maxFuzzySearchConfig = getMaxFuzzySearchConfig();
    const terms = getTerms(tree);
    const maxWildcard = getMaxWildCard(maxWildcardConfig, terms);
    const maxFuzzySearch = getMaxFuzzySearch(maxFuzzySearchConfig, terms);
    if (terms.length <= queryTweaking.maxNbOfTermsTrigger) {
      let filteredTerms = filterTermsByQueryTweaking(terms);
      const termsToEscape = getTermsToEscape(filteredTerms);
      str = escapeTerms(str, termsToEscape);
      filteredTerms = removeExcludedTerms(filteredTerms, termsToEscape);
      filteredTerms = removeTermsPartOfString(filteredTerms, str);
      str = wildcardsAndFuzzyWithTerms(str, filteredTerms, terms.length === 1, maxWildcard, maxFuzzySearch);
    }
  } catch (e) {
    //Special character is not supported in parser
    // try minimal replacements:
    str = escapeSpecialCharacters(str);
  }
  return str;
};