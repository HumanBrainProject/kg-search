package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.source.openMINDSv3.commons.InternalDatasetVersion;
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

    public static List<InternalDatasetVersion> sort(List<InternalDatasetVersion> datasetVersions) {
        LinkedList<String> versions = new LinkedList<>();
        List<InternalDatasetVersion> datasetVersionsWithoutVersion = new ArrayList<>();
        Map<String, InternalDatasetVersion> lookup = new HashMap<>();
        datasetVersions.forEach(dv -> {
            String id = dv.getVersionIdentifier();
            if (id != null) {
                lookup.put(id, dv);
                if (versions.isEmpty()) {
                    versions.add(id);
                } else {
                    String previousVersionIdentifier = dv.getPreviousVersionIdentifier();
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
        List<InternalDatasetVersion> result = versions.stream().map(lookup::get).collect(Collectors.toList());
        result.addAll(datasetVersionsWithoutVersion);
        return result;
    }
}
