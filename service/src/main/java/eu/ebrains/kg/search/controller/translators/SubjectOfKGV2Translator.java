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
import eu.ebrains.kg.search.model.source.openMINDSv1.SubjectV1;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Subject;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.emptyToNull;

public class SubjectOfKGV2Translator implements Translator<SubjectV1, Subject> {

    public Subject translate(SubjectV1 subject, DataStage dataStage, boolean liveMode) {
        Subject s = new Subject();
        String uuid = IdUtils.getUUID(subject.getId());
        s.setId(uuid);
        List<String> identifiers = Arrays.asList(uuid, String.format("Subject/%s", subject.getIdentifier()));
        s.setIdentifier(identifiers);
        s.setAge(subject.getAge());
        s.setAgeCategory(emptyToNull(subject.getAgeCategory()));
        if (dataStage == DataStage.IN_PROGRESS) {
            s.setEditorId(subject.getEditorId());
        }
        s.setFirstRelease(subject.getFirstReleaseAt());
        s.setLastRelease(subject.getLastReleaseAt());
        s.setGenotype(subject.getGenotype());
        s.setSex(emptyToNull(subject.getSex()));
        s.setSpecies(emptyToNull(subject.getSpecies()));
        s.setStrain(subject.getStrain() != null ? subject.getStrain() : subject.getStrains());
        s.setTitle(subject.getTitle());
        s.setWeight(subject.getWeight());
        if(!CollectionUtils.isEmpty(subject.getSamples())) {
            s.setSamples(subject.getSamples().stream()
                    .map(sample ->
                            new TargetInternalReference(
                                    liveMode ? sample.getRelativeUrl() : sample.getIdentifier(),
                                    sample.getName(), null)
                    ).collect(Collectors.toList()));
        }
        if(!CollectionUtils.isEmpty(subject.getDatasets())) {
            s.setDatasets(emptyToNull(subject.getDatasets().stream()
                    .filter(d -> !(CollectionUtils.isEmpty(d.getComponentName()) && CollectionUtils.isEmpty(d.getInstances())))
                    .map(d ->
                            new Subject.Dataset(
                                    !CollectionUtils.isEmpty(d.getComponentName())?d.getComponentName():null,
                                    !CollectionUtils.isEmpty(d.getInstances()) ?
                                    d.getInstances().stream()
                                            .map(i ->
                                                    new TargetInternalReference(
                                                            liveMode ? i.getRelativeUrl() : i.getIdentifier(),
                                                            i.getName(), null)
                                            ).collect(Collectors.toList()) : null
                            )
                    ).collect(Collectors.toList())));
        }
        return s;
    }
}
