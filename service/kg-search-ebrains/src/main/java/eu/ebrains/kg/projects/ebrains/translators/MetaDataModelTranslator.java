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

package eu.ebrains.kg.projects.ebrains.translators;

import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.source.ResultsOfKG;
import eu.ebrains.kg.common.model.target.*;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import eu.ebrains.kg.projects.ebrains.EBRAINSTranslatorUtils;
import eu.ebrains.kg.projects.ebrains.source.MetaDataModelV3;
import eu.ebrains.kg.projects.ebrains.target.MetaDataModel;
import eu.ebrains.kg.projects.ebrains.target.Version;
import eu.ebrains.kg.projects.ebrains.translators.commons.EBRAINSTranslator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MetaDataModelTranslator extends EBRAINSTranslator<MetaDataModelV3, MetaDataModel, MetaDataModelTranslator.Result> {

    public static class Result extends ResultsOfKG<MetaDataModelV3> {
    }

    @Override
    public Class<MetaDataModelV3> getSourceType() {
        return MetaDataModelV3.class;
    }

    @Override
    public Class<MetaDataModel> getTargetType() {
        return MetaDataModel.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("1ac00dd9-c597-4ed6-a655-d39fc8500e15");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/MetaDataModel");
    }

    public MetaDataModel translate(MetaDataModelV3 model, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        MetaDataModel m = new MetaDataModel();

        m.setCategory(new Value<>("Meta Data Model Overview"));
        m.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));

        List<eu.ebrains.kg.projects.ebrains.source.commons.Version> sortedVersions = EBRAINSTranslatorUtils.sort(model.getVersions(), translatorUtils.getErrors());
        List<Children<Version>> metaDataModelVersions = sortedVersions.stream().map(v -> {
            Version version = new Version();
            version.setVersion(new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier()));
            version.setInnovation(v.getVersionInnovation() != null ? new Value<>(v.getVersionInnovation()) : null);
            return new Children<>(version);
        }).collect(Collectors.toList());
        m.setModelVersions(metaDataModelVersions);
        m.setId(IdUtils.getUUID(model.getId()));

        m.setAllIdentifiers(model.getIdentifier());
        m.setIdentifier(IdUtils.getUUID(model.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        m.setDescription(value(model.getDescription()));
        m.setTitle(value(model.getTitle()));
        String homepage = model.getHomepage();
        if (StringUtils.isNotBlank(homepage)) {
            m.setHomepage(new TargetExternalReference(homepage, homepage));
        }
        if (!CollectionUtils.isEmpty(model.getDeveloper())) {
            m.setContributors(model.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            EBRAINSTranslatorUtils.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(model.getCustodian())) {
            m.setCustodians(model.getCustodian().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            EBRAINSTranslatorUtils.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }

        handleCitation(model, m);
        if(m.getCitation()!=null){
            m.setCitationHint(value("Using this citation allows you to reference all versions of this meta data model with one citation.\nUsage of version specific data and metadata should be acknowledged by citing the individual meta data model version."));
        }
        m.setQueryBuilderText(value(TranslatorUtils.createQueryBuilderText(model.getPrimaryType(), m.getId())));
        return m;
    }
}
