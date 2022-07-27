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

package eu.ebrains.kg.common.controller.translators.kgv2;

import eu.ebrains.kg.common.controller.translators.Translator;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.source.ResultsOfKGv2;
import eu.ebrains.kg.common.model.source.openMINDSv1.SampleV1;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.Sample;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetFile;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.services.DOICitationFormatter;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static eu.ebrains.kg.common.controller.translators.TranslatorCommons.emptyToNull;
import static eu.ebrains.kg.common.controller.translators.kgv2.TranslatorOfKGV2Commons.*;

public class SampleV1Translator extends TranslatorV2<SampleV1, Sample, SampleV1Translator.Result> {

    public static class Result extends ResultsOfKGv2<SampleV1> {
    }

    @Override
    public Class<SampleV1> getSourceType() {
        return SampleV1.class;
    }

    @Override
    public Class<Sample> getTargetType() {
        return Sample.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("minds/experiment/sample/v1.0.0");
    }

    public Sample translate(SampleV1 sample, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        Sample s = new Sample();

        s.setCategory(new Value<>("Sample"));
        s.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));

        s.setAllIdentifiers(createList(sample.getIdentifier()));
        s.setId(sample.getIdentifier());
        s.setIdentifier(createList(sample.getIdentifier(), String.format("Sample/%s", sample.getIdentifier())).stream().distinct().collect(Collectors.toList()));
        String title = sample.getTitle();
        s.setTitle(value(title));
        s.setFirstRelease(value(sample.getFirstReleaseAt()));
        s.setLastRelease(value(sample.getLastReleaseAt()));
        if (dataStage == DataStage.IN_PROGRESS) {
            s.setEditorId(value(sample.getEditorId()));
        }
        s.setParcellationAtlas(emptyToNull(value(sample.getParcellationAtlas())));
        s.setWeightPreFixation(value(sample.getWeightPreFixation()));
        s.setMethods(emptyToNull(value(sample.getMethods())));
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
            final List<Sample.Dataset> datasets = sample.getDatasets().stream()
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
                    ).collect(Collectors.toList());
            s.setDatasets(children(datasets));
            s.setDatasetExists(!datasets.isEmpty());
        }
        if (!CollectionUtils.isEmpty(sample.getSubjects())) {
            s.setSubject(children(sample.getSubjects().stream()
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
                    ).collect(Collectors.toList())));
        }
        String containerUrl = sample.getContainerUrl();
        if (!hasEmbargoStatus(sample, EMBARGOED) && !StringUtils.isBlank(containerUrl) && !CollectionUtils.isEmpty(sample.getFiles())) {
            if (containerUrl.startsWith("https://object.cscs.ch")) {
                s.setAllFiles(new TargetExternalReference(
                        String.format("https://data.kg.ebrains.eu/zip?container=%s", containerUrl),
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
                                    f.getPrivateAccess() ? String.format("%s?url=%s", Translator.FILE_PROXY, f.getAbsolutePath()) : f.getAbsolutePath(),
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
