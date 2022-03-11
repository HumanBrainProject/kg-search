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

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv3;
import eu.ebrains.kg.search.model.source.openMINDSv3.PersonOrOrganizationV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.ExtendedFullNameRefForResearchProductVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.IdUtils;
import eu.ebrains.kg.search.utils.TranslationException;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

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


    public Contributor translate(PersonOrOrganizationV3 personOrOrganization, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        Contributor c = new Contributor();
        String uuid = IdUtils.getUUID(personOrOrganization.getId());
        c.setId(uuid);

        c.setAllIdentifiers(personOrOrganization.getIdentifier());
        List<String> identifiers = IdUtils.getIdentifiersWithPrefix("Contributor", personOrOrganization.getIdentifier());
        identifiers.add(uuid);
        c.setIdentifier(identifiers.stream().distinct().collect(Collectors.toList()));
        c.setTitle(value(personOrOrganization.getFullName() != null ? personOrOrganization.getFullName() : personOrOrganization.getGivenName()==null ? personOrOrganization.getFamilyName() : String.format("%s, %s", personOrOrganization.getFamilyName(), personOrOrganization.getGivenName())));
        if (CollectionUtils.isEmpty(personOrOrganization.getCustodianOfDataset()) &&
                        CollectionUtils.isEmpty(personOrOrganization.getCustodianOfSoftware()) &&
                        CollectionUtils.isEmpty(personOrOrganization.getCustodianOfMetaDataModel())&&
                        CollectionUtils.isEmpty(personOrOrganization.getCustodianOfModel()) &&
                        CollectionUtils.isEmpty(personOrOrganization.getDatasetContributions()) &&
                        CollectionUtils.isEmpty(personOrOrganization.getModelContributions()) &&
                        CollectionUtils.isEmpty(personOrOrganization.getSoftwareContributions()) &&
                    CollectionUtils.isEmpty(personOrOrganization.getMetaDataModelContributions())
        ) {
            //No contributions to anything - we don't index it...
            return null;
        }
        c.setCustodianOfDataset(getReferences(personOrOrganization.getCustodianOfDataset()));
        c.setCustodianOfModel(getReferences(personOrOrganization.getCustodianOfModel()));
        c.setCustodianOfSoftware(getReferences(personOrOrganization.getCustodianOfSoftware()));
        c.setCustodianOfMetaDataModels(getReferences(personOrOrganization.getCustodianOfMetaDataModel()));

        c.setDatasetContributions(getReferences(personOrOrganization.getDatasetContributions()));
        c.setModelContributions(getReferences(personOrOrganization.getModelContributions()));
        c.setSoftwareContributions(getReferences(personOrOrganization.getSoftwareContributions()));
        c.setMetaDataModelContributions(getReferences(personOrOrganization.getMetaDataModelContributions()));

        return c;
    }


    private List<TargetInternalReference> getReferences(List<ExtendedFullNameRefForResearchProductVersion> references){
        if(references == null){
            return null;
        }
        final List<TargetInternalReference> result = references.stream().map(r -> {
            //Add all children elements if it has a research product version
            List<TargetInternalReference> refs;
            if(!CollectionUtils.isEmpty(r.getResearchProductVersions())) {
                refs = refVersion(r.getResearchProductVersions(), true);
            }
            else{
                refs = Collections.singletonList(ref(r));
            }
            return refs;
        }).filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull).distinct().sorted(Comparator.comparing(TargetInternalReference::getValue)).collect(Collectors.toList());
        return CollectionUtils.isEmpty(result) ? null : result;

    }


}
