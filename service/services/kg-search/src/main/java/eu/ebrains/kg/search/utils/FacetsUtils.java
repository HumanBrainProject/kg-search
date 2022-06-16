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

import java.text.Normalizer;

public class FacetsUtils {

    public final static String FACET_TYPE = "facet_type";

    public static String getPath(String path, String property) {
        if (property.equals("children")) {
            return path;
        }
        if (StringUtils.isBlank(path)) {
            return property;
        }
        return String.format("%s.%s", path, property);
    }

    public static String getChildPath(String path, String property) {
        if (StringUtils.isBlank(path)) {
            return property;
        }
        if (property.equals("children")) {
            return String.format("%s.%s", path, property);
        }
        return String.format("%s.%s.children", path, property);
    }

    public static String getFacetName(String label) {
        String normalized = Normalizer.normalize(label, Normalizer.Form.NFD);
        String alphanum = normalized.replaceAll("[^A-Za-z0-9 ]", "");
        return camelCase(alphanum);
    }

    private static String camelCase(String text) {
        String[] words = text.split("[\\W_]+");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i == 0) {
                word = word.isEmpty() ? word : word.toLowerCase();
            } else {
                word = word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
            }
            builder.append(word);
        }
        return builder.toString();
    }
}
