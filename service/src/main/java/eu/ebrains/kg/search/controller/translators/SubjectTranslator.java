package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.openMINDSv1.SubjectV1;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Subject;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;

import java.util.stream.Collectors;


public class SubjectTranslator implements Translator<SubjectV1, Subject> {

    public Subject translate(SubjectV1 subject, DatabaseScope databaseScope, boolean liveMode) {
        Subject s = new Subject();
        s.setAge(subject.getAge());
        s.setAgeCategory(subject.getAgeCategory());
        s.setDatasetExists(subject.getDatasetExists());
        if (databaseScope == DatabaseScope.INFERRED) {
            s.setEditorId(subject.getEditorId());
        }
        s.setFirstRelease(subject.getFirstReleaseAt());
        s.setLastRelease(subject.getLastReleaseAt());
        s.setGenotype(subject.getGenotype());
        s.setIdentifier(subject.getIdentifier());
        s.setSex(subject.getSex());
        s.setSpecies(subject.getSpecies());
        s.setStrain(subject.getStrain()!=null?subject.getStrain():subject.getStrains());
        s.setTitle(subject.getTitle());
        s.setWeight(subject.getWeight());
        s.setSamples(subject.getSamples().stream()
                .map(sample ->
                        new TargetInternalReference(
                                liveMode ? sample.getRelativeUrl() : String.format("Sample/%s", sample.getIdentifier()),
                                sample.getName(), null)
                ).collect(Collectors.toList()));
        s.setDatasets(subject.getDatasets().stream()
                .map(d ->
                        new Subject.Dataset(
                                d.getComponentName(),
                                d.getInstances().stream()
                                        .map(i ->
                                                new TargetInternalReference(
                                                        liveMode ? i.getRelativeUrl() : String.format("Dataset/%s", i.getIdentifier()),
                                                        i.getName(), null)
                                        ).collect(Collectors.toList())
                        )
                ).collect(Collectors.toList()));
        return s;
    }
}
