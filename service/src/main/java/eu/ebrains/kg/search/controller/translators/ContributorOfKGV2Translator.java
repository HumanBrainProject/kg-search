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
import eu.ebrains.kg.search.model.source.PersonSources;
import eu.ebrains.kg.search.model.source.PersonV1andV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.emptyToNull;

public class ContributorOfKGV2Translator implements Translator<PersonSources, Contributor> {


    public Contributor translate(PersonSources personSources, DataStage dataStage, boolean liveMode) {
        Contributor c = new Contributor();
        PersonV1andV2 person = personSources.getPersonV2() != null ? personSources.getPersonV2() : personSources.getPersonV1();
        String uuid = IdUtils.getUUID(person.getId());
        c.setId(uuid);
        List<String> identifiers = Arrays.asList(uuid, String.format("Contributor/%s", person.getIdentifier()));
        c.setIdentifier(identifiers);
        c.setFirstRelease(person.getFirstReleaseAt());
        c.setLastRelease(person.getLastReleaseAt());
        c.setTitle(person.getTitle());
        if(!CollectionUtils.isEmpty(person.getContributions())) {
            c.setContributions(person.getContributions().stream()
                    .map(contribution ->
                            new TargetInternalReference(
                                    liveMode ? contribution.getRelativeUrl() : contribution.getIdentifier(),
                                    contribution.getName(), null)).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(person.getCustodianOf())) {
            c.setCustodianOf(person.getCustodianOf().stream()
                    .map(custodianOf ->
                            new TargetInternalReference(
                                    liveMode ? custodianOf.getRelativeUrl() : custodianOf.getIdentifier(),
                                    custodianOf.getName(), null)).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(person.getCustodianOfModel())) {
            c.setCustodianOfModel(person.getCustodianOfModel().stream()
                    .map(custodianOfModel ->
                            new TargetInternalReference(
                                    liveMode ? custodianOfModel.getRelativeUrl() : custodianOfModel.getIdentifier(),
                                    custodianOfModel.getName(), null)).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(person.getModelContributions())) {
            c.setModelContributions(person.getModelContributions().stream()
                    .map(contribution -> new TargetInternalReference(
                            liveMode ? contribution.getRelativeUrl() : contribution.getIdentifier(),
                            contribution.getName(), null
                    )).collect(Collectors.toList()));
        }
        if(!CollectionUtils.isEmpty(person.getPublications())) {
            c.setPublications(emptyToNull(person.getPublications().stream()
                    .map(publication -> {
                        String publicationResult = null;
                        if (StringUtils.isNotBlank(publication.getCitation()) && StringUtils.isNotBlank(publication.getDoi())) {
                            String url = URLEncoder.encode(publication.getDoi(), StandardCharsets.UTF_8);
                            publicationResult = publication.getCitation() + "\n" + String.format("[DOI: %s]\n[DOI: %s]: https://doi.org/%s", publication.getDoi(), publication.getDoi(), url);
                        } else if (StringUtils.isNotBlank(publication.getCitation()) && StringUtils.isBlank(publication.getDoi())) {
                            publicationResult = publication.getCitation().trim().replaceAll(",$", "");
                        } else if (StringUtils.isBlank(publication.getCitation()) && StringUtils.isNotBlank(publication.getDoi())) {
                            String url = URLEncoder.encode(publication.getDoi(), StandardCharsets.UTF_8);
                            publicationResult = String.format("[DOI: %s]\n[DOI: %s]: https://doi.org/%s", publication.getDoi(), publication.getDoi(), url);
                        }
                        return publicationResult;
                    }).filter(Objects::nonNull).collect(Collectors.toList())));
        }
        if (dataStage == DataStage.IN_PROGRESS) {
            c.setEditorId(person.getEditorId());
        }
        return c;
    }
}
