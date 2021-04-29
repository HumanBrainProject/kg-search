package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv3.SubjectV3;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Subject;
import eu.ebrains.kg.search.utils.IdUtils;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.emptyToNull;

public class SubjectOfKGV3Translator implements Translator<SubjectV3, Subject>{

    public Subject translate(SubjectV3 subject, DataStage dataStage, boolean liveMode) {
        Subject s = new Subject();

        String uuid = IdUtils.getUUID(subject.getId());
        s.setId(uuid);
        s.setIdentifier(subject.getIdentifier());
        s.setAge(subject.getAge());
        s.setAgeCategory(emptyToNull(subject.getAgeCategory()));
//        s.setDatasetExists(emptyToNull(subject.getDatasetExists()));
        s.setGenotype(subject.getGenotype());
        s.setSex(emptyToNull(subject.getSex()));
        s.setSpecies(emptyToNull(subject.getSpecies()));
        s.setStrain(subject.getStrain());
        s.setTitle(subject.getTitle());
        s.setWeight(subject.getWeight());
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
