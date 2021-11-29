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
import eu.ebrains.kg.search.model.source.openMINDSv3.PersonOrOrganizationV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.IdUtils;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.*;

public class ContributorV3Translator extends TranslatorV3<PersonOrOrganizationV3, Contributor, ContributorV3Translator.Result> {
    private static final String CONTRIBUTOR_QUERY_ID = "b31f015f-9592-408a-b3e2-d6ed74abc5ce";

    private static final String CONTRIBUTOR_ORGANIZATION_QUERY_ID = "00ef38c9-2532-4403-a292-5f6b3ccb85a9";

    public static class Result extends ResultsOfKGv3<PersonOrOrganizationV3> {
    }

    @Override
    public Class<PersonOrOrganizationV3> getSourceType() {
        return PersonOrOrganizationV3.class;
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
    public String getQueryFileName(String type) {
        switch (type) {
            case "https://openminds.ebrains.eu/core/Person":
                return "contributorPerson";
            case "https://openminds.ebrains.eu/core/Organization":
                return "contributorOrganization";
        }
        return null;
    }

    @Override
    public String getQueryIdByType(String type) {
        switch (type) {
            case "https://openminds.ebrains.eu/core/Person":
                return CONTRIBUTOR_QUERY_ID;
            case "https://openminds.ebrains.eu/core/Organization":
                return CONTRIBUTOR_ORGANIZATION_QUERY_ID;
            default:
                throw new RuntimeException(String.format("There is no query defined for type %s", type));
        }
    }

    @Override
    public List<String> getQueryIds() {
        return Arrays.asList(CONTRIBUTOR_ORGANIZATION_QUERY_ID, CONTRIBUTOR_QUERY_ID);
    }

    @Override
    public List<String> semanticTypes() {
        return Arrays.asList("https://openminds.ebrains.eu/core/Organization", "https://openminds.ebrains.eu/core/Person");
    }

    private void handleSimpleContribution(Map<String, List<TargetInternalReference>> collector, List<PersonOrOrganizationV3.SimpleContribution> contributions, String type){
        contributions.forEach(cont -> {
            if(!collector.containsKey(type)){
                collector.put(type, new ArrayList<>());
            }
            final String uuidOfRef = IdUtils.getUUID(cont.getId());
            collector.get(type).add(new TargetInternalReference(uuidOfRef, StringUtils.defaultString(StringUtils.defaultString(cont.getFullName(), cont.getFallbackName()), uuidOfRef)));
        });
    }


    public Contributor translate(PersonOrOrganizationV3 personOrOrganization, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        Contributor c = new Contributor();
        String uuid = IdUtils.getUUID(personOrOrganization.getId());
        c.setId(uuid);
        List<String> identifiers = IdUtils.getUUID(personOrOrganization.getIdentifier());
        identifiers.add(uuid);
        c.setIdentifier(identifiers.stream().distinct().collect(Collectors.toList()));
        c.setTitle(Helpers.getFullName(personOrOrganization.getFullName(), personOrOrganization.getFamilyName(), personOrOrganization.getGivenName()));
        if (personOrOrganization.getFullName() != null) {
            //It's an organization
            if (CollectionUtils.isEmpty(personOrOrganization.getDeveloper()) && CollectionUtils.isEmpty(personOrOrganization.getAuthor()) && CollectionUtils.isEmpty(personOrOrganization.getCustodian()) && CollectionUtils.isEmpty(personOrOrganization.getOtherContribution())) {
                //The organization has no contributions at all -> we skip its indexing
                return null;
            } else {
                Map<String, List<TargetInternalReference>> custodianOf = new HashMap<>();

                if(!CollectionUtils.isEmpty(personOrOrganization.getCustodian())){
                    personOrOrganization.getCustodian().forEach(cont -> {
                        cont.getType().stream().map(t -> {
                            final String[] split = t.split("/");
                            return split[split.length - 1].replaceAll("Version", "");
                        }).forEach(type -> {
                            if(!custodianOf.containsKey(type)){
                                custodianOf.put(type, new ArrayList<>());
                            }
                            final String uuidOfRef = IdUtils.getUUID(cont.getId());
                            custodianOf.get(type).add(new TargetInternalReference(uuidOfRef, StringUtils.defaultString(StringUtils.defaultString(cont.getFullName(), cont.getFallbackName()), uuidOfRef)));
                        });
                    });
                    c.setCustodian(custodianOf.keySet().stream().map(k -> {
                        Contributor.Contribution cont = new Contributor.Contribution();
                        cont.setTypeLabel(new Value<>(k));
                        cont.setInstances(custodianOf.get(k));
                        return new Children<>(cont);
                    }).collect(Collectors.toList()));
                }

                Map<String, List<TargetInternalReference>> contributions = new HashMap<>();
                if(!CollectionUtils.isEmpty(personOrOrganization.getDeveloper())){
                    handleSimpleContribution(contributions, personOrOrganization.getDeveloper(), "Software");
                }
                if(!CollectionUtils.isEmpty(personOrOrganization.getAuthor())){
                    handleSimpleContribution(contributions, personOrOrganization.getAuthor(), "Dataset");
                }
                if(!contributions.isEmpty()){
                c.setContribution(contributions.keySet().stream().map(k -> {
                    Contributor.Contribution cont = new Contributor.Contribution();
                    cont.setTypeLabel(new Value<>(k));
                    cont.setInstances(contributions.get(k));
                    return new Children<>(cont);
                }).collect(Collectors.toList()));
                }
            }
        } else {
            if (!CollectionUtils.isEmpty(personOrOrganization.getContributions())) {
                c.setContributions(personOrOrganization.getContributions().stream()
                        .map(contribution ->
                                new TargetInternalReference(
                                        IdUtils.getUUID(contribution.getId()),
                                        contribution.getName(),
                                        null)
                        ).collect(Collectors.toList())
                );
            }

            if (!CollectionUtils.isEmpty(personOrOrganization.getCustodianOf())) {
                c.setCustodianOf(personOrOrganization.getCustodianOf().stream()
                        .map(custodianOf ->
                                new TargetInternalReference(
                                        IdUtils.getUUID(custodianOf.getId()),
                                        custodianOf.getName(),
                                        null)
                        ).collect(Collectors.toList())
                );
            }

            if (!CollectionUtils.isEmpty(personOrOrganization.getCustodianOfModel())) {
                c.setCustodianOfModel(personOrOrganization.getCustodianOfModel().stream()
                        .map(custodianOfModel ->
                                new TargetInternalReference(
                                        IdUtils.getUUID(custodianOfModel.getId()),
                                        custodianOfModel.getName(),
                                        null)
                        ).collect(Collectors.toList())
                );
            }

            if (!CollectionUtils.isEmpty(personOrOrganization.getModelContributions())) {
                c.setModelContributions(personOrOrganization.getModelContributions().stream()
                        .map(contribution -> new TargetInternalReference(
                                IdUtils.getUUID(contribution.getId()),
                                contribution.getName(),
                                null)
                        ).collect(Collectors.toList())
                );
            }
            if (!CollectionUtils.isEmpty(personOrOrganization.getPublications())) {
                c.setPublications(emptyToNull(personOrOrganization.getPublications().stream()
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
        }
        return c;
    }


}
