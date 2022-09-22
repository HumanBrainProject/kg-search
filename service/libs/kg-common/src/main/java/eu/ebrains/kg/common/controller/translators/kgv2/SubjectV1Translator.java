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

package eu.ebrains.kg.common.controller.translators.kgv2;

import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.source.ResultsOfKGv2;
import eu.ebrains.kg.common.model.source.openMINDSv1.SubjectV1;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.Subject;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static eu.ebrains.kg.common.controller.translators.TranslatorCommons.emptyToNull;

public class SubjectV1Translator extends TranslatorV2<SubjectV1, Subject, SubjectV1Translator.Result> {

    public static class Result extends ResultsOfKGv2<SubjectV1> {
    }

    @Override
    public Class<SubjectV1> getSourceType() {
        return SubjectV1.class;
    }

    @Override
    public Class<Subject> getTargetType() {
        return Subject.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("minds/experiment/subject/v1.0.0");
    }

    public Subject translate(SubjectV1 subject, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        Subject s = new Subject();

        s.setCategory(new Value<>("Subject"));
        s.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));

        s.setAllIdentifiers(createList(subject.getIdentifier()));
        s.setId(subject.getIdentifier());
        List<String> identifiers = createList(subject.getIdentifier(), String.format("Subject/%s", subject.getIdentifier()));
        s.setIdentifier(identifiers.stream().distinct().collect(Collectors.toList()));
        s.setAge(value(subject.getAge()));
        s.setAgeCategory(emptyToNull(value(subject.getAgeCategory())));
        if (dataStage == DataStage.IN_PROGRESS) {
            s.setEditorId(value(subject.getEditorId()));
        }
        s.setFirstRelease(value(subject.getFirstReleaseAt()));
        s.setLastRelease(value(subject.getLastReleaseAt()));
        s.setGenotype(value(subject.getGenotype()));
        s.setSex(emptyToNull(value(subject.getSex())));
        s.setSpecies(emptyToNull(value(subject.getSpecies())));
        s.setStrain(subject.getStrain() != null ? value(subject.getStrain()) : value(subject.getStrains()));
        s.setTitle(value(subject.getTitle()));
        s.setWeight(value(subject.getWeight()));
        if(!CollectionUtils.isEmpty(subject.getSamples())) {
            s.setSamples(subject.getSamples().stream()
                    .map(sample ->
                            new TargetInternalReference(
                                    liveMode ? sample.getRelativeUrl() : String.format("Sample/%s", sample.getIdentifier()),
                                    sample.getName())
                    ).collect(Collectors.toList()));
        }
        if(!CollectionUtils.isEmpty(subject.getDatasets())) {
            final List<Subject.Dataset> datasets = subject.getDatasets().stream()
                    .filter(d -> !(CollectionUtils.isEmpty(d.getComponentName()) && CollectionUtils.isEmpty(d.getInstances())))
                    .map(d ->
                            new Subject.Dataset(
                                    !CollectionUtils.isEmpty(d.getComponentName()) ? d.getComponentName() : null,
                                    !CollectionUtils.isEmpty(d.getInstances()) ?
                                            d.getInstances().stream()
                                                    .map(i ->
                                                            new TargetInternalReference(
                                                                    liveMode ? i.getRelativeUrl() : String.format("Dataset/%s", i.getIdentifier()),
                                                                    i.getName())
                                                    ).collect(Collectors.toList()) : null
                            )
                    ).collect(Collectors.toList());
            s.setDatasetExists(!datasets.isEmpty());
            s.setDatasets(emptyToNull(children(datasets)));
        }
        return s;
    }
}
