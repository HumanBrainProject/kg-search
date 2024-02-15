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
import eu.ebrains.kg.common.model.target.Value;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import eu.ebrains.kg.projects.ebrains.source.SubjectV3;
import eu.ebrains.kg.projects.ebrains.target.Subject;
import eu.ebrains.kg.projects.ebrains.translators.commons.EBRAINSTranslator;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SubjectTranslator extends EBRAINSTranslator<SubjectV3, Subject, SubjectTranslator.Result> {

    public static class Result extends ResultsOfKG<SubjectV3> {
    }

    @Override
    public Class<SubjectV3> getSourceType() {
        return SubjectV3.class;
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
        return Collections.singletonList("068ff65a-dd75-4ebf-8d81-80ed59c1e086");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/Subject");
    }

    public Subject translate(SubjectV3 subject, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        Subject s = new Subject();

        s.setCategory(new Value<>("Subject"));
        s.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));

        if(subject.getDatasets() == null || subject.getDatasets().size()<2){
            //If a subject is not part of multiple datasets, we don't expose it as its own card because there is not
            //enough additional value.
            return null;
        }
        String uuid = IdUtils.getUUID(subject.getId());
        s.setId(uuid);

        s.setAllIdentifiers(subject.getIdentifier());
        s.setIdentifier(IdUtils.getIdentifiersWithPrefix("Subject", subject.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        s.setAge(value(subject.getAge()));
        s.setAgeCategory(emptyToNull(value(subject.getAgeCategory())));
//        s.setDatasetExists(emptyToNull(subject.getDatasetExists()));
        s.setGenotype(value(subject.getGenotype()));
        s.setSex(emptyToNull(value(subject.getSex())));
        s.setSpecies(emptyToNull(value(subject.getSpecies())));
        s.setStrain(value(subject.getStrain()));
        s.setTitle(value(subject.getTitle()));
        s.setWeight(value(subject.getWeight()));

        s.setQueryBuilderText(value(TranslatorUtils.createQueryBuilderText(subject.getPrimaryType(), s.getId())));
//        if(!CollectionUtils.isEmpty(subject.getSamples())) {
//            s.setSamples(subject.getSamples().stream()
//                    .map(sample ->
//                            new TargetInternalReference(
//                                    liveMode ? sample.getRelativeUrl() : sample.getIdentifier(),
//                                    sample.getName(), null)
//                    ).collect(Collectors.toList()));
//        }
//        if(!CollectionUtils.isEmpty(subject.getDatasets())) {
//            s.setDatasets(emptyToNull(subject.getDatasets().stream()
//                    .filter(d -> !(CollectionUtils.isEmpty(d.getComponentName()) && CollectionUtils.isEmpty(d.getInstances())))
//                    .map(d ->
//                            new Subject.Dataset(
//                                    !CollectionUtils.isEmpty(d.getComponentName())?d.getComponentName():null,
//                                    !CollectionUtils.isEmpty(d.getInstances()) ?
//                                            d.getInstances().stream()
//                                                    .map(i ->
//                                                            new TargetInternalReference(
//                                                                    liveMode ? i.getRelativeUrl() : i.getIdentifier(),
//                                                                    i.getName(), null)
//                                                    ).collect(Collectors.toList()) : null
//                            )
//                    ).collect(Collectors.toList())));
//        }


        return s;
    }
}
