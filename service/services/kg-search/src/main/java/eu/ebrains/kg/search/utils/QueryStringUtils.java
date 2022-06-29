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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class QueryStringUtils {

    public static final Set<String> CONDITIONAL_OPERATORS = Stream.of(
            "NOT"
    ).collect(Collectors.toSet());

    public static final Set<String> LOGICAL_OPERATORS = Stream.of(
            "AND", "OR"
    ).collect(Collectors.toSet());


    public static final Set<Character> SPECIAL_CHARS = Stream.of(
            '\\', '+', '-', '&', '|', '!', '(', ')', '{', '}', '[', ']', '^', '~', '*', '?', ':', '/'

    ).collect(Collectors.toSet());

    public static List<String> sanitizeQueryString(String query) {
        if (!StringUtils.isBlank(query)) {
            String text = query.trim().replaceAll("\\s+", " ");
            text = capitalizeOperators(text);
            final List<String> tokens = Arrays.stream(text.split(" ")).map(item -> {
                if (CONDITIONAL_OPERATORS.contains(item.toUpperCase()) || LOGICAL_OPERATORS.contains(item.toUpperCase())) {
                    return item.toUpperCase();
                } else if (QueryTweaking.EXCLUDED_TERMS.contains(item.toLowerCase())) {
                    return null;
                } else {
                    return escapeSpecialCharacters(item);
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
            if (!tokens.isEmpty()) {
                String previousToken = null;
                final Iterator<String> iterator = tokens.iterator();
                while (iterator.hasNext()) {
                    final String token = iterator.next();
                    if (LOGICAL_OPERATORS.contains(token)) {
                        if (previousToken == null || LOGICAL_OPERATORS.contains(previousToken)) {
                            iterator.remove();
                        }
                    }
                    if (previousToken != null && CONDITIONAL_OPERATORS.contains(token) && CONDITIONAL_OPERATORS.contains(previousToken)) {
                        iterator.remove();
                    }
                    previousToken = token;
                }
                Collections.reverse(tokens);
                final Iterator<String> reverseIterator = tokens.iterator();
                while (reverseIterator.hasNext()) {
                    final String next = reverseIterator.next();
                    if (CONDITIONAL_OPERATORS.contains(next) || LOGICAL_OPERATORS.contains(next)) {
                        reverseIterator.remove();
                    } else {
                        break;
                    }
                }
                Collections.reverse(tokens);
                return tokens;
            }
        }
        return Collections.emptyList();
    }

    public static String escapeSpecialCharacters(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        StringBuilder regex = new StringBuilder();
        regex.append("([");
        for (Character character : SPECIAL_CHARS) {
            regex.append(String.format("\\%s", character));
        }
        regex.append("])");
        return str.replaceAll(regex.toString(), "\\\\\\\\$1");
    }

    public static String prepareQuery(List<String> tokens) {
        return tokens.stream().map(token -> {
            if (LOGICAL_OPERATORS.contains(token) || CONDITIONAL_OPERATORS.contains(token)) {
                return token;
            } else {
               return String.format("%s*", token);
            }
        }).collect(Collectors.joining(" "));
    }

    private static String capitalizeOperators(String str) {
        String res = str;
        for (String op : LOGICAL_OPERATORS) {
            res = res.replaceAll("([ \"\\[\\]{}()])(?i)" + op + "([ \"\\[\\]{}()])", String.format("$1%s$2", op));
        }
        return res;
    }

}
