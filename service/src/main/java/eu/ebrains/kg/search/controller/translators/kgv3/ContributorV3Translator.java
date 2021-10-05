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
import eu.ebrains.kg.search.model.source.openMINDSv3.PersonV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.*;

public class ContributorV3Translator extends TranslatorV3<PersonV3, Contributor, ContributorV3Translator.Result> {
    private static final String CONTRIBUTOR_QUERY_ID = "b31f015f-9592-408a-b3e2-d6ed74abc5ce";

    public static class Result extends ResultsOfKGv3<PersonV3> {
    }

    @Override
    public Class<PersonV3> getSourceType() {
        return PersonV3.class;
    }

    @Override
    public Class<Contributor> getTargetType() {
        return Contributor.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList(CONTRIBUTOR_QUERY_ID);
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/Person");
    }

    public Contributor translate(PersonV3 person, DataStage dataStage, boolean liveMode) {
        Contributor c = new Contributor();
        String uuid = IdUtils.getUUID(person.getId());
        c.setId(uuid);
        List<String> identifiers = IdUtils.getUUID(person.getIdentifier());
        identifiers.add(uuid);
        c.setIdentifier(identifiers.stream().distinct().collect(Collectors.toList()));
        c.setTitle(Helpers.getFullName(person.getFamilyName(), person.getGivenName()));
        if(!CollectionUtils.isEmpty(person.getContributions())) {
            c.setContributions(person.getContributions().stream()
                    .map(contribution ->
                            new TargetInternalReference(
                                    IdUtils.getUUID(contribution.getId()),
                                    contribution.getName(),
                                    null)
                    ).collect(Collectors.toList())
            );
        }

        if(!CollectionUtils.isEmpty(person.getCustodianOf())) {
            c.setCustodianOf(person.getCustodianOf().stream()
                    .map(custodianOf ->
                            new TargetInternalReference(
                                    IdUtils.getUUID(custodianOf.getId()),
                                    custodianOf.getName(),
                                    null)
                    ).collect(Collectors.toList())
            );
        }

        if(!CollectionUtils.isEmpty(person.getCustodianOfModel())) {
            c.setCustodianOfModel(person.getCustodianOfModel().stream()
                    .map(custodianOfModel ->
                            new TargetInternalReference(
                                    IdUtils.getUUID(custodianOfModel.getId()),
                                    custodianOfModel.getName(),
                                    null)
                    ).collect(Collectors.toList())
            );
        }

        if(!CollectionUtils.isEmpty(person.getModelContributions())) {
            c.setModelContributions(person.getModelContributions().stream()
                    .map(contribution -> new TargetInternalReference(
                            IdUtils.getUUID(contribution.getId()),
                            contribution.getName(),
                            null)
                    ).collect(Collectors.toList())
            );
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
        return c;
    }


}
