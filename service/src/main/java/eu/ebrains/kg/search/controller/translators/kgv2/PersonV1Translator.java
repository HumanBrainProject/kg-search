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
import eu.ebrains.kg.search.model.source.openMINDSv1.PersonV1;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.TranslationException;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PersonV1Translator extends TranslatorV2<PersonV1, Contributor, PersonV1Translator.Result> {

    public static class Result extends ResultsOfKGv2<PersonV1> {
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("minds/core/person/v1.0.0");
    }

    @Override
    public Class<PersonV1> getSourceType() {
        return PersonV1.class;
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
    public Contributor translate(PersonV1 person, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        Contributor c = new Contributor();

        c.setCategory(new Value<>("Contributor"));
        c.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));

        c.setId(person.getIdentifier());
        c.setAllIdentifiers(createList(person.getIdentifier()));
        c.setIdentifier(createList(c.getId(), String.format("Contributor/%s", person.getIdentifier())).stream().distinct().collect(Collectors.toList()));
        c.setFirstRelease(value(person.getFirstReleaseAt()));
        c.setLastRelease(value(person.getLastReleaseAt()));
        c.setTitle(value(person.getTitle()));
        if(!CollectionUtils.isEmpty(person.getContributions())) {
            c.setDatasetContributions(person.getContributions().stream()
                    .map(contribution ->
                            new TargetInternalReference(
                                    liveMode ? contribution.getRelativeUrl() : String.format("Dataset/%s", contribution.getIdentifier()),
                                    contribution.getName(), null)).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(person.getCustodianOf())) {
            c.setCustodianOfDataset(person.getCustodianOf().stream()
                    .map(custodianOf ->
                            new TargetInternalReference(
                                    liveMode ? custodianOf.getRelativeUrl() : String.format("Dataset/%s", custodianOf.getIdentifier()),
                                    custodianOf.getName(), null)).collect(Collectors.toList()));
        }
        if (dataStage == DataStage.IN_PROGRESS) {
            c.setEditorId(value(person.getEditorId()));
        }
        return c;
    }
}
