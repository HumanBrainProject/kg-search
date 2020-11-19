package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.source.openMINDSv1.SampleV1;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Sample;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.ExternalReference;

import java.util.stream.Collectors;

public class SampleTranslator implements Translator<SampleV1, Sample> {
    public Sample translate(SampleV1 sample) {
        Sample s = new Sample();
        String title = sample.getTitle();
        s.setTitle(title);
        s.setFirstRelease(sample.getFirstReleaseAt());
        s.setLastRelease(sample.getLastReleaseAt());
        s.setIdentifier(sample.getIdentifier());
        s.setDatasetExists(sample.getDatasetExists());
        s.setEditorId(sample.getEditorId());
        s.setParcellationAtlas(sample.getParcellationAtlas());
        s.setWeightPreFixation(sample.getWeightPreFixation());
        s.setMethods(sample.getMethods());
        s.setRegion(sample.getParcellationRegion().stream()
                .map(r ->
                        new ExternalReference(r.getUrl(), r.getAlias()!=null?r.getAlias():r.getName())
                ).collect(Collectors.toList()));
        s.setViewer(sample.getBrainViewer().stream()
                .map(url ->
                        new ExternalReference(url, title!=null?String.format("Show %s in brain atlas viewer", title):"Show in brain atlas viewer")
                ).collect(Collectors.toList()));
        s.setDatasets(sample.getDatasets().stream()
                .map(d ->
                        new Sample.Dataset(
                                d.getComponentName(),
                                d.getInstances().stream()
                                        .map(i ->
                                                new InternalReference(
                                                        String.format("Dataset/%s", i.getIdentifier()),
                                                        i.getName(), null)
                                        ).collect(Collectors.toList())
                        )
                ).collect(Collectors.toList()));
        s.setSubject(sample.getSubjects().stream()
                .map(d ->
                        new Sample.Subject(
                                new InternalReference(
                                        false?d.getRelativeUrl():String.format("Subject/%s", d.getIdentifier()), // TODO: replace false by isLive
                                        d.getName(),
                                        null
                                ),
                                d.getSpecies(),
                                d.getSex(),
                                d.getAge(),
                                d.getAgeCategory(),
                                d.getWeight(),
                                d.getStrain()!=null?d.getStrain():d.getStrains(),
                                d.getGenotype()
                        )
                ).collect(Collectors.toList()));
        //s.setFiles(sample.getFiles());
        //s.setAllfiles(sample.getFiles());
        return s;
    }
}