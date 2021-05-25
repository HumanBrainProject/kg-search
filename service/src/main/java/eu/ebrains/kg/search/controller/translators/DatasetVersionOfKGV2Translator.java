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

package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.commonsV1andV2.SourceExternalReference;
import eu.ebrains.kg.search.model.source.openMINDSv1.DatasetV1;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.DatasetVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetFile;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static eu.ebrains.kg.search.controller.translators.TranslatorCommons.*;
import static eu.ebrains.kg.search.controller.translators.TranslatorOfKGV2Commons.*;

public class DatasetVersionOfKGV2Translator implements Translator<DatasetV1, DatasetVersion> {

    public DatasetVersion translate(DatasetV1 datasetV1, DataStage dataStage, boolean liveMode) {
        DatasetVersion d = new DatasetVersion();
        String uuid = IdUtils.getUUID(datasetV1.getId());
        d.setId(uuid);
        List<String> identifiers = Arrays.asList(uuid, String.format("Dataset/%s", datasetV1.getIdentifier()));
        d.setIdentifier(identifiers);
        String containerUrl = datasetV1.getContainerUrl();
        boolean containerUrlAsZIP = datasetV1.getContainerUrlAsZIP();
        List<DatasetV1.SourceFile> files = datasetV1.getFiles();
        if (!datasetV1.isUseHDG() && !hasEmbargoStatus(datasetV1, EMBARGOED, UNDER_REVIEW) && (StringUtils.isNotBlank(containerUrl) && (containerUrlAsZIP || CollectionUtils.isEmpty(files)))) {
            d.setZip(new TargetExternalReference(
                    String.format("https://kg.ebrains.eu/proxy/export?container=%s", containerUrl), // TODO: Get rid of empty and containerUrlAsZip condition
                    "Download all related data as ZIP"
            ));
        }
        if (dataStage == DataStage.IN_PROGRESS) {
            d.setEditorId(datasetV1.getEditorId());
        }
        d.setMethods(emptyToNull(datasetV1.getMethods()));
        d.setDescription(datasetV1.getDescription());

        SourceExternalReference license = firstItemOrNull(datasetV1.getLicense());
        if (license != null) {
            d.setLicenseInfo(new TargetExternalReference(license.getUrl(), license.getName()));
        }
        if (!CollectionUtils.isEmpty(datasetV1.getOwners())) {
            d.setCustodians(datasetV1.getOwners().stream()
                    .map(o -> new TargetInternalReference(
                            liveMode ? o.getRelativeUrl() : o.getIdentifier(),
                            o.getName(),
                            null
                    )).collect(Collectors.toList()));
        }
        if (StringUtils.isNotBlank(datasetV1.getDataDescriptorURL())) {
            d.setDataDescriptor(new TargetExternalReference(
                    datasetV1.getDataDescriptorURL(), datasetV1.getDataDescriptorURL()
            ));
        }
        d.setSpeciesFilter(emptyToNull(datasetV1.getSpeciesFilter()));

        if (datasetV1.isUseHDG()) {
            String hdgMessage = String.format("This data requires you to explicitly **[request access](https://hdg.kg.ebrains.eu/request_access?kg_id=%s)** with your EBRAINS account. If you don't have such an account yet, please **[register](https://ebrains.eu/register/)**.", datasetV1.getEditorId());
            String hdgMessageHTML = String.format("This data requires you to explicitly <b><a href=\"https://hdg.kg.ebrains.eu/request_access?kg_id=%s\" target=\"_blank\">request access</a></b> with your EBRAINS account. If you don't have such an account yet, please <b><a href=\"https://ebrains.eu/register/\" target=\"_blank\">register</a></b>.", datasetV1.getEditorId());
            d.setUseHDG(hdgMessage);
            d.setEmbargo(hdgMessageHTML);
            d.setDataAccessibility("Controlled access");
        } else {
            if (dataStage == DataStage.RELEASED) {
                if (hasEmbargoStatus(datasetV1, EMBARGOED)) {
                    d.setEmbargo("This dataset is temporarily under embargo. The data will become available for download after the embargo period.");
                } else if (hasEmbargoStatus(datasetV1, UNDER_REVIEW)) {
                    d.setEmbargo("This dataset is currently reviewed by the Data Protection Office regarding GDPR compliance. The data will be available after this review.");
                }
            }
            d.setDataAccessibility(firstItemOrNull(datasetV1.getEmbargoForFilter()));
            if (!CollectionUtils.isEmpty(datasetV1.getFiles()) && (dataStage == DataStage.IN_PROGRESS || (dataStage == DataStage.RELEASED && !hasEmbargoStatus(datasetV1, EMBARGOED, UNDER_REVIEW)))) {
                d.setFiles(emptyToNull(datasetV1.getFiles().stream()
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
            d.setCitation(citation + String.format(" [DOI: %s]\n[DOI: %s]: https://doi.org/%s", doi, doi, url));
        }


        if (!CollectionUtils.isEmpty(datasetV1.getPublications())) {
            d.setPublications(emptyToNull(datasetV1.getPublications().stream()
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
                    }).filter(Objects::nonNull).collect(Collectors.toList())));
        }
        d.setAtlas(emptyToNull(datasetV1.getParcellationAtlas()));

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
        d.setTitle(datasetV1.getTitle());
        d.setModalityForFilter(emptyToNull(datasetV1.getModalityForFilter()));
        d.setDoi(firstItemOrNull(datasetV1.getDoi()));

        if (!CollectionUtils.isEmpty(datasetV1.getContributors())) {
            d.setContributors(datasetV1.getContributors().stream()
                    .map(c -> new TargetInternalReference(
                            liveMode ? c.getRelativeUrl() : c.getIdentifier(),
                            c.getName(),
                            null
                    )).collect(Collectors.toList()));
        }

        d.setPreparation(emptyToNull(datasetV1.getPreparation()));
        if (!CollectionUtils.isEmpty(datasetV1.getComponent())) {
            d.setProjects(datasetV1.getComponent().stream()
                    .map(c -> new TargetInternalReference(
                            liveMode ? c.getRelativeUrl() : c.getIdentifier(),
                            c.getName(),
                            null
                    )).collect(Collectors.toList()));
        }
        d.setKeywords(emptyToNull(datasetV1.getProtocols()));

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
            d.setSubjects(datasetV1.getSubjects().stream()
                    .map(s ->
                            new DatasetVersion.Subject(
                                    new TargetInternalReference(
                                            liveMode ? s.getRelativeUrl() : s.getIdentifier(),
                                            s.getName(),
                                            null
                                    ),
                                    CollectionUtils.isEmpty(s.getSpecies()) ? null : s.getSpecies(),
                                    CollectionUtils.isEmpty(s.getSex()) ? null : s.getSex(),
                                    s.getAge(),
                                    CollectionUtils.isEmpty(s.getAgeCategory()) ? null : s.getAgeCategory(),
                                    s.getWeight(),
                                    s.getStrain() != null ? s.getStrain() : s.getStrains(),
                                    s.getGenotype(),
                                    !CollectionUtils.isEmpty(s.getSamples()) ? s.getSamples().
                                            stream().
                                            map(sample -> new TargetInternalReference(
                                                    liveMode ? sample.getRelativeUrl() : sample.getIdentifier(),
                                                    sample.getName(),
                                                    null
                                            )).collect(Collectors.toList()) : null
                            )
                    ).collect(Collectors.toList()));
        }
        if (dataStage == DataStage.IN_PROGRESS && !datasetV1.isUseHDG()) {
            if (containerUrl != null && containerUrl.startsWith("https://object.cscs.ch")) {
                if (hasEmbargoStatus(datasetV1, EMBARGOED)) {
                    d.setEmbargoRestrictedAccess(String.format("This dataset is temporarily under embargo. The data will become available for download after the embargo period.<br/><br/>If you are an authenticated user, <a href=\"https://kg.ebrains.eu/files/cscs/list?url=%s\" target=\"_blank\"> you should be able to access the data here</a>", containerUrl));
                } else if (hasEmbargoStatus(datasetV1, UNDER_REVIEW)) {
                    d.setEmbargoRestrictedAccess(String.format("This dataset is currently reviewed by the Data Protection Office regarding GDPR compliance. The data will be available after this review.<br/><br/>If you are an authenticated user, <a href=\"https://kg.ebrains.eu/files/cscs/list?url=%s\" target=\"_blank\"> you should be able to access the data here</a>", containerUrl));
                }
            } else {
                if (hasEmbargoStatus(datasetV1, EMBARGOED)) {
                    d.setEmbargoRestrictedAccess("This dataset is temporarily under embargo. The data will become available for download after the embargo period.");
                } else if (hasEmbargoStatus(datasetV1, UNDER_REVIEW)) {
                    d.setEmbargoRestrictedAccess("This dataset is currently reviewed by the Data Protection Office regarding GDPR compliance. The data will be available after this review.");
                }
            }
        }
        d.setFirstRelease(datasetV1.getFirstReleaseAt());
        d.setLastRelease(datasetV1.getLastReleaseAt());
        return d;
    }
}
