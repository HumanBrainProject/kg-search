/*
 *  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *  Copyright 2021 - 2023 EBRAINS AISBL
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This open source software code was developed in part or in whole in the
 *  Human Brain Project, funded from the European Union's Horizon 2020
 *  Framework Programme for Research and Innovation under
 *  Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 *  (Human Brain Project SGA1, SGA2 and SGA3).
 */

package eu.ebrains.kg.search.utils;

import eu.ebrains.kg.search.model.Facet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;

import java.text.Normalizer;
import java.util.Set;

public class FacetsUtils {

    public static final String FACET_TYPE = "facet_type";

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

    public static String getUniqueFacetName(Facet facet, Set<String> names) {
        String name = getFacetName(facet);
        int count = 1;
        String nameWithSuffix = name;
        while (names.contains(nameWithSuffix)) {
            count+=1;
            nameWithSuffix = String.format("%s%d", name, count);
        }
        return nameWithSuffix;
    }

    private static String getFacetName(Facet facet) {
        if (StringUtils.isNotBlank(facet.getLabel())) {
            String normalized = Normalizer.normalize(facet.getLabel(), Normalizer.Form.NFD);
            String alphanum = normalized.replaceAll("[^A-Za-z0-9 ]", "");
            return CaseUtils.toCamelCase(alphanum, false, ' ');
        }
        return facet.getName();
    }
}
