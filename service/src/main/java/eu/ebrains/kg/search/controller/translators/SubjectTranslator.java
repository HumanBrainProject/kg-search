package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv1.SubjectV1;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Subject;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.ESHelper;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.*;

public class SubjectTranslator implements Translator<SubjectV1, Subject> {

    public Subject translate(SubjectV1 subject, DataStage dataStage, boolean liveMode) {
        Subject s = new Subject();
        String uuid = ESHelper.getUUID(subject.getId());
        s.setId(uuid);
        List<String> identifiers = Arrays.asList(uuid, String.format("Subject/%s", subject.getIdentifier()));
        s.setIdentifier(identifiers);
        s.setAge(subject.getAge());
        s.setAgeCategory(emptyToNull(subject.getAgeCategory()));
        s.setDatasetExists(emptyToNull(subject.getDatasetExists()));
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
