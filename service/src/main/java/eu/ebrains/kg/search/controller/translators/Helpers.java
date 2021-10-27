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

import eu.ebrains.kg.search.model.source.ResultsOfKG;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class Helpers {
    private final static Logger logger = LoggerFactory.getLogger(Helpers.class);

    public static String abbreviateGivenName(String givenName) {
        if (givenName != null) {
            return Arrays.stream(givenName.split(" ")).filter(e -> e.length() > 0).
                    map(e -> String.format("%s.", e.charAt(0))).
                    collect(Collectors.joining(" "));
        }
        return null;

    }

    public static String getFullName(String familyName, String givenName) {
        if (familyName == null) {
            return null;
        }
        return givenName == null ? familyName : String.format("%s, %s", familyName, abbreviateGivenName(givenName));
    }

    public static String stripDOIPrefix(String doi) {
        if (doi != null) {
            int indexToCrop = doi.indexOf("/10.");
            if (indexToCrop != -1) {
                return doi.substring(indexToCrop + 1);
            }
        }
        return doi;
    }

    public static String getFullName(String fullName, String familyName, String givenName) {
        if (StringUtils.isNotBlank(fullName)) {
            return fullName;
        }
        return getFullName(familyName, givenName);
    }

    private static Version getByPreviousVersion(String previousVersion, List<Version> versions) {
        List<Version> previous = versions.stream().filter(v -> previousVersion == null ? v.getIsNewVersionOf() == null : v.getIsNewVersionOf() != null && v.getIsNewVersionOf().equals(previousVersion)).collect(Collectors.toList());
        if (previous.size() > 1) {
            logger.error(String.format("Ambiguous new versions detected. This is not valid. %s", previous.stream().filter(Objects::nonNull).map(Version::getId).collect(Collectors.joining(", "))));
            return null;
        } else if (CollectionUtils.isEmpty(previous)) {
            return null;
        } else {
            return previous.get(0);
        }
    }


    public static List<Version> sort(List<Version> unsortedVersions) {
        List<Version> versions = new ArrayList<>();
        String previousVersion = null;
        Version v;
        while ((v = getByPreviousVersion(previousVersion, unsortedVersions)) != null) {
            if (versions.contains(v)) {
                logger.error(String.format("Circular dependency detected in versions: %s", unsortedVersions.stream().filter(Objects::nonNull).map(Version::getId).collect(Collectors.joining(", "))));
                return unsortedVersions;
            }
            versions.add(v);
            previousVersion = v.getVersionIdentifier();
            if (previousVersion == null) {
                logger.error(String.format("Circular dependency detected in versions: %s", unsortedVersions.stream().filter(Objects::nonNull).map(Version::getId).collect(Collectors.joining(", "))));
                return unsortedVersions;
            }
        }
        if (CollectionUtils.isEmpty(versions)) {
            return unsortedVersions;
        }
        Collections.reverse(versions);
        if (versions.size() != unsortedVersions.size()) {
            for (Version alt : unsortedVersions) {
                if (!versions.contains(alt)) {
                    versions.add(alt);
                }
            }
        }
        return versions;
    }

    public static <E> Stats getStats(ResultsOfKG<E> result, int from) {
        int pageSize = CollectionUtils.isEmpty(result.getData()) ? 0 : result.getData().size();
        int cumulatedSize = from + pageSize;
        String percentage = (CollectionUtils.isEmpty(result.getData()) || result.getTotal() == null || result.getTotal() == 0) ? "unknown%" : String.format("%d%s", Math.round(100.0 * cumulatedSize / result.getTotal()), "%");
        String info = String.format("%d out of %d, %s", cumulatedSize, result.getTotal(), percentage);
        return new Stats(pageSize, info);
    }

    public static String getFormattedDOI(DOICitationFormatter doiCitationFormatter, String doi){
        if(StringUtils.isNotBlank(doi)) {
            final String doiCitation = doiCitationFormatter.getDOICitation(doi);
            final String[] split = doi.split("doi\\.org/");
            String simpleDOI;
            if (split.length == 2) {
                simpleDOI = split[1];
            } else {
                simpleDOI = doi;
            }
            String doiLink = String.format("[DOI: %s]\n[DOI: %s]: %s", simpleDOI, simpleDOI, doi);

            if (doiCitation != null) {
                return String.format("%s\n%s", doiCitation, doiLink);
            } else {
                return doiLink;
            }
        }
        return null;
    }

}
