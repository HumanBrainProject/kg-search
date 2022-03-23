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

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKG;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.FileRepository;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.RelatedPublication;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class Helpers {
    private final static Logger logger = LoggerFactory.getLogger(Helpers.class);
    private final static Map<Class<?>, Set<Field>> nonStaticTransientFields = new HashMap<>();

    public static String createEmbargoMessage(String type, FileRepository fileRepository, DataStage stage) {
        if (fileRepository != null && fileRepository.getIri() != null) {
            final String message = String.format("This %s is temporarily under embargo. It will become available for download after the embargo period.", type);
            if (stage == DataStage.IN_PROGRESS) {
                final String url = translateInternalFileRepoToUrl(fileRepository);
                if (url != null) {
                    return String.format("%s <br/><br/>If you are an authenticated user, <a href=\"%s\" target=\"_blank\"> you should be able to access the data here</a>", message, url);
                }
            }
            return String.format("This dataset is temporarily under embargo. The data will become available for download after the embargo period.<br/><br/>If you are an authenticated user, <a href=\"https://data.kg.ebrains.eu/files/list?url=%s\" target=\"_blank\"> you should be able to access the data here</a>", fileRepository.getIri());
        }
        return null;
    }


    public static boolean isCscsContainer(FileRepository repository) {
        if (repository != null && repository.getIri() != null) {
            return repository.getIri().startsWith("https://object.cscs.ch");
        }
        return false;
    }

    public static boolean isDataProxyBucket(FileRepository repository) {
        return repository != null && isDataProxyBucket(repository.getIri());
    }

    public static boolean isDataProxyBucket(String repositoryIri) {
        if (repositoryIri != null) {
            return repositoryIri.startsWith("https://data-proxy.ebrains.eu");
        }
        return false;
    }

    public static String translateInternalFileRepoToUrl(FileRepository repository) {
        if (repository != null && repository.getIri() != null) {
            if (isDataProxyBucket(repository)) {
                String id = repository.getIri().replace("https://data-proxy.ebrains.eu/api/buckets/", "");
                return String.format("https://data-proxy.ebrains.eu/%s", id);
            } else if (isCscsContainer(repository)) {
                return String.format("https://data.kg.ebrains.eu/files/list?url=%s", repository.getIri());
            }
        }
        return null;
    }


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
                logger.error(String.format("Circular dependency detected in versions - sorting by natural order: %s", unsortedVersions.stream().filter(Objects::nonNull).map(Version::getId).collect(Collectors.joining(", "))));
                versions.sort(Comparator.comparing(Version::getNaturalSortValue));
                return unsortedVersions;
            }
            versions.add(v);
            previousVersion = v.getVersionIdentifier();
            if (previousVersion == null) {
                logger.error(String.format("Circular dependency detected in versions - sorting by natural order: %s", unsortedVersions.stream().filter(Objects::nonNull).map(Version::getId).collect(Collectors.joining(", "))));
                versions.sort(Comparator.comparing(Version::getNaturalSortValue));
                return unsortedVersions;
            }
        }
        if (CollectionUtils.isEmpty(versions)) {
            return unsortedVersions;
        }
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

    public static String getFormattedDigitalIdentifier(DOICitationFormatter doiCitationFormatter, String digitalIdentifier, RelatedPublication.PublicationType resolvedType) {
        if (StringUtils.isNotBlank(digitalIdentifier)) {
            if (resolvedType == RelatedPublication.PublicationType.DOI) {
                String absoluteDOI = digitalIdentifier.contains("http") && digitalIdentifier.contains("doi.org") ? digitalIdentifier : String.format("https://doi.org/%s", digitalIdentifier);
                final String doiCitation = doiCitationFormatter.getDOICitation(absoluteDOI, "european-journal-of-neuroscience");
                final String[] split = absoluteDOI.split("doi\\.org/");
                String simpleDOI;
                if (split.length == 2) {
                    simpleDOI = split[1];
                } else {
                    simpleDOI = digitalIdentifier;
                }
                String doiLink = String.format("[DOI: %s]\n[DOI: %s]: %s", simpleDOI, simpleDOI, absoluteDOI);
                if (doiCitation != null) {
                    return String.format("%s\n%s", doiCitation, doiLink);
                } else {
                    return doiLink;
                }
            } else if (resolvedType == RelatedPublication.PublicationType.HANDLE) {
                return String.format("[HANDLE: %s]\n[HANDLE: %s]: %s", digitalIdentifier, digitalIdentifier, digitalIdentifier);
            }
        }
        return null;
    }


    private synchronized static Set<Field> getNonStaticNonTransientFields(Class<?> clazz) {
        Set<Field> fields = nonStaticTransientFields.get(clazz);
        if (fields == null) {
            fields = new HashSet<>();
            collectAllNonStaticNonTransientFields(clazz, fields);
            nonStaticTransientFields.put(clazz, fields);
        }
        return fields;
    }


    private static void collectAllNonStaticNonTransientFields(Class<?> clazz, Set<Field> collector) {
        if (clazz.getCanonicalName().startsWith("eu.ebrains.kg")) {
            final Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                if (!Modifier.isTransient(declaredField.getModifiers()) && !Modifier.isStatic(declaredField.getModifiers())) {
                    collector.add(declaredField);
                }
            }
            collectAllNonStaticNonTransientFields(clazz.getSuperclass(), collector);
        }
    }

    public static void collectAllTargetInternalReferences(Object obj, List<TargetInternalReference> collector) {
        if (obj == null) {
            return;
        }
        if (obj instanceof TargetInternalReference) {
            collector.add((TargetInternalReference) obj);
            return;
        }
        getNonStaticNonTransientFields(obj.getClass()).forEach(f -> {
            try {
                f.setAccessible(true);
                Object value = f.get(obj);
                if (value != null) {
                    if (value instanceof Collection) {
                        ((Collection<?>) value).forEach(c -> collectAllTargetInternalReferences(c, collector));
                    } else if (value instanceof Map) {
                        ((Map<?, ?>) value).forEach((k, v) -> collectAllTargetInternalReferences(v, collector));
                    } else {
                        collectAllTargetInternalReferences(value, collector);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

    }

}
