package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.openMINDSv1.SubjectV1;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Subject;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import org.springframework.util.CollectionUtils;

import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.*;

public class SubjectTranslator implements Translator<SubjectV1, Subject> {

    public Subject translate(SubjectV1 subject, DatabaseScope databaseScope, boolean liveMode) {
        Subject s = new Subject();
        s.setAge(subject.getAge());
        s.setAgeCategory(emptyToNull(subject.getAgeCategory()));
        s.setDatasetExists(emptyToNull(subject.getDatasetExists()));
        if (databaseScope == DatabaseScope.INFERRED) {
            s.setEditorId(subject.getEditorId());
        }
        s.setFirstRelease(subject.getFirstReleaseAt());
        s.setLastRelease(subject.getLastReleaseAt());
        s.setGenotype(subject.getGenotype());
        s.setIdentifier(subject.getIdentifier());
        s.setSex(emptyToNull(subject.getSex()));
        s.setSpecies(emptyToNull(subject.getSpecies()));
        s.setStrain(subject.getStrain() != null ? subject.getStrain() : subject.getStrains());
        s.setTitle(subject.getTitle());
        s.setWeight(subject.getWeight());
        if(!CollectionUtils.isEmpty(subject.getSamples())) {
            s.setSamples(subject.getSamples().stream()
                    .map(sample ->
                            new TargetInternalReference(
                                    liveMode ? sample.getRelativeUrl() : String.format("Sample/%s", sample.getIdentifier()),
                                    sample.getName(), null)
                    ).collect(Collectors.toList()));
        }
        if(!CollectionUtils.isEmpty(subject.getDatasets())) {
            s.setDatasets(subject.getDatasets().stream()
                    .filter(d -> !(CollectionUtils.isEmpty(d.getComponentName()) && CollectionUtils.isEmpty(d.getInstances())))
                    .map(d ->
                            new Subject.Dataset(
                                    !CollectionUtils.isEmpty(d.getComponentName())?d.getComponentName():null,
                                    !CollectionUtils.isEmpty(d.getInstances()) ?
                                    d.getInstances().stream()
                                            .map(i ->
                                                    new TargetInternalReference(
                                                            liveMode ? i.getRelativeUrl() : String.format("Dataset/%s", i.getIdentifier()),
                                                            i.getName(), null)
                                            ).collect(Collectors.toList()) : null
                            )
                    ).collect(Collectors.toList()));
        }
        return s;
    }
}
