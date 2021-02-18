package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.openMINDSv1.SampleV1;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.Sample;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetFile;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.*;

public class SampleTranslator implements Translator<SampleV1, Sample> {

    public Sample translate(SampleV1 sample, DataStage dataStage, boolean liveMode) {
        Sample s = new Sample();
        String title = sample.getTitle();
        s.setTitle(title);
        s.setFirstRelease(sample.getFirstReleaseAt());
        s.setLastRelease(sample.getLastReleaseAt());
        s.setIdentifier(sample.getIdentifier());
        s.setDatasetExists(emptyToNull(sample.getDatasetExists()));
        if (dataStage == DataStage.IN_PROGRESS) {
            s.setEditorId(sample.getEditorId());
        }
        s.setParcellationAtlas(emptyToNull(sample.getParcellationAtlas()));
        s.setWeightPreFixation(sample.getWeightPreFixation());
        s.setMethods(emptyToNull(sample.getMethods()));
        if (!CollectionUtils.isEmpty(sample.getParcellationRegion())) {
            s.setRegion(sample.getParcellationRegion().stream()
                    .map(r ->
                            new TargetExternalReference(
                                    StringUtils.isBlank(r.getUrl())?null:r.getUrl(),
                                    StringUtils.isBlank(r.getAlias()) ? r.getName() : r.getAlias()
                            )
                    ).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(sample.getBrainViewer())) {
            s.setViewer(sample.getBrainViewer().stream()
                    .map(url ->
                            new TargetExternalReference(url, title != null ? String.format("Show %s in brain atlas viewer", title) : "Show in brain atlas viewer")
                    ).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(sample.getDatasets())) {
            s.setDatasets(sample.getDatasets().stream()
                    .map(d ->
                            new Sample.Dataset(
                                    CollectionUtils.isEmpty(d.getComponentName()) ? null : d.getComponentName(),
                                    d.getInstances().stream()
                                            .map(i ->
                                                    new TargetInternalReference(
                                                            liveMode ? i.getRelativeUrl() : String.format("Dataset/%s", i.getIdentifier()),
                                                            i.getName(), null)
                                            ).collect(Collectors.toList())
                            )
                    ).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(sample.getSubjects())) {
            s.setSubject(sample.getSubjects().stream()
                    .map(d ->
                            new Sample.Subject(
                                    new TargetInternalReference(
                                            liveMode ? d.getRelativeUrl() : String.format("Subject/%s", d.getIdentifier()),
                                            d.getName(),
                                            null
                                    ),
                                    CollectionUtils.isEmpty(d.getSpecies()) ? null : d.getSpecies(),
                                    CollectionUtils.isEmpty(d.getSex()) ? null : d.getSex(),
                                    d.getAge(),
                                    CollectionUtils.isEmpty(d.getAgeCategory()) ? null : d.getAgeCategory(),
                                    d.getWeight(),
                                    d.getStrain() != null ? d.getStrain() : d.getStrains(),
                                    d.getGenotype()
                            )
                    ).collect(Collectors.toList()));
        }
        String containerUrl = sample.getContainerUrl();
        if (!hasEmbargoStatus(sample, EMBARGOED) && !StringUtils.isBlank(containerUrl) && !CollectionUtils.isEmpty(sample.getFiles())) {
            if (containerUrl.startsWith("https://object.cscs.ch")) {
                s.setAllFiles(new TargetExternalReference(
                        String.format("https://kg.ebrains.eu/proxy/export?container=%s", containerUrl),
                        "download all related data as ZIP"
                ));
            } else {
                s.setAllFiles(new TargetExternalReference(
                        containerUrl,
                        "Go to the data."
                ));
            }
        }
        if (!CollectionUtils.isEmpty(sample.getFiles()) && (dataStage == DataStage.IN_PROGRESS || (dataStage == DataStage.RELEASED && !hasEmbargoStatus(sample, EMBARGOED, UNDER_REVIEW)))) {
            s.setFiles(emptyToNull(sample.getFiles().stream()
                    .filter(v -> v.getAbsolutePath() != null && v.getName() != null)
                    .map(f ->
                            new TargetFile(
                                    f.getPrivateAccess() ? String.format("%s/files/cscs?url=%s", Translator.fileProxy, f.getAbsolutePath()) : f.getAbsolutePath(),
                                    f.getPrivateAccess() ? String.format("ACCESS PROTECTED: %s", f.getName()) : f.getName(),
                                    f.getHumanReadableSize(),
                                    null,
                                    getFileImage(f.getPreviewUrl(), !CollectionUtils.isEmpty(f.getPreviewAnimated()) && f.getPreviewAnimated().get(0)), //TODO review,
                                    null
                            )
                    ).collect(Collectors.toList())));
        }
        return s;
    }
}
