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
import eu.ebrains.kg.search.model.source.openMINDSv3.SoftwareVersionV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.SoftwareVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.emptyToNull;

public class SoftwareVersionOfKGV3Translator implements Translator<SoftwareVersionV3, SoftwareVersion>{

    public SoftwareVersion translate(SoftwareVersionV3 softwareVersion, DataStage dataStage, boolean liveMode) {
        SoftwareVersion s = new SoftwareVersion();
        SoftwareVersionV3.SoftwareVersions software = softwareVersion.getSoftware();
        s.setVersion(softwareVersion.getVersion());
        s.setId(IdUtils.getUUID(softwareVersion.getId()));
        s.setIdentifier(IdUtils.getUUID(softwareVersion.getIdentifier()));

        List<Version> versions = software == null?null:software.getVersions();
        if (!CollectionUtils.isEmpty(versions)) {
            List<Version> sortedVersions = Helpers.sort(versions);
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            references.add(new TargetInternalReference(IdUtils.getUUID(software.getId()), "All versions"));
            s.setVersions(references);
            // if versions cannot be sorted (sortedVersions == versions) we flag it as searchable
            s.setSearchable(sortedVersions == versions || sortedVersions.get(0).getId().equals(softwareVersion.getId()));
        } else {
            if(software != null) {
                List<TargetInternalReference> references = new ArrayList<>();
                references.add(new TargetInternalReference(IdUtils.getUUID(softwareVersion.getId()), softwareVersion.getVersion()));
                references.add(new TargetInternalReference(IdUtils.getUUID(software.getId()), "All versions"));
                s.setVersions(references);
            }
            s.setSearchable(true);
        }
        if (!StringUtils.isBlank(softwareVersion.getDescription())) {
            s.setDescription(softwareVersion.getDescription());
        } else if (software != null) {
            s.setDescription(software.getDescription());
        }
//        if (!StringUtils.isBlank(softwareVersion.getFullName())) {
//            s.setTitle(softwareVersion.getFullName());
//        } else if (software != null {
//            s.setTitle(software.getFullName());
//        }
        // For the UI we don't need the version number in the title as it is set in de dropdown
        if (software != null) {
            s.setTitle(software.getFullName());
            s.setSoftware(new TargetInternalReference(IdUtils.getUUID(software.getId()), software.getFullName()));
        }
        if (!CollectionUtils.isEmpty(softwareVersion.getDeveloper())) {
            s.setDevelopers(softwareVersion.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        } else if (software != null && !CollectionUtils.isEmpty(software.getDeveloper())) {
            s.setDevelopers(software.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(softwareVersion.getCustodian())) {
            s.setCustodians(softwareVersion.getCustodian().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        } else if (software != null && !CollectionUtils.isEmpty(software.getCustodian())) {
            s.setCustodians(software.getCustodian().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }
        s.setAppCategory(emptyToNull(softwareVersion.getApplicationCategory()));

        if (!CollectionUtils.isEmpty(softwareVersion.getSourceCode())) {
            s.setSourceCode(softwareVersion.getSourceCode().stream()
                    .map(sc -> new TargetExternalReference(sc, sc))
                    .collect(Collectors.toList()));
        }
        s.setFeatures(emptyToNull(softwareVersion.getFeatures()));
        if (!CollectionUtils.isEmpty(softwareVersion.getDocumentation())) {
            s.setDocumentation(softwareVersion.getDocumentation().stream()
                    .map(d -> new TargetExternalReference(d, d))
                    .collect(Collectors.toList()));
        }
        s.setLicense(emptyToNull(softwareVersion.getLicense()));
        s.setOperatingSystem(emptyToNull(softwareVersion.getOperatingSystem()));

        String homepage = softwareVersion.getHomepage();
        if (StringUtils.isNotBlank(homepage)) {
            s.setHomepage(Collections.singletonList(new TargetExternalReference(homepage, homepage)));
        }
        return s;
    }
}
