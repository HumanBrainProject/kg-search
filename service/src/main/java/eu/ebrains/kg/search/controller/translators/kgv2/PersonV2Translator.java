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
import eu.ebrains.kg.search.model.source.openMINDSv2.PersonV2;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Contributor;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.*;

public class PersonV2Translator extends TranslatorV2<PersonV2, Contributor, PersonV2Translator.Result> {

    public static class Result extends ResultsOfKGv2<PersonV2> {
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("uniminds/core/person/v1.0.0");
    }

    @Override
    public Class<PersonV2> getSourceType() {
        return PersonV2.class;
    }

    @Override
    public Class<Contributor> getTargetType() {
        return Contributor.class;
    }

    @Override
    public Contributor translate(PersonV2 person, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        Contributor c = new Contributor();
        c.setId(person.getIdentifier());

        c.setAllIdentifiers(Collections.singletonList(person.getIdentifier()));
        c.setIdentifier(Arrays.asList(c.getId(), String.format("Contributor/%s", person.getIdentifier())));
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

        if(!CollectionUtils.isEmpty(person.getCustodianOfModel())) {
            c.setCustodianOfModel(person.getCustodianOfModel().stream()
                    .map(custodianOfModel ->
                            new TargetInternalReference(
                                    liveMode ? custodianOfModel.getRelativeUrl() : String.format("Model/%s", custodianOfModel.getIdentifier()),
                                    custodianOfModel.getName(), null)).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(person.getModelContributions())) {
            c.setModelContributions(person.getModelContributions().stream()
                    .map(contribution -> new TargetInternalReference(
                            liveMode ? contribution.getRelativeUrl() : String.format("Model/%s", contribution.getIdentifier()),
                            contribution.getName(), null
                    )).collect(Collectors.toList()));
        }
        if (dataStage == DataStage.IN_PROGRESS) {
            c.setEditorId(value(person.getEditorId()));
        }
        return c;
    }
}
