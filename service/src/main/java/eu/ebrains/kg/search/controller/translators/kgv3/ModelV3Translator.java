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
import eu.ebrains.kg.search.model.source.openMINDSv3.ModelV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.RelatedPublication;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Model;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.IdUtils;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModelV3Translator extends TranslatorV3<ModelV3, Model, ModelV3Translator.Result> {

    public static class Result extends ResultsOfKGv3<ModelV3> {
    }

    @Override
    public Class<ModelV3> getSourceType() {
        return ModelV3.class;
    }

    @Override
    public Class<Model> getTargetType() {
        return Model.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("493c0895-9f9a-4eb1-9f24-8d4227daa87c");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/Model");
    }

    public Model translate(ModelV3 model, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        Model m = new Model();
        m.setId(IdUtils.getUUID(model.getId()));

        m.setAllIdentifiers(model.getIdentifier());
        m.setIdentifier(IdUtils.getUUID(model.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        m.setDescription(value(model.getDescription()));
        m.setTitle(value(model.getTitle()));
        String homepage = model.getHomepage();
        if (StringUtils.isNotBlank(homepage)) {
            m.setHomepage(new TargetExternalReference(homepage, homepage));
        }
        if (!CollectionUtils.isEmpty(model.getStudyTarget())) {
            m.setStudyTarget(ref(model.getStudyTarget()));
        }
        m.setScope(ref(model.getModelScope()));
        m.setAbstractionLevel(ref(model.getAbstractionLevel()));
        if (!CollectionUtils.isEmpty(model.getDeveloper())) {
            m.setContributors(model.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(model.getCustodian())) {
            m.setCustodians(model.getCustodian().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }

        String citation = model.getHowToCite();
        String doi = model.getDoi();
        if (StringUtils.isNotBlank(doi)) {
            final String doiWithoutPrefix = Helpers.stripDOIPrefix(doi);
            //TODO do we want to keep this one? It's actually redundant with what we have in "cite dataset"
            m.setDoi(value(doiWithoutPrefix));
            if (StringUtils.isNotBlank(citation)) {
                m.setCitation(value(String.format("%s [DOI: %s](%s)", citation, doiWithoutPrefix, doi)));
            } else {
                m.setCitation(value(Helpers.getFormattedDigitalIdentifier(doiCitationFormatter, doi, RelatedPublication.PublicationType.DOI)));
            }
        } else if (StringUtils.isNotBlank(citation)) {
            m.setCitation(value(citation));
        }

        if (!CollectionUtils.isEmpty(model.getVersions())) {
            List<Version> sortedVersions = Helpers.sort(model.getVersions());                                         //v.getFullName()
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            m.setVersions(references);
        }
        return m;
    }
}
