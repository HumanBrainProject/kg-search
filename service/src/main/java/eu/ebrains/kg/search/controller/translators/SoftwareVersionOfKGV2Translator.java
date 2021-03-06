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
import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.SoftwareVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.emptyToNull;
import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.firstItemOrNull;

public class SoftwareVersionOfKGV2Translator implements Translator<SoftwareV2, SoftwareVersion> {

    public SoftwareVersion translate(SoftwareV2 softwareV2, DataStage dataStage, boolean liveMode) {
        SoftwareVersion s = new SoftwareVersion();
        String uuid = IdUtils.getUUID(softwareV2.getId());
        s.setId(uuid);
        List<String> identifiers = Arrays.asList(uuid, String.format("Software/%s", softwareV2.getIdentifier()));
        s.setIdentifier(identifiers);
        if (!CollectionUtils.isEmpty(softwareV2.getVersions())) {
            softwareV2.getVersions().sort(Comparator.comparing(SoftwareV2.Version::getVersion).reversed());
        }
        SoftwareV2.Version version = firstItemOrNull(softwareV2.getVersions());

        if (dataStage == DataStage.IN_PROGRESS) {
            s.setEditorId(softwareV2.getEditorId());
        }
        s.setAppCategory(emptyToNull(version.getApplicationCategory()));
        s.setTitle(softwareV2.getTitle());

        s.setDescription(softwareV2.getDescription() + (StringUtils.isBlank(version.getDescription())? "": ("\n\n" + version.getDescription())));

        if (!CollectionUtils.isEmpty(version.getSourceCode())) {
            s.setSourceCode(version.getSourceCode().stream()
                    .map(sc -> new TargetExternalReference(sc, sc))
                    .collect(Collectors.toList()));
        }
        s.setFeatures(emptyToNull(version.getFeatures()));
        if (!CollectionUtils.isEmpty(version.getDocumentation())) {
            s.setDocumentation(version.getDocumentation().stream()
                    .map(d -> new TargetExternalReference(d, d))
                    .collect(Collectors.toList()));
        }
        s.setLicense(emptyToNull(version.getLicense()));
        s.setOperatingSystem(emptyToNull(version.getOperatingSystem()));
        s.setVersion(version.getVersion());
        if (!CollectionUtils.isEmpty(version.getHomepage())) {
            s.setHomepage(version.getHomepage().stream()
                    .map(h -> new TargetExternalReference(h, h))
                    .collect(Collectors.toList()));
        }
        s.setFirstRelease(softwareV2.getFirstReleaseAt());
        s.setLastRelease(softwareV2.getLastReleaseAt());
        s.setSearchable(true);
        return s;
    }
}
