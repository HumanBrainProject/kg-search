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

package eu.ebrains.kg.search.controller.translators.kgv3;

import eu.ebrains.kg.search.controller.translators.Helpers;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv3;
import eu.ebrains.kg.search.model.source.openMINDSv3.ParcellationEntityV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.ParcellationEntityVersionV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.FullNameRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Versions;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Dataset;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.ParcellationEntity;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.ParcellationEntityVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Version;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.IdUtils;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ParcellationEntityV3Translator extends TranslatorV3<ParcellationEntityV3, ParcellationEntity, ParcellationEntityV3Translator.Result> {

    public static class Result extends ResultsOfKGv3<ParcellationEntityV3> {
    }

    @Override
    public Class<ParcellationEntityV3> getSourceType() {
        return ParcellationEntityV3.class;
    }

    @Override
    public Class<ParcellationEntity> getTargetType() {
        return ParcellationEntity.class;
    }

    @Override
    public Class<ParcellationEntityV3Translator.Result> getResultType() {
        return ParcellationEntityV3Translator.Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("7c61cc82-415e-4ba1-8dee-940741e8f128");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/sands/ParcellationEntity");
    }

    public ParcellationEntity translate(ParcellationEntityV3 parcellationEntity, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        ParcellationEntity pe = new ParcellationEntity();
        List<eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version> sortedVersions = Helpers.sort(parcellationEntity.getVersions());
        List<Children<Version>> parcellationEntityVersions = sortedVersions.stream().map(v -> {
            Version version = new Version();
            version.setVersion(new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier()));
            version.setInnovation(v.getVersionInnovation() != null ? new Value<>(v.getVersionInnovation()) : null);
            return new Children<>(version);
        }).collect(Collectors.toList());
        pe.setVersionsTable(parcellationEntityVersions);
        pe.setId(IdUtils.getUUID(pe.getId()));
        pe.setIdentifier(IdUtils.getUUID(pe.getIdentifier()));
        if (StringUtils.isNotBlank(parcellationEntity.getName())) {
            pe.setTitle(value(parcellationEntity.getName()));
        }
        //TODO switch to link once the brain atlas has its own representation
        if (!CollectionUtils.isEmpty(parcellationEntity.getBrainAtlas())) {
            pe.setBrainAtlas(value(parcellationEntity.getBrainAtlas().stream().map(FullNameRef::getFullName).collect(Collectors.toList())));
        }
        pe.setOntologyIdentifier(value(parcellationEntity.getOntologyIdentifier()));
        return pe;
    }
}
