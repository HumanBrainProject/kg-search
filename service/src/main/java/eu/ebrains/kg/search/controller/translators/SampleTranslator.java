package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.source.openMINDSv1.SampleV1;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Sample;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.ExternalReference;

import java.util.stream.Collectors;

public class SampleTranslator implements Translator<SampleV1, Sample> {
    public Sample translate(SampleV1 sample) {
        Sample s = new Sample();
        s.setTitle(sample.getTitle());
        s.setFirstRelease(sample.getFirstReleaseAt());
        s.setLastRelease(sample.getLastReleaseAt());
        s.setIdentifier(sample.getIdentifier());
        s.setDatasetExists(sample.getDatasetExists());
        s.setEditorId(sample.getEditorId());
//        s.setAllfiles(sample.getFiles());
//        s.setDatasets(sample.getDatasets());
//        s.setMethods(sample.getMethods());
//        s.setParcellationAtlas(sample.getParcellationAtlas());
//        s.setRegion(...);
//        s.setSubject(sample.getSubjects());
//        s.setViewer(sample.getBrainViewer());
//        s.setWeightPreFixation(sample.getWeightPreFixation());
        return s;
    }
}