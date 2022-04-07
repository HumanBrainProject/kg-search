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
import eu.ebrains.kg.search.model.source.openMINDSv3.*;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Versions;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.ControlledTerm;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.ModelVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.ParcellationEntityVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.IdUtils;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ParcellationEntityVersionV3Translator extends TranslatorV3<ParcellationEntityVersionV3, ParcellationEntityVersion, ParcellationEntityVersionV3Translator.Result> {

    public static class Result extends ResultsOfKGv3<ParcellationEntityVersionV3> {
    }

    @Override
    public Class<ParcellationEntityVersionV3> getSourceType() {
        return ParcellationEntityVersionV3.class;
    }

    @Override
    public Class<ParcellationEntityVersion> getTargetType() {
        return ParcellationEntityVersion.class;
    }

    @Override
    public Class<ParcellationEntityVersionV3Translator.Result> getResultType() {
        return ParcellationEntityVersionV3Translator.Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("2a3a00fb-3f31-4e5b-9e2f-aa63b8339e45");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/sands/ParcellationEntityVersion");
    }

    public ParcellationEntityVersion translate(ParcellationEntityVersionV3 parcellationEntityVersion, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        ParcellationEntityVersion pev = new ParcellationEntityVersion();

        pev.setCategory(new Value<>("Parcellation Entity"));
        pev.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));

        final Versions parcellationEntity = parcellationEntityVersion.getParcellationEntity();
        List<Version> versions = parcellationEntity == null ? null : parcellationEntity.getVersions();
        boolean hasMultipleVersions = !CollectionUtils.isEmpty(versions) && versions.size() > 1;

        pev.setAllIdentifiers(pev.getIdentifier());
        if (hasMultipleVersions) {
            pev.setVersion(parcellationEntityVersion.getVersion());
            List<Version> sortedVersions = Helpers.sort(versions);
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            references.add(new TargetInternalReference(IdUtils.getUUID(parcellationEntity.getId()), "All versions"));
            pev.setVersions(references);
            pev.setAllVersionRef(new TargetInternalReference(IdUtils.getUUID(parcellationEntity.getId()), "All versions"));
        }

        if (StringUtils.isNotBlank(parcellationEntityVersion.getName())) {
            if(hasMultipleVersions || StringUtils.isBlank(parcellationEntityVersion.getVersionIdentifier())) {
                pev.setTitle(value(parcellationEntityVersion.getName()));
            }
            else{
                pev.setTitle(value(String.format("%s %s", parcellationEntityVersion.getName(), parcellationEntityVersion.getVersionIdentifier())));
            }
        } else if (parcellationEntity != null && StringUtils.isNotBlank(parcellationEntity.getFullName())) {
            if(hasMultipleVersions || StringUtils.isBlank(parcellationEntityVersion.getVersionIdentifier())) {
                pev.setTitle(value(parcellationEntity.getFullName()));
            }
            else{
                pev.setTitle(value(String.format("%s %s", parcellationEntity.getFullName(), parcellationEntityVersion.getVersionIdentifier())));
            }
        }
        //TODO finalize
        return pev;
    }
}
