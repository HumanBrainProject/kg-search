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

package eu.ebrains.kg.search.utils;

import eu.ebrains.kg.search.model.QueryTweaking;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class QueryStringUtils {

    public static final Set<String> OPERATORS = Stream.of(
            "AND", "OR", "NOT"
    ).collect(Collectors.toSet());

    public static final Set<Character> SPECIAL_CHARS = Stream.of(
            '\\', '+', '-', '&', '|', '!', '(', ')', '{', '}', '[', ']', '^', '~', '*', '?', ':', '/'

    ).collect(Collectors.toSet());

    public static String sanitizeQueryString(String query) {
        if (StringUtils.isBlank(query)) {
            return query;
        }
        String text = query.trim().replaceAll("\\s+", " ");
        text = trimOperators(text).toLowerCase();
        text = capitalizeOperators(text);
        return text;
    }

    public static String escapeSpecialCharacters(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        StringBuilder regex = new StringBuilder();
        regex.append("([");
        for(Character character : SPECIAL_CHARS) {
            regex.append(String.format("\\%s", character));
        }
        regex.append("])");
        return str.replaceAll(regex.toString(), "\\\\$1");
    };

    private static boolean containsSpecialChars(String query) {
        if (StringUtils.isBlank(query)) {
            return false;
        }
        for(Character character : SPECIAL_CHARS) {
            if (query.contains(character.toString())) {
                return true;
            }
        }
        return false;
    }

    public static String fuzzyQueryString(String query) {
        if (StringUtils.isBlank(query) || containsSpecialChars(query)) {
            return null;
        }
        StringReader sr = new StringReader(query);
        Analyzer analyzer = new StandardAnalyzer();
        try (TokenStream ts = analyzer.tokenStream("", sr)) {
            Set<String> terms = new HashSet<>();
            CharTermAttribute ta = ts.addAttribute (CharTermAttribute.class);
            ts.reset(); // Resets this stream to the beginning. (Required)
            while (ts.incrementToken()) {
                String term = ta.toString();
                if (includeTerm(term)) {
                    terms.add(ta.toString());
                }
            }
            ts.end(); // Perform end-of-stream operations, e.g. set the final offset.
            Set<String> filteredTerms = removeTermsPartOfString(terms, query);
            if (filteredTerms.isEmpty() || filteredTerms.size() > QueryTweaking.MIN_NUMBER_OF_TERMS_TRIGGER) {
                return null;
            }
            return wildcardsAndFuzzyWithTerms(query, filteredTerms, filteredTerms.size() == 1);

        } catch (IOException e) {
            //Special character is not supported in parser
            // try minimal replacements:
            return null;
        }
        // Release resources associated with this stream.
    }

    private static boolean includeTerm(String term) {
        if (StringUtils.isBlank(term) || term.contains(".") || QueryTweaking.EXCLUDED_TERMS.contains(term)) {
            return false;
        }
        return (term.length() >= QueryTweaking.Wildcard.MIN_NUMBER_OF_CHARS || term.length() >= QueryTweaking.FuzzySearch.MIN_NUMBER_OF_CHARS);
    }

    private static String wildcardsAndFuzzyWithTerms(String query, Set<String> terms, boolean forceWildcard) {
        String str = query;
        int idx = 0;
        for(String term : terms) {
            boolean wildcardCondition = forceWildcard || (idx < QueryTweaking.Wildcard.MAX_NUMBER_OF_TERMS && term.length() >= QueryTweaking.Wildcard.MIN_NUMBER_OF_CHARS);
            boolean fuzzySearchCondition = idx < QueryTweaking.FuzzySearch.MAX_NUMBER_OF_TERMS && term.length() >= QueryTweaking.FuzzySearch.MIN_NUMBER_OF_CHARS;
            if (wildcardCondition || fuzzySearchCondition) {
                String re1 = "([ \"\\[\\]{}()][+\\-]?)(?i)" + term + "([ \"\\[\\]{}()])";
                String re2 = "^([+\\-]?)(?i)" + term + "([ \"\\[\\]{}()])";
                String re3 = "([ \"\\[\\]{}()][+\\-]?)(?i)" + term + "$";
                String re4 = "^([+\\-]?)(?i)" + term + "$";
                if (wildcardCondition && fuzzySearchCondition) {
                    str = str.replaceAll(re1, "$1(" + term + "* OR " + term + "* OR " + term + "~)$2");
                    str = str.replaceAll(re2, "$1(" + term + "* OR " + term + "* OR " + term + "~)$2");
                    str = str.replaceAll(re3, "$1(" + term + "* OR " + term + "* OR " + term + "~)");
                    str = str.replaceAll(re4, "$1(" + term + "* OR " + term + "* OR " + term + "~)");
                } else if (wildcardCondition) {
                    str = str.replaceAll(re1, "$1" + term + "*$2");
                    str = str.replaceAll(re2, "$1" + term + "*$2");
                    str = str.replaceAll(re3, "$1" + term + "*");
                    str = str.replaceAll(re4, "$1" + term + "*");
                } else if (fuzzySearchCondition) {
                    str = str.replaceAll(re1, "$1" + term + "~$2");
                    str = str.replaceAll(re2, "$1" + term + "~$2");
                    str = str.replaceAll(re3, "$1" + term + "~");
                    str = str.replaceAll(re4, "$1" + term + "~");
                }
            }
            idx++;
        }
        return str;
    }

    private static Set<String> removeTermsPartOfString(Set<String> terms, String str) {
        return terms.stream().filter(term -> !str.contains(String.format("\"%s\"", term))).collect(Collectors.toSet());
    };

    private static String capitalizeOperators(String str) {
        String res = str;
        for(String op : OPERATORS) {
            res = res.replaceAll("([ \"\\[\\]{}()])(?i)" + op + "([ \"\\[\\]{}()])", String.format("$1%s$2", op));
        }
        return res;
    }

    private static String trimOperators(String str) {
        String res = str; // we use blanch char in regex because all \s have already been replaced previously, so better readability
        res = res.replaceFirst("^ *&& *", ""); // trim starting with &&
        res = res.replaceFirst("^ *\\|\\| *", ""); // trim starting with ||
        res = res.replaceFirst(" *&& *$", ""); // trim ending with &&
        res = res.replaceFirst(" *\\|\\| *$", ""); // trim ending with ||

        res = res.replaceAll(" *&& +&& *", " && "); // replace double && && by single &&
        res = res.replaceAll(" *\\|\\| +\\|\\| *", " || "); // replace double || || by single ||

        res = res.replaceAll(" *\\|\\| *&& *", " || "); // replace || && by single ||
        res = res.replaceAll(" *&& *\\|\\| *", " || "); // replace && || by single ||

        res = res.replaceAll(" *&&&+ *", " && "); // replace more than 2 & by &&
        res = res.replaceAll(" *\\|\\|\\|+ ", " || "); // replace more than 2 | by ||
        res = res.replaceAll(" +", " ");
        res = res.trim();
        if (!res.equals(str)) {
            return trimOperators(res);
        }
        return res;
    };

}
