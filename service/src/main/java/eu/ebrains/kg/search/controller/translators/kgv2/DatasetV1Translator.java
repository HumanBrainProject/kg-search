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

package eu.ebrains.kg.search.controller.translators.kgv2;

import eu.ebrains.kg.search.controller.translators.Translator;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv2;
import eu.ebrains.kg.search.model.source.commonsV1andV2.SourceExternalReference;
import eu.ebrains.kg.search.model.source.openMINDSv1.DatasetV1;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.DatasetVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetFile;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.emptyToNull;
import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.firstItemOrNull;
import static eu.ebrains.kg.search.controller.translators.kgv2.TranslatorOfKGV2Commons.*;

public class DatasetV1Translator extends TranslatorV2<DatasetV1, DatasetVersion, DatasetV1Translator.Result> {
    public static class Result extends ResultsOfKGv2<DatasetV1> {
    }

    @Override
    public Class<DatasetV1> getSourceType() {
        return DatasetV1.class;
    }

    @Override
    public Class<DatasetVersion> getTargetType() {
        return DatasetVersion.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("minds/core/dataset/v1.0.0");
    }


    public DatasetVersion translate(DatasetV1 datasetV1, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        DatasetVersion d = new DatasetVersion();
        d.setId(datasetV1.getIdentifier());
        d.setAllIdentifiers(createList(datasetV1.getIdentifier()));
        List<String> identifiers = createList(datasetV1.getIdentifier(), String.format("Dataset/%s", datasetV1.getIdentifier()));
        d.setIdentifier(identifiers.stream().distinct().collect(Collectors.toList()));
        String containerUrl = datasetV1.getContainerUrl();
        List<DatasetV1.SourceFile> files = datasetV1.getFiles();
        if (!datasetV1.isUseHDG() && !hasEmbargoStatus(datasetV1, EMBARGOED, UNDER_REVIEW) && StringUtils.isNotBlank(containerUrl) && CollectionUtils.isEmpty(datasetV1.getExternalDatalink()) && CollectionUtils.isEmpty(files)) {
            d.setDownloadAsZip(new TargetExternalReference(
                    String.format("https://kg.ebrains.eu/proxy/export?container=%s", containerUrl), // TODO: Get rid of empty and containerUrlAsZip condition
                    "Download all related data as ZIP"
            ));
        }
        if (dataStage == DataStage.IN_PROGRESS) {
            d.setEditorId(value(datasetV1.getEditorId()));
        }
        d.setMethods(value(emptyToNull(datasetV1.getMethods())));
        d.setMethodsForFilter(value(emptyToNull(datasetV1.getMethods())));
        d.setDescription(value(datasetV1.getDescription()));

        SourceExternalReference license = firstItemOrNull(datasetV1.getLicense());
        if (license != null) {
            d.setLicenseInfo(new TargetExternalReference(license.getUrl(), license.getName()));
        }
        if (!CollectionUtils.isEmpty(datasetV1.getOwners())) {
            d.setCustodians(datasetV1.getOwners().stream()
                    .map(o -> new TargetInternalReference(
                            liveMode ? o.getRelativeUrl() : String.format("Contributor/%s", o.getIdentifier()),
                            o.getName(),
                            null
                    )).collect(Collectors.toList()));
        }
        if (StringUtils.isNotBlank(datasetV1.getDataDescriptorURL())) {
            d.setDataDescriptor(new TargetExternalReference(
                    datasetV1.getDataDescriptorURL(), datasetV1.getDataDescriptorURL()
            ));
        }
        d.setSpeciesFilter(value(emptyToNull(datasetV1.getSpeciesFilter())));

        if (datasetV1.isUseHDG()) {
            final String editorId = datasetV1.getEditorId();
            final String[] split = editorId.split("/");
            String uuid = split[split.length - 1];
            d.setEmbargo(value(DatasetVersion.createHDGMessage(uuid, true)));
            d.setDataAccessibility(translateAccessibility(value("Controlled access")));
        } else {
            if (dataStage == DataStage.RELEASED) {
                if (hasEmbargoStatus(datasetV1, EMBARGOED)) {
                    d.setEmbargo(value(DatasetVersion.EMBARGO_MESSAGE));
                } else if (hasEmbargoStatus(datasetV1, UNDER_REVIEW)) {
                    d.setEmbargo(value("This dataset is currently reviewed by the Data Protection Office regarding GDPR compliance. The data will be available after this review."));
                }
            }
            d.setDataAccessibility(translateAccessibility(value(firstItemOrNull(datasetV1.getEmbargoForFilter()))));
            if (!CollectionUtils.isEmpty(datasetV1.getFiles()) && (dataStage == DataStage.IN_PROGRESS || (dataStage == DataStage.RELEASED && !hasEmbargoStatus(datasetV1, EMBARGOED, UNDER_REVIEW)))) {
                d.setFilesOld(emptyToNull(datasetV1.getFiles().stream()
                        .filter(v -> v.getAbsolutePath() != null && v.getName() != null)
                        .map(f ->
                                new TargetFile(
                                        f.getPrivateAccess() ? String.format("%s/files/cscs?url=%s", Translator.fileProxy, f.getAbsolutePath()) : f.getAbsolutePath(),
                                        f.getPrivateAccess() ? String.format("ACCESS PROTECTED: %s", f.getName()) : f.getName(),
                                        f.getHumanReadableSize(),
                                        getFileImage(f.getStaticImageUrl(), false),
                                        getFileImage(f.getPreviewUrl(), !f.getPreviewAnimated().isEmpty() && f.getPreviewAnimated().get(0)), //TODO review
                                        getFileImage(f.getThumbnailUrl(), false)
                                )
                        ).collect(Collectors.toList())));
            }
        }

        String citation = firstItemOrNull(datasetV1.getCitation());
        String doi = firstItemOrNull(datasetV1.getDoi());
        if (StringUtils.isNotBlank(citation) && StringUtils.isNotBlank(doi)) {
            String url = URLEncoder.encode(doi, StandardCharsets.UTF_8);
            d.setCitation(value(citation + String.format(" [DOI: %s]\n[DOI: %s]: https://doi.org/%s", doi, doi, url)));
        }


        if (!CollectionUtils.isEmpty(datasetV1.getPublications())) {
            d.setPublications(value(emptyToNull(datasetV1.getPublications().stream()
                    .map(publication -> {
                        String publicationResult = null;
                        if (StringUtils.isNotBlank(publication.getCitation()) && StringUtils.isNotBlank(publication.getDoi())) {
                            String url = URLEncoder.encode(publication.getDoi(), StandardCharsets.UTF_8);
                            publicationResult = publication.getCitation() + "\n" + String.format("[DOI: %s]\n[DOI: %s]: https://doi.org/%s", publication.getDoi(), publication.getDoi(), url);
                        } else if (StringUtils.isNotBlank(publication.getCitation()) && StringUtils.isBlank(publication.getDoi())) {
                            publicationResult = publication.getCitation().trim().replaceAll(",$", "");
                        } else if (StringUtils.isNotBlank(publication.getDoi())) {
                            String url = URLEncoder.encode(publication.getDoi(), StandardCharsets.UTF_8);
                            publicationResult = String.format("[DOI: %s]\n[DOI: %s]: https://doi.org/%s", publication.getDoi(), publication.getDoi(), url);
                        }
                        return publicationResult;
                    }).filter(Objects::nonNull).collect(Collectors.toList()))));
        }
        d.setAtlas(value(emptyToNull(datasetV1.getParcellationAtlas())));

        if (!CollectionUtils.isEmpty(datasetV1.getExternalDatalink())) {
            d.setExternalDatalink(datasetV1.getExternalDatalink().stream()
                    .map(ed -> new TargetExternalReference(ed, ed)).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(datasetV1.getParcellationRegion())) {
            d.setRegion(datasetV1.getParcellationRegion().stream()
                    .map(r -> new TargetExternalReference(
                            StringUtils.isNotBlank(r.getUrl()) ? r.getUrl() : null,
                            StringUtils.isBlank(r.getAlias()) ? r.getName() : r.getAlias()))
                    .collect(Collectors.toList()));
        }
        d.setTitle(value(datasetV1.getTitle()));
        d.setModality(value(emptyToNull(datasetV1.getModalityForFilter())));
        d.setModalityForFilter(value(emptyToNull(datasetV1.getModalityForFilter())));
        d.setDoi(value(firstItemOrNull(datasetV1.getDoi())));

        if (!CollectionUtils.isEmpty(datasetV1.getContributors())) {
            d.setContributors(datasetV1.getContributors().stream()
                    .map(c -> new TargetInternalReference(
                            liveMode ? c.getRelativeUrl() : String.format("Contributor/%s", c.getIdentifier()),
                            c.getName(),
                            null
                    )).collect(Collectors.toList()));
        }

        d.setPreparation(emptyRef(emptyToNull(datasetV1.getPreparation())));
        if (!CollectionUtils.isEmpty(datasetV1.getComponent())) {
            d.setProjects(datasetV1.getComponent().stream()
                    .map(c -> new TargetInternalReference(
                            liveMode ? c.getRelativeUrl() : String.format("Project/%s", c.getIdentifier()),
                            c.getName(),
                            null
                    )).collect(Collectors.toList()));
        }
        d.setKeywords(value(emptyToNull(datasetV1.getProtocols())));

        if (!CollectionUtils.isEmpty(datasetV1.getBrainViewer())) {
            d.setViewer(datasetV1.getBrainViewer().stream()
                    .map(v -> new TargetExternalReference(
                            v.getUrl(),
                            StringUtils.isBlank(v.getName()) ? "Show in brain atlas viewer" : String.format("Show %s in brain atlas viewer", v.getName())
                    )).collect(Collectors.toList()));
        } else if (!CollectionUtils.isEmpty(datasetV1.getNeuroglancer())) {
            d.setViewer(datasetV1.getNeuroglancer().stream()
                    .filter(n -> StringUtils.isNotBlank(n.getUrl()))
                    .map(n ->
                            new TargetExternalReference(String.format("https://neuroglancer.humanbrainproject.org/?%s", n.getUrl()),
                                    String.format("Show %s in brain atlas viewer", StringUtils.isNotBlank(n.getName()) ? n.getName() : datasetV1.getTitle())
                            )).collect(Collectors.toList()));
        }

        if (!CollectionUtils.isEmpty(datasetV1.getSubjects())) {
            d.setSubjectGroupOrSingleSubjectOld(children(datasetV1.getSubjects().stream()
                    .map(s ->
                            new DatasetVersion.OldSubject(
                                    new TargetInternalReference(
                                            liveMode ? s.getRelativeUrl() : String.format("Subject/%s", s.getIdentifier()),
                                            s.getName(),
                                            null
                                    ),
                                    CollectionUtils.isEmpty(s.getSpecies()) ? null : value(s.getSpecies()),
                                    CollectionUtils.isEmpty(s.getSex()) ? null : value(s.getSex()),
                                    value(s.getAge()),
                                    CollectionUtils.isEmpty(s.getAgeCategory()) ? null : value(s.getAgeCategory()),
                                    value(s.getWeight()),
                                    s.getStrain() != null ? value(s.getStrain()) : value(s.getStrains()),
                                    value(s.getGenotype()),
                                    !CollectionUtils.isEmpty(s.getSamples()) ? s.getSamples().
                                            stream().
                                            map(sample -> new TargetInternalReference(
                                                    liveMode ? sample.getRelativeUrl() : String.format("Sample/%s", sample.getIdentifier()),
                                                    sample.getName(),
                                                    null
                                            )).collect(Collectors.toList()) : null
                            )
                    ).collect(Collectors.toList())));
        }
        if (dataStage == DataStage.IN_PROGRESS && !datasetV1.isUseHDG()) {
            if (containerUrl != null && containerUrl.startsWith("https://object.cscs.ch")) {
                if (hasEmbargoStatus(datasetV1, EMBARGOED)) {
                    d.setEmbargoRestrictedAccess(value(String.format("This dataset is temporarily under embargo. The data will become available for download after the embargo period.<br/><br/>If you are an authenticated user, <a href=\"https://kg.ebrains.eu/files/cscs/list?url=%s\" target=\"_blank\"> you should be able to access the data here</a>", containerUrl)));
                } else if (hasEmbargoStatus(datasetV1, UNDER_REVIEW)) {
                    d.setEmbargoRestrictedAccess(value(String.format("This dataset is currently reviewed by the Data Protection Office regarding GDPR compliance. The data will be available after this review.<br/><br/>If you are an authenticated user, <a href=\"https://kg.ebrains.eu/files/cscs/list?url=%s\" target=\"_blank\"> you should be able to access the data here</a>", containerUrl)));
                }
            } else {
                if (hasEmbargoStatus(datasetV1, EMBARGOED)) {
                    d.setEmbargoRestrictedAccess(value("This dataset is temporarily under embargo. The data will become available for download after the embargo period."));
                } else if (hasEmbargoStatus(datasetV1, UNDER_REVIEW)) {
                    d.setEmbargoRestrictedAccess(value("This dataset is currently reviewed by the Data Protection Office regarding GDPR compliance. The data will be available after this review."));
                }
            }
        }
        d.setFirstRelease(value(datasetV1.getFirstReleaseAt()));
        d.setLastRelease(value(datasetV1.getLastReleaseAt()));
        d.setSearchable(true);
        return d;
    }

    private Value<String> translateAccessibility(Value<String> oldValue) {
        if (oldValue != null && oldValue.getValue() != null) {
            final String value = oldValue.getValue();
            switch (value.toLowerCase()) {
                case "free":
                    return value("free access");
                case "embargoed":
                    return value("under embargo");
            }
        }
        return oldValue;
    }
}
