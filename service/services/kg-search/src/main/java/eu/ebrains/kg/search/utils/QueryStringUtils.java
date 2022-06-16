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

import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryStringUtils {

    private static final Set<String> OPERATORS = Stream.of(
            "AND", "OR", "NOT"
    ).collect(Collectors.toSet());

    private static final Set<Character> SPECIAL_CHARS = Stream.of(
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

    public static String fuzzyQueryString(String query) {
        if (StringUtils.isBlank(query)) {
            return query;
        }
        try {
            return QueryStringUtils.escapeSpecialCharacters(query);
        } catch (Exception e) {
            //Special character is not supported in parser
            // try minimal replacements:
            return QueryStringUtils.escapeSpecialCharacters(query);
        }
    }

    private static String capitalizeOperators(String str) {
        String res = str;
        for(String op : OPERATORS) {
            res = res.replaceAll("([ \"\\[\\]{}()]) (?i)" + Pattern.quote(op) + "([ \"\\[\\]{}()])", String.format("$1 %s $2", op));
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
