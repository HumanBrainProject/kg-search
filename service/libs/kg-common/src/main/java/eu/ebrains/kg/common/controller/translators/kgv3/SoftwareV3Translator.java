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

package eu.ebrains.kg.common.controller.translators.kgv3;

import eu.ebrains.kg.common.controller.translators.Helpers;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.source.ResultsOfKGv3;
import eu.ebrains.kg.common.model.source.openMINDSv3.SoftwareV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.Software;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.services.DOICitationFormatter;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SoftwareV3Translator extends TranslatorV3<SoftwareV3, Software, SoftwareV3Translator.Result> {
    public static class Result extends ResultsOfKGv3<SoftwareV3> {
    }

    @Override
    public Class<SoftwareV3> getSourceType() {
        return SoftwareV3.class;
    }

    @Override
    public Class<Software> getTargetType() {
        return Software.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("d149f504-9086-4f0e-bd1d-55fd4355bca0");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/Software");
    }

    public Software translate(SoftwareV3 software, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        Software s = new Software();

        s.setCategory(new Value<>("Software Overview"));
        s.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));

        List<Version> sortedVersions = Helpers.sort(software.getVersions(), translatorUtils.getErrors());
        List<Children<Software.Version>> softwareVersions = sortedVersions.stream().map(v -> {
            Software.Version version = new Software.Version();
            version.setVersion(new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier()));
            version.setInnovation(v.getVersionInnovation() != null ? new Value<>(v.getVersionInnovation()) : null);
            return new Children<>(version);
        }).collect(Collectors.toList());
        s.setSoftwareVersions(softwareVersions);
        s.setId(IdUtils.getUUID(software.getId()));
        s.setAllIdentifiers(software.getIdentifier());
        s.setIdentifier(IdUtils.getIdentifiersWithPrefix("Software", software.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        s.setDescription(software.getDescription());
        s.setTitle(software.getTitle());
        if (!CollectionUtils.isEmpty(software.getDeveloper())) {
            s.setDevelopers(software.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(software.getCustodian())) {
            s.setCustodians(software.getCustodian().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }

        handleCitation(software, s);
        if(s.getCitation()!=null){
            s.setCitationHint(value("Using this citation allows you to reference all versions of this software with one citation.\nUsage of version specific software and metadata should be acknowledged by citing the individual software version."));
        }

        s.setQueryBuilderText(value(TranslatorUtils.createQueryBuilderText(software.getPrimaryType(), s.getId())));
        return s;
    }
}
