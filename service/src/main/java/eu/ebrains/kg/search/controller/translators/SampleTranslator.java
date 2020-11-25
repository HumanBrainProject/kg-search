package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DatabaseScope;
import eu.ebrains.kg.search.model.source.openMINDSv1.SampleV1;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Sample;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetFile;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;

import java.util.stream.Collectors;

public class SampleTranslator implements Translator<SampleV1, Sample> {
//TODO Ioannis
    public Sample translate(SampleV1 sample, DatabaseScope databaseScope, boolean liveMode) {
        Sample s = new Sample();
        String title = sample.getTitle();
        String embargo = (sample.getEmbargo() != null && !sample.getEmbargo().isEmpty())?sample.getEmbargo().get(0):null;
        s.setTitle(title);
        s.setFirstRelease(sample.getFirstReleaseAt());
        s.setLastRelease(sample.getLastReleaseAt());
        s.setIdentifier(sample.getIdentifier());
        s.setDatasetExists(sample.getDatasetExists());
        if (databaseScope == DatabaseScope.INFERRED) {
            s.setEditorId(sample.getEditorId());
        }
        s.setParcellationAtlas(sample.getParcellationAtlas());
        s.setWeightPreFixation(sample.getWeightPreFixation());
        s.setMethods(sample.getMethods());
        s.setRegion(sample.getParcellationRegion().stream()
                .map(r ->
                        new TargetExternalReference(r.getUrl(), r.getAlias() != null ? r.getAlias() : r.getName())
                ).collect(Collectors.toList()));
        s.setViewer(sample.getBrainViewer().stream()
                .map(url ->
                        new TargetExternalReference(url, title != null ? String.format("Show %s in brain atlas viewer", title) : "Show in brain atlas viewer")
                ).collect(Collectors.toList()));
        s.setDatasets(sample.getDatasets().stream()
                .map(d ->
                        new Sample.Dataset(
                                d.getComponentName(),
                                d.getInstances().stream()
                                        .map(i ->
                                                new TargetInternalReference(
                                                        liveMode ? i.getRelativeUrl() : String.format("Dataset/%s", i.getIdentifier()),
                                                        i.getName(), null)
                                        ).collect(Collectors.toList())
                        )
                ).collect(Collectors.toList()));
        s.setSubject(sample.getSubjects().stream()
                .map(d ->
                        new Sample.Subject(
                                new TargetInternalReference(
                                        liveMode ? d.getRelativeUrl() : String.format("Subject/%s", d.getIdentifier()),
                                        d.getName(),
                                        null
                                ),
                                d.getSpecies(),
                                d.getSex(),
                                d.getAge(),
                                d.getAgeCategory(),
                                d.getWeight(),
                                d.getStrain() != null ? d.getStrain() : d.getStrains() ,
                                d.getGenotype()
                        )
                ).collect(Collectors.toList()));
        String containerUrl = sample.getContainerUrl();
        if (embargo != null && !embargo.equals("Embargoed")) {
            if (containerUrl.startsWith("https://object.cscs.ch")) {
                s.setAllFiles(new TargetExternalReference(
                        String.format("https://kg.ebrains.eu/proxy/export?container=%s", containerUrl),
                        "Download all related data as ZIP"
                ));
            } else {
                s.setAllFiles(new TargetExternalReference(
                        containerUrl,
                        "Go to the data."
                ));
            }
        }
        if (databaseScope == DatabaseScope.INFERRED || (databaseScope == DatabaseScope.RELEASED && (embargo == null || (!embargo.equals("Embargoed") && !embargo.equals("Under review"))))) {
            s.setFiles(sample.getFiles().stream()
                    .filter(v -> v.getAbsolutePath() != null && v.getName() != null)
                    .map(f ->
                            new TargetFile(
                                    f.getPrivateAccess() ? String.format("%s/files/cscs?url=%s", Translator.fileProxy, f.getAbsolutePath()) : f.getAbsolutePath(),
                                    f.getPrivateAccess() ? String.format("ACCESS PROTECTED: %s", f.getName()) : f.getName(),
                                    f.getHumanReadableSize()
                            )
                    ).collect(Collectors.toList()));
        }
        return s;
    }
}