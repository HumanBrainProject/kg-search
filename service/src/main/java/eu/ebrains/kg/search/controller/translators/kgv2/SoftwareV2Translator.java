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

package eu.ebrains.kg.search.controller.translators.kgv2;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv2;
import eu.ebrains.kg.search.model.source.openMINDSv2.SoftwareV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.SoftwareVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.*;

public class SoftwareV2Translator extends TranslatorV2<SoftwareV2, SoftwareVersion, SoftwareV2Translator.Result> {

    public static class Result extends ResultsOfKGv2<SoftwareV2> {
    }

    @Override
    public Class<SoftwareV2> getSourceType() {
        return SoftwareV2.class;
    }

    @Override
    public Class<SoftwareVersion> getTargetType() {
        return SoftwareVersion.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("softwarecatalog/software/softwareproject/v1.0.0");
    }

    public SoftwareVersion translate(SoftwareV2 softwareV2, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        SoftwareVersion s = new SoftwareVersion();

        s.setCategory(new Value<>("Software"));
        s.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));

        s.setAllIdentifiers(createList(softwareV2.getIdentifier()));
        s.setId(softwareV2.getIdentifier());
        List<String> identifiers = createList(softwareV2.getIdentifier(), String.format("Software/%s", softwareV2.getIdentifier()));
        s.setIdentifier(identifiers.stream().distinct().collect(Collectors.toList()));
        if (!CollectionUtils.isEmpty(softwareV2.getVersions())) {
            softwareV2.getVersions().sort(Comparator.comparing(SoftwareV2.Version::getVersion).reversed());
        }
        SoftwareV2.Version version = firstItemOrNull(softwareV2.getVersions());

        if (dataStage == DataStage.IN_PROGRESS) {
            s.setEditorId(toValue(softwareV2.getEditorId()));
        }
        s.setAppCategoryOld( emptyToNullValueList(version.getApplicationCategory()));
        s.setTitle(softwareV2.getTitle());

        s.setDescription(softwareV2.getDescription() + (StringUtils.isBlank(version.getDescription())? "": ("\n\n" + version.getDescription())));

        if (!CollectionUtils.isEmpty(version.getSourceCode())) {
            s.setSourceCodeOld(version.getSourceCode().stream()
                    .map(sc -> new TargetExternalReference(sc, sc))
                    .collect(Collectors.toList()));
        }
        s.setFeaturesOld(emptyToNullValueList(version.getFeatures()));
        if (!CollectionUtils.isEmpty(version.getDocumentation())) {
            s.setDocumentation(version.getDocumentation().stream()
                    .map(d -> new TargetExternalReference(d, d))
                    .collect(Collectors.toList()));
        }
        s.setLicenseOld(emptyToNullValueList(version.getLicense()));
        s.setOperatingSystemOld(emptyToNullValueList(version.getOperatingSystem()));
        s.setVersionOld(toValue(version.getVersion()));
        if (!CollectionUtils.isEmpty(version.getHomepage())) {
            s.setHomepageOld(version.getHomepage().stream()
                    .map(h -> new TargetExternalReference(h, h))
                    .collect(Collectors.toList()));
        }
        s.setFirstRelease(softwareV2.getFirstReleaseAt());
        s.setLastRelease(softwareV2.getLastReleaseAt());
        s.setSearchable(true);
        return s;
    }
}
