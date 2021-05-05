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

package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class Helpers {
    public static String getFullName(String familyName, String givenName) {
        if(familyName == null && givenName == null) {
            return  null;
        }
        if(familyName != null && givenName == null) {
            return familyName;
        }
        if(familyName == null) {
            return givenName;
        }
        return String.format("%s, %s", familyName, givenName);
    }

    public static String getFullName(String fullName, String familyName, String givenName) {
        if (StringUtils.isNotBlank(fullName)) {
            return  fullName;
        }
        return getFullName(familyName, givenName);
    }

    public static List<Version> sort(List<Version> datasetVersions) {
        LinkedList<String> versions = new LinkedList<>();
        List<Version> datasetVersionsWithoutVersion = new ArrayList<>();
        Map<String, Version> lookup = new HashMap<>();
        datasetVersions.forEach(dv -> {
            String id = dv.getVersionIdentifier();
            if (id != null) {
                lookup.put(id, dv);
                if (versions.isEmpty()) {
                    versions.add(id);
                } else {
                    String previousVersionIdentifier = dv.getIsNewVersionOf();
                    if (previousVersionIdentifier != null) {
                        int i = versions.indexOf(previousVersionIdentifier);
                        if (i == -1) {
                            versions.addLast(id);
                        } else {
                            versions.add(i, id);
                        }
                    } else {
                        versions.addFirst(id);
                    }
                }
            } else {
                datasetVersionsWithoutVersion.add(dv);
            }
        });
        List<Version> result = versions.stream().map(lookup::get).collect(Collectors.toList());
        result.addAll(datasetVersionsWithoutVersion);
        return result;
    }
}
