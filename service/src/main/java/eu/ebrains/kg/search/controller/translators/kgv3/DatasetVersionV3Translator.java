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

package eu.ebrains.kg.search.controller.translators.kgv3;

import eu.ebrains.kg.search.controller.translators.Helpers;
import eu.ebrains.kg.search.controller.translators.kgv3.commons.Accessibility;
import eu.ebrains.kg.search.controller.translators.kgv3.commons.Constants;
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv3;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetVersionV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.*;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.DatasetVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.*;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.IdUtils;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.websocket.RemoteEndpoint;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//Test ids
// 4ac9f0bc-560d-47e0-8916-7b24da9bb0ce (multiple versions)
// 4840dd00-058b-437c-9d0f-091b482d51b8 (experimental approaches and techniques)

//Subjectgroup with subject group state: 40a998fb-9483-42ad-b46b-2f8d0bc5aa3e
//Subjectgroup with individual subjects and single state 088a7717-76d2-4520-b9e8-3f2fecce1ee4
//Subjectgroup with individual subjects and multiple states (on INT) 4840dd00-058b-437c-9d0f-091b482d51b8
//Direct subjects with single states: ccc680a2-995d-48f7-904a-53a7190c6632

//Tissue sample collection b3d4234a-a014-47d2-8753-c64cb5042e51
//Tissue sample collection with individual samples b4a37f80-e231-4a27-92ca-f47de7b2208d

public class DatasetVersionV3Translator extends TranslatorV3<DatasetVersionV3, DatasetVersion, DatasetVersionV3Translator.Result> {

    private static final Map<String, Map<String, String>> COLOR_TO_TYPE_MAP = Map.of(
            "https://openminds.ebrains.eu/core/TissueSample", Map.of("#4e8c00", "Tissue sample"),
            "https://openminds.ebrains.eu/core/TissueSampleState", Map.of("#345d00", "Tissue sample state"),
            "https://openminds.ebrains.eu/core/TissueSampleCollection", Map.of("#e8ff24", "Tissue sample collection"),
            "https://openminds.ebrains.eu/core/TissueSampleCollectionState", Map.of("#a8ba14", "Tissue sample state"),
            "https://openminds.ebrains.eu/core/Subject", Map.of("#ffbe00", "Subject"),
            "https://openminds.ebrains.eu/core/SubjectState", Map.of("#e28500", "Subject state"),
            "https://openminds.ebrains.eu/core/SubjectGroup", Map.of("#48c9ef", "Subject group"),
            "https://openminds.ebrains.eu/core/SubjectGroupState", Map.of("#4b93e9", "Subject group state")
            );
    private static final Map<String, String> UNKNOWN_COLOR_MAP = Map.of("#939393", "Unknown");
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static class Result extends ResultsOfKGv3<DatasetVersionV3> {
    }

    @Override
    public Class<DatasetVersionV3> getSourceType() {
        return DatasetVersionV3.class;
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
        return Collections.singletonList("e09b4984-5272-431e-8d3b-7d498328d8ee");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList(Constants.OPENMINDS_ROOT + "core/DatasetVersion");
    }

    private boolean isExternalLink(FileRepository repository) {
        return repository != null && repository.getIri() != null && !(repository.getIri().contains("object.cscs.ch") || repository.getIri().contains("data-proxy.ebrains.eu"));
    }

    public DatasetVersion translate(DatasetVersionV3 datasetVersion, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        DatasetVersion d = new DatasetVersion();

        d.setCategory(new Value<>("Dataset"));
        d.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));

        d.setId(datasetVersion.getUUID());
        d.setFirstRelease(value(datasetVersion.getReleaseDate() != null ? datasetVersion.getReleaseDate() : datasetVersion.getFirstReleasedAt()));
        d.setLastRelease(value(datasetVersion.getLastReleasedAt()));
        DatasetVersionV3.DatasetVersions dataset = datasetVersion.getDataset();
        Accessibility accessibility = Accessibility.fromPayload(datasetVersion);
        String containerUrl = datasetVersion.getFileRepository() != null ? datasetVersion.getFileRepository().getIri() : null;
        if (accessibility != null) {
            switch (accessibility) {
                case CONTROLLED_ACCESS:
                    d.setEmbargo(value(DatasetVersion.createHDGMessage(datasetVersion.getUUID(), true)));
                    break;
                case UNDER_EMBARGO:
                    if (dataStage == DataStage.IN_PROGRESS && containerUrl != null) {
                        d.setEmbargoRestrictedAccess(value(DatasetVersion.createEmbargoInProgressMessage(containerUrl)));
                    } else {
                        d.setEmbargo(value(DatasetVersion.EMBARGO_MESSAGE));
                    }
                    break;
                case RESTRICTED_ACCESS:
                    d.setEmbargo(value(DatasetVersion.RESTRICTED_ACCESS_MESSAGE));
                    break;
                default:
                    if (datasetVersion.getFileRepository() != null) {
                        if (isExternalLink(datasetVersion.getFileRepository())) {
                            d.setExternalDatalink(Collections.singletonList(new TargetExternalReference(datasetVersion.getFileRepository().getIri(), datasetVersion.getFileRepository().getIri())));
                        } else {
                            if (datasetVersion.getFileRepository().getFirstFile() == null) {
                                //Although the dataset version is supposed to be accessible, it is not indexed (yet). We're forwarding to data proxy
                                d.setDataProxyLink(new TargetExternalReference(String.format("https://data-proxy.ebrains.eu/datasets/%s", d.getId()), "Browse files"));
                            } else {
                                String endpoint;
                                if (liveMode) {
                                    endpoint = String.format("/api/repositories/%s/files/live", IdUtils.getUUID(datasetVersion.getFileRepository().getId()));
                                } else {
                                    endpoint = String.format("/api/groups/%s/repositories/%s/files", dataStage == DataStage.IN_PROGRESS ? "curated" : "public", IdUtils.getUUID(datasetVersion.getFileRepository().getId()));
                                }
                                d.setFilesAsyncUrl(endpoint);
                            }
                        }
                    }
            }
            d.setDataAccessibility(value(datasetVersion.getAccessibility().getName()));
        }

        d.setExperimentalApproach(ref(datasetVersion.getExperimentalApproach()));
        if (!CollectionUtils.isEmpty(datasetVersion.getExperimentalApproach())) {
            final List<String> experimentalApproachesForFilter = datasetVersion.getExperimentalApproach().stream().map(FullNameRef::getFullName).filter(Objects::nonNull).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(experimentalApproachesForFilter)) {
                d.setModalityForFilter(value(experimentalApproachesForFilter));
            }
        }
        d.setBehavioralProtocols(ref(datasetVersion.getBehavioralProtocol()));
        d.setPreparation(ref(datasetVersion.getPreparationDesign()));
        d.setTechnique(ref(datasetVersion.getTechnique()));
        if (!CollectionUtils.isEmpty(datasetVersion.getTechnique())) {
            final List<String> techniquesForFilter = datasetVersion.getTechnique().stream().map(FullNameRef::getFullName).filter(Objects::nonNull).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(techniquesForFilter)) {
                d.setMethodsForFilter(value(techniquesForFilter));
            }

        }


        d.setAllIdentifiers(datasetVersion.getIdentifier());
        d.setIdentifier(IdUtils.getIdentifiersWithPrefix("Dataset", datasetVersion.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        List<Version> versions = dataset == null ? null : dataset.getVersions();
        boolean hasMultipleVersions = !CollectionUtils.isEmpty(versions) && versions.size() > 1;
        if (hasMultipleVersions) {
            d.setVersion(datasetVersion.getVersion());
            List<Version> sortedVersions = Helpers.sort(versions);
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            references.add(new TargetInternalReference(IdUtils.getUUID(dataset.getId()), "version overview"));
            d.setVersions(references);
            d.setAllVersionRef(new TargetInternalReference(IdUtils.getUUID(dataset.getId()), "version overview"));
            // if versions cannot be sorted (sortedVersions == versions) we flag it as searchable
            d.setSearchable(sortedVersions == versions || sortedVersions.get(0).getId().equals(datasetVersion.getId()));
        } else {
            d.setSearchable(true);
        }

        if (StringUtils.isNotBlank(datasetVersion.getDescription())) {
            d.setDescription(value(datasetVersion.getDescription()));
        } else if (dataset != null) {
            d.setDescription(value(dataset.getDescription()));
        }
        if (StringUtils.isNotBlank(datasetVersion.getVersionInnovation()) && !Constants.VERSION_INNOVATION_DEFAULTS.contains(StringUtils.trim(datasetVersion.getVersionInnovation()).toLowerCase())) {
            d.setNewInThisVersion(new Value<>(datasetVersion.getVersionInnovation()));
        }

        if (StringUtils.isNotBlank(datasetVersion.getFullName())) {
            if (hasMultipleVersions || StringUtils.isBlank(datasetVersion.getVersion())) {
                d.setTitle(value(datasetVersion.getFullName()));
            } else {
                d.setTitle(value(String.format("%s (%s)", datasetVersion.getFullName(), datasetVersion.getVersion())));
            }
        } else if (dataset != null && StringUtils.isNotBlank(dataset.getFullName())) {
            if (hasMultipleVersions || StringUtils.isBlank(datasetVersion.getVersion())) {
                d.setTitle(value(dataset.getFullName()));
            } else {
                d.setTitle(value(String.format("%s (%s)", dataset.getFullName(), datasetVersion.getVersion())));
            }
        }
        if (!CollectionUtils.isEmpty(datasetVersion.getAuthor())) {
            d.setContributors(datasetVersion.getAuthor().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        } else if (dataset != null && !CollectionUtils.isEmpty(dataset.getAuthor())) {
            d.setContributors(dataset.getAuthor().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }

        handleCitation(datasetVersion, d);

        d.setLicenseInfo(link(datasetVersion.getLicense()));

        List<FullNameRef> projects = null;
        if (datasetVersion.getProjects() != null && (dataset != null && dataset.getDatasetProjects() != null)) {
            projects = Stream.concat(datasetVersion.getProjects().stream(), dataset.getDatasetProjects().stream()).distinct().collect(Collectors.toList());
        } else if (datasetVersion.getProjects() != null) {
            projects = datasetVersion.getProjects();
        } else if (dataset != null && dataset.getDatasetProjects() != null) {
            projects = dataset.getDatasetProjects();
        }
        d.setProjects(ref(projects));

        List<PersonOrOrganizationRef> custodians = datasetVersion.getCustodians();
        if (CollectionUtils.isEmpty(custodians) && datasetVersion.getDataset() != null) {
            custodians = datasetVersion.getDataset().getCustodians();
        }

        if (!CollectionUtils.isEmpty(custodians)) {
            d.setCustodians(custodians.stream().map(c -> new TargetInternalReference(IdUtils.getUUID(c.getId()), Helpers.getFullName(c.getFullName(), c.getFamilyName(), c.getGivenName()))).collect(Collectors.toList()));
        }

        if (!CollectionUtils.isEmpty(datasetVersion.getRelatedPublications())) {
            d.setPublications(datasetVersion.getRelatedPublications().stream().map(p -> Helpers.getFormattedDigitalIdentifier(doiCitationFormatter, p.getIdentifier(), p.resolvedType())).filter(Objects::nonNull).map(Value::new).collect(Collectors.toList()));
        }

        if (!CollectionUtils.isEmpty(datasetVersion.getKeyword())) {
            Collections.sort(datasetVersion.getKeyword());
            d.setKeywords(value(datasetVersion.getKeyword()));
        }
        if (!CollectionUtils.isEmpty(datasetVersion.getSubjects())) {
            final Set<String> groupedSubjects = datasetVersion.getSubjects().stream().map(DatasetVersionV3.SubjectOrSubjectGroup::getChildren).filter(children -> !CollectionUtils.isEmpty(children)).flatMap(Collection::stream).map(DatasetVersionV3.SubjectOrSubjectGroup::getId).collect(Collectors.toSet());
            final List<DatasetVersion.SubjectGroupOrSingleSubject> subjects = datasetVersion.getSubjects().stream()
                    //We don't want individual subjects to appear on the root hierarchy level if they also have a representation inside the groups...
                    .filter(s -> !groupedSubjects.contains(s.getId()))
                    .map(s ->
                            {
                                //Some subject groups contain individual subject information. Let's populate the values before we start to translate
                                s.calculateSubjectGroupInformationFromChildren();
                                DatasetVersion.SubjectGroupOrSingleSubject subj = new DatasetVersion.SubjectGroupOrSingleSubject();
                                fillIndividualSubjectInformation(subj, s, null);
                                if (!CollectionUtils.isEmpty(s.getChildren())) {
                                    //This is a subject group with individual information.
                                    subj.setCollapsible(true);
                                    subj.setChildren(s.getChildren().stream().map(child -> fillIndividualSubjectInformation(new DatasetVersion.SingleSubject(), child, subj)).sorted(Comparator.comparing(DatasetVersion.SingleSubject::getLabel)).collect(Collectors.toList()));
                                }
                                subj.setNumberOfSubjects(value(s.getQuantity() != null ? String.valueOf(s.getQuantity()) : null));
                                return subj;
                            }
                    ).sorted(Comparator.comparing(DatasetVersion.SubjectGroupOrSingleSubject::getLabel)).collect(Collectors.toList());
            if (!subjects.isEmpty()) {
                d.setSubjectGroupOrSingleSubject(children(subjects));
            }
        }

        if (!CollectionUtils.isEmpty(datasetVersion.getTissueSampleOrCollection())) {

            final Map<String, String> sampleToGroup = new HashMap<>();
            datasetVersion.getTissueSampleOrCollection().forEach(s -> {
                if (!s.getChildren().isEmpty()) {
                    s.getChildren().forEach(c -> {
                        sampleToGroup.put(c.getId(), s.getId());
                    });
                }
            });

            final Map<String, Set<String>> groupToFilteredSamples = new HashMap<>();
            datasetVersion.getTissueSampleOrCollection().forEach(s -> {
                if (sampleToGroup.containsKey(s.getId())) {
                    String groupId = sampleToGroup.get(s.getId());
                    if (!groupToFilteredSamples.containsKey(groupId)) {
                        groupToFilteredSamples.put(groupId, new HashSet<>());
                    }
                    groupToFilteredSamples.get(groupId).add(s.getId());
                }
            });

            final List<DatasetVersion.TissueSampleOrTissueSampleCollection> tissueSamples = datasetVersion.getTissueSampleOrCollection().stream()
                    //We don't want individual tissue samples to appear on the root hierarchy level if they also have a representation inside the groups...
                    .filter(s -> !sampleToGroup.containsKey(s.getId()))
                    .map(s ->
                            {
                                DatasetVersion.TissueSampleOrTissueSampleCollection tissueSample = new DatasetVersion.TissueSampleOrTissueSampleCollection();
                                fillIndividualTissueSampleInformation(tissueSample, s, null);
                                if (!CollectionUtils.isEmpty(s.getChildren())) {
                                    //This is a tissue sample group with individual information.
                                    tissueSample.setCollapsible(true);
                                    boolean isFiltered = groupToFilteredSamples.containsKey(s.getId());
                                    if (isFiltered) {
                                        tissueSample.setSubset(true);
                                    }
                                    Set<String> filteredSampleIds = groupToFilteredSamples.get(s.getId());
                                    tissueSample.setChildren((s.getChildren().stream().filter(c -> !isFiltered || filteredSampleIds.contains(c.getId()))).map(child -> fillIndividualTissueSampleInformation(new DatasetVersion.SingleTissueSample(), child, tissueSample)).sorted(Comparator.comparing(DatasetVersion.SingleTissueSample::getLabel)).collect(Collectors.toList()));
                                }
                                tissueSample.setNumberOfSamples(value(s.getQuantity() != null ? String.valueOf(s.getQuantity()) : null));
                                return tissueSample;
                            }
                    ).sorted(Comparator.comparing(DatasetVersion.TissueSampleOrTissueSampleCollection::getLabel)).collect(Collectors.toList());
            if (!tissueSamples.isEmpty()) {
                d.setTissueSamples(children(tissueSamples));
            }
        }

        if (datasetVersion.getEthicsAssessment() != null) {
            String ethicsAssessment = null;
            if (datasetVersion.getEthicsAssessment().contains(Constants.OPENMINDS_INSTANCES + "/ethicsAssessment/notRequired")) {
                ethicsAssessment = "not-required";
            } else if (datasetVersion.getEthicsAssessment().contains(Constants.OPENMINDS_INSTANCES + "/ethicsAssessment/EUCompliantNonSensitive") || datasetVersion.getEthicsAssessment().contains(Constants.OPENMINDS_INSTANCES + "/ethicsAssessment/EUCompliantSensitive")) {
                ethicsAssessment = "EU-compliant";
            }
            d.setEthicsAssessment(value(ethicsAssessment));
        }


        final List<File> specialFiles = datasetVersion.getSpecialFiles();

        final List<File> dataDescriptors = specialFiles.stream().filter(s -> s.getRoles().contains(Constants.OPENMINDS_INSTANCES + "/fileUsageRole/dataDescriptor")).collect(Collectors.toList());
        if (!dataDescriptors.isEmpty()) {
            TargetExternalReference reference;
            if (dataDescriptors.size() > 1) {
                logger.error(String.format("The dataset version contains multiple data descriptors: %s - picking the first one", dataDescriptors.stream().map(File::getIri).collect(Collectors.joining(", "))), datasetVersion.getUUID());
                reference = new TargetExternalReference(dataDescriptors.get(0).getIri(), dataDescriptors.get(0).getName());
            } else {
                if (datasetVersion.getFullDocumentationFile() != null && !dataDescriptors.get(0).getIri().equals(datasetVersion.getFullDocumentationFile().getIri())) {
                    logger.error(String.format("The dataset has a file (%s) flagged with the role data descriptor and another one (%s) for the full documentation. Falling back to the full documentation file!", dataDescriptors.get(0).getIri(), datasetVersion.getFullDocumentationFile().getIri()), datasetVersion.getUUID());
                    reference = new TargetExternalReference(datasetVersion.getFullDocumentationFile().getIri(), datasetVersion.getFullDocumentationFile().getName());
                } else if (datasetVersion.getFullDocumentationDOI() != null) {
                    logger.error(String.format("The dataset has a file (%s) flagged with the role data descriptor and a DOI (%s) for the full documentation. Falling back to the full documentation DOI!", dataDescriptors.get(0).getIri(), datasetVersion.getFullDocumentationDOI()), datasetVersion.getUUID());
                    reference = new TargetExternalReference(datasetVersion.getFullDocumentationDOI(), datasetVersion.getFullDocumentationDOI());
                } else if (datasetVersion.getFullDocumentationUrl() != null) {
                    logger.error(String.format("The dataset has a file (%s) flagged with the role data descriptor and a URL (%s) for the full documentation. Falling back to the full documentation URL!", dataDescriptors.get(0).getIri(), datasetVersion.getFullDocumentationUrl()), datasetVersion.getUUID());
                    reference = new TargetExternalReference(datasetVersion.getFullDocumentationUrl(), datasetVersion.getFullDocumentationUrl());
                } else {
                    reference = new TargetExternalReference(dataDescriptors.get(0).getIri(), dataDescriptors.get(0).getName());
                }
            }
            d.setDataDescriptor(reference);
        } else if (datasetVersion.getFullDocumentationFile() != null) {
            d.setDataDescriptor(new TargetExternalReference(datasetVersion.getFullDocumentationFile().getIri(), datasetVersion.getFullDocumentationFile().getName()));
        } else if (datasetVersion.getFullDocumentationUrl() != null) {
            d.setDataDescriptor(new TargetExternalReference(datasetVersion.getFullDocumentationUrl(), datasetVersion.getFullDocumentationUrl()));
        } else if (datasetVersion.getFullDocumentationDOI() != null) {
            d.setDataDescriptor(new TargetExternalReference(datasetVersion.getFullDocumentationDOI(), datasetVersion.getFullDocumentationDOI()));
        }

        List<String> videoExtensions = Arrays.asList(".mp4");
        List<String> imageExtensions = Arrays.asList(".gif", ".jpg", ".jpeg", ".png");


        final List<File> previewFiles = specialFiles.stream().filter(s -> s.getRoles().contains(Constants.OPENMINDS_INSTANCES + "/fileUsageRole/preview") || s.getRoles().contains(Constants.OPENMINDS_INSTANCES + "/fileUsageRole/screenshot")).collect(Collectors.toList());
        final List<File> previewImages = previewFiles.stream().filter(f -> imageExtensions.stream().anyMatch(i -> f.getIri().toLowerCase().endsWith(i))).collect(Collectors.toList());
        final Map<String, File> previewImagesByFileNameWithoutExtension = previewImages.stream().collect(Collectors.toMap(this::stripFileExtension, v -> v));


        List<DatasetVersion.PreviewObject> previews = previewFiles.stream().filter(f -> videoExtensions.stream().anyMatch(e -> f.getIri().toLowerCase().endsWith(e))).map(f -> {
            DatasetVersion.PreviewObject o = new DatasetVersion.PreviewObject();
            o.setVideoUrl(f.getIri());
            final File staticPreviewImage = previewImagesByFileNameWithoutExtension.get(stripFileExtension(f));
            if (staticPreviewImage != null) {
                o.setImageUrl(staticPreviewImage.getIri());
                previewImages.remove(staticPreviewImage);
            }
            if (StringUtils.isNotBlank(f.getContentDescription())) {
                o.setDescription(f.getContentDescription());
            }
            return o;
        }).collect(Collectors.toList());


        if (!CollectionUtils.isEmpty(datasetVersion.getServiceLinks()) || !CollectionUtils.isEmpty(datasetVersion.getServiceLinksFromFiles())) {
            //Service links for file bundles
            previews.addAll(Stream.concat(datasetVersion.getServiceLinks().stream(), datasetVersion.getServiceLinksFromFiles().stream()).map(l -> {
                if (l.getFile() != null) {
                    final File staticPreviewImage = previewImagesByFileNameWithoutExtension.get(stripFileExtension(l.getFile()));
                    if (staticPreviewImage != null) {
                        DatasetVersion.PreviewObject previewObject = new DatasetVersion.PreviewObject();
                        if (l.getUrl() != null) {
                            previewObject.setLink(new TargetExternalReference(l.getUrl(), l.displayLabel()));
                        }
                        if (l.getLabel() != null) {
                            previewObject.setDescription(l.getLabel());
                        }
                        previewObject.setImageUrl(staticPreviewImage.getIri());
                        previewImages.remove(staticPreviewImage);
                        return previewObject;
                    }
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList()));

            Map<String, List<TargetExternalReference>> viewData = new HashMap<>();
            Stream.concat(datasetVersion.getServiceLinks().stream(), datasetVersion.getServiceLinksFromFiles().stream()).forEach(s -> {
                if (!viewData.containsKey(s.getService())) {
                    viewData.put(s.getService(), new ArrayList<>());
                }
                List<TargetExternalReference> targetExternalReferences = viewData.get(s.getService());
                targetExternalReferences.add(new TargetExternalReference(s.getUrl(), s.getLabel()));
                targetExternalReferences.sort(Comparator.comparing(TargetExternalReference::getValue));
            });
            d.setViewData(viewData);
        }

        previews.addAll(previewImages.stream().map(i -> {
            DatasetVersion.PreviewObject o = new DatasetVersion.PreviewObject();
            o.setImageUrl(i.getIri());
            if (StringUtils.isNotBlank(i.getContentDescription())) {
                o.setDescription(i.getContentDescription());
            }
            return o;
        }).collect(Collectors.toList()));

        //TODO Sorting
        d.setPreviewObjects(previews);

        List<String> brainRegionStudyTargets = Arrays.asList(Constants.OPENMINDS_ROOT + "controlledTerms/UBERONParcellation", Constants.OPENMINDS_ROOT + "sands/ParcellationEntityVersion", Constants.OPENMINDS_ROOT + "sands/ParcellationEntity", Constants.OPENMINDS_ROOT + "sands/CustomAnatomicalEntity");

        final Map<Boolean, List<StudyTarget>> brainRegionOrNot = datasetVersion.getStudyTarget().stream().collect(Collectors.groupingBy(s -> s.getStudyTargetType() != null && s.getStudyTargetType().stream().anyMatch(brainRegionStudyTargets::contains)));
        d.setStudyTargets(refVersion(brainRegionOrNot.get(Boolean.FALSE), false));
        if (!CollectionUtils.isEmpty(brainRegionOrNot.get(Boolean.TRUE))) {
            d.setStudiedBrainRegion(brainRegionOrNot.get(Boolean.TRUE).stream().map(this::refAnatomical).collect(Collectors.toList()));
        }

        final List<TargetInternalReference> collectedAnatomicalLocations = d.getTissueSamples() == null ? Collections.emptyList() : d.getTissueSamples().stream().filter(Objects::nonNull).map(Children::getChildren).filter(Objects::nonNull).map(DatasetVersion.AbstractTissueSampleOrTissueSampleCollection::getAnatomicalLocation).filter(Objects::nonNull).flatMap(Collection::stream).distinct().sorted().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(collectedAnatomicalLocations)) {
            d.setAnatomicalLocationOfTissueSamples(collectedAnatomicalLocations);
        }

        d.setContentTypes(value(datasetVersion.getContentTypes()));

        final List<TargetInternalReference> speciesFromSG = d.getSubjectGroupOrSingleSubject() != null ? d.getSubjectGroupOrSingleSubject().stream().filter(sg -> sg.getChildren() != null).map(sg -> sg.getChildren().getSpecies()).filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull).distinct().collect(Collectors.toList()) : Collections.emptyList();
        final List<TargetInternalReference> speciesFromTS = d.getTissueSamples() != null ? d.getTissueSamples().stream().filter(ts -> ts.getChildren() != null).map(ts -> ts.getChildren().getSpecies()).filter(Objects::nonNull).flatMap(Collection::stream).filter(Objects::nonNull).distinct().collect(Collectors.toList()) : Collections.emptyList();
        List<TargetInternalReference> species = Stream.concat(speciesFromSG.stream(), speciesFromTS.stream()).distinct().collect(Collectors.toList());
        d.setSpeciesFilter(value(species.stream().map(TargetInternalReference::getValue).filter(Objects::nonNull).collect(Collectors.toList())));

        if (StringUtils.isNotBlank(datasetVersion.getHomepage())) {
            d.setHomepage(new TargetExternalReference(datasetVersion.getHomepage(), datasetVersion.getHomepage()));
        }

        if (!CollectionUtils.isEmpty(datasetVersion.getSupportChannels())) {
            d.setSupportChannels(datasetVersion.getSupportChannels().stream().map(supportChannel -> {
                supportChannel = supportChannel.trim();
                if (StringUtils.isNotBlank(supportChannel)) {
                    if (!supportChannel.startsWith("http://") &&
                            !supportChannel.startsWith("https://") &&
                            Pattern.compile("^(.+)@(\\S+)$").matcher(supportChannel).matches()) {
                        return new TargetExternalReference("mailto:" + supportChannel, supportChannel);
                    }
                    return new TargetExternalReference(supportChannel, supportChannel);
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList()));
        }

        d.setSpecimenBySubject(specimenBySubject(datasetVersion.getStudiedSpecimen()));
        return d;
    }


    private Set<DatasetVersionV3.StudiedState> findDescendentStates(DatasetVersionV3.StudiedState root, List<DatasetVersionV3.StudiedSpecimen> studiedSpecimen) {
        return studiedSpecimen.stream().map(DatasetVersionV3.StudiedSpecimen::getStudiedState).flatMap(Collection::stream).filter(st -> st.getDescendedFrom().contains(root.getId())).collect(Collectors.toSet());
    }

    private List<DatasetVersionV3.StudiedSpecimen> findPartOfRelations(DatasetVersionV3.StudiedSpecimen root, List<DatasetVersionV3.StudiedSpecimen> studiedSpecimen) {
        return studiedSpecimen.stream().filter(s -> s.getIsPartOf().contains(root.getId())).collect(Collectors.toList());
    }

    private DatasetVersion.DSVSubject translateSubject(DatasetVersionV3.StudiedSpecimen specimen){
        final DatasetVersion.DSVSubject sub = new DatasetVersion.DSVSubject();
        sub.setLabel(value(specimen.getInternalIdentifier()));
        return sub;
    }

    private DatasetVersion.DSVSubjectState translateSubjectState(DatasetVersionV3.StudiedState state){
        final DatasetVersion.DSVSubjectState sub = new DatasetVersion.DSVSubjectState();
        sub.setLabel(value(state.getParent().getInternalIdentifier()));
        return sub;
    }

    private DatasetVersion.DSVSubjectGroup translateSubjectGroup(DatasetVersionV3.StudiedSpecimen specimen){
        final DatasetVersion.DSVSubjectGroup subGrp = new DatasetVersion.DSVSubjectGroup();
        subGrp.setLabel(value(specimen.getInternalIdentifier()));
        return subGrp;
    }

    private DatasetVersion.DSVSubjectGroupState translateSubjectGroupState(DatasetVersionV3.StudiedState state){
        final DatasetVersion.DSVSubjectGroupState subGrp = new DatasetVersion.DSVSubjectGroupState();
        subGrp.setLabel(value(state.getParent().getInternalIdentifier()));
        return subGrp;
    }

    private DatasetVersion.DSVTissueSample translateTissueSample(DatasetVersionV3.StudiedSpecimen specimen){
        final DatasetVersion.DSVTissueSample tissueSample = new DatasetVersion.DSVTissueSample();
        tissueSample.setLabel(value(specimen.getInternalIdentifier()));
        return tissueSample;
    }

    private DatasetVersion.DSVTissueSampleState translateTissueSampleState(DatasetVersionV3.StudiedState state){
        final DatasetVersion.DSVTissueSampleState tissueSampleState = new DatasetVersion.DSVTissueSampleState();
        tissueSampleState.setLabel(value(state.getParent().getInternalIdentifier()));
        return tissueSampleState;
    }

    private DatasetVersion.DSVTissueSampleCollection translateTissueSampleCollection(DatasetVersionV3.StudiedSpecimen specimen){
        final DatasetVersion.DSVTissueSampleCollection tissueSampleCollection = new DatasetVersion.DSVTissueSampleCollection();
        tissueSampleCollection.setLabel(value(specimen.getInternalIdentifier()));
        return tissueSampleCollection;
    }

    private DatasetVersion.DSVTissueSampleCollectionState translateTissueSampleCollectionState(DatasetVersionV3.StudiedState state){
        final DatasetVersion.DSVTissueSampleCollectionState tissueSampleCollectionState = new DatasetVersion.DSVTissueSampleCollectionState();
        tissueSampleCollectionState.setLabel(value(state.getParent().getInternalIdentifier()));
        return tissueSampleCollectionState;
    }

    private BasicHierarchyElement translateToBasicHierarchyElement(DatasetVersionV3.StudiedState studiedState, Map<String, Set<DatasetVersionV3.StudiedState>> descendentStatesMap, Map<String, List<DatasetVersionV3.StudiedSpecimen>> partOfRelations, Map<String, String> legendCollector, boolean attachRootElementAsChild){
        BasicHierarchyElement elState = new BasicHierarchyElement();
        elState.setKey(IdUtils.getUUID(studiedState.getId()));
        elState.setTitle(studiedState.getLookupLabel());
        elState.setColor(findColor(studiedState.getType(), legendCollector));
        if(studiedState.getType().contains("https://openminds.ebrains.eu/core/SubjectState")){
            elState.setData(translateSubjectState(studiedState));
        }
        else if(studiedState.getType().contains("https://openminds.ebrains.eu/core/SubjectGroupState")){
            elState.setData(translateSubjectGroupState(studiedState));
        }
        else if(studiedState.getType().contains("https://openminds.ebrains.eu/core/TissueSampleState")){
            elState.setData(translateTissueSampleState(studiedState));
        }
        else if(studiedState.getType().contains("https://openminds.ebrains.eu/core/TissueSampleCollectionState")){
            elState.setData(translateTissueSampleCollectionState(studiedState));
        }
        final Set<DatasetVersionV3.StudiedState> descendentStates = descendentStatesMap.get(studiedState.getId());
        final List<BasicHierarchyElement> children = descendentStates.stream().map(st -> translateToBasicHierarchyElement(st, descendentStatesMap, partOfRelations, legendCollector, true)).collect(Collectors.toList());
        if(attachRootElementAsChild && studiedState.getParent()!=null){
            final BasicHierarchyElement parent = translateToBasicHierarchyElement(studiedState.getParent(), descendentStatesMap, partOfRelations, legendCollector, true);
            if(canMergeHierarchyElements(studiedState.getParent())){
                elState = mergeBasicHierarchyElements(parent, elState, children, true);
            }
            else{
                children.add(parent);
            }
        }
        if(!CollectionUtils.isEmpty(children)) {
            elState.setChildren(children);
        }
        return elState;
    }

    // If there is only a single state available for this studied specimen, we can simplify the structure by merging it.
    private boolean canMergeHierarchyElements(DatasetVersionV3.StudiedSpecimen specimen){
        return specimen.getStudiedState().size() == 1;
    }

    private BasicHierarchyElement mergeBasicHierarchyElements(BasicHierarchyElement parent, BasicHierarchyElement child, List<BasicHierarchyElement> children, boolean childFirst){
        BasicHierarchyElement el = new BasicHierarchyElement();
        el.setKey(parent.getKey());
        //If we can merge an element, it means that there is only a single state and we therefore don't need a "state label" to distinguish. We therefore can just use the parent label.
        el.setTitle(String.format("%s (merged)", parent.getTitle()));
        el.setColor(child.getColor());
        el.setData(child.getData());
        if(parent.getChildren()!=null){
            children.addAll(parent.getChildren().stream().filter(c -> !children.contains(c)).collect(Collectors.toList()));
        }
        if(child.getChildren()!=null){
            children.addAll(child.getChildren().stream().filter(c -> !children.contains(c)).collect(Collectors.toList()));
        }
        return el;
    }

    private String findColor(List<String> types, Map<String, String> legendCollector){
        final Map<String, String> foundMap = types.stream().map(COLOR_TO_TYPE_MAP::get).filter(Objects::nonNull).findFirst().orElse(UNKNOWN_COLOR_MAP);
        final String color = foundMap.keySet().iterator().next();
        legendCollector.put(color, foundMap.get(color));
        return color;
    }

    private BasicHierarchyElement translateToBasicHierarchyElement(DatasetVersionV3.StudiedSpecimen studiedSpecimen, Map<String, Set<DatasetVersionV3.StudiedState>> descendentStates, Map<String, List<DatasetVersionV3.StudiedSpecimen>> partOfRelations, Map<String, String> legendCollector, boolean attachRootElementAsChild){
        BasicHierarchyElement el = new BasicHierarchyElement();
        el.setKey(IdUtils.getUUID(studiedSpecimen.getId()));
        el.setTitle(StringUtils.defaultString(studiedSpecimen.getInternalIdentifier(), studiedSpecimen.getLookupLabel()));
        el.setColor(findColor(studiedSpecimen.getType(), legendCollector));
        if(studiedSpecimen.getType().contains("https://openminds.ebrains.eu/core/Subject")){
            el.setData(translateSubject(studiedSpecimen));
        }
        else if(studiedSpecimen.getType().contains("https://openminds.ebrains.eu/core/SubjectGroup")){
            el.setData(translateSubjectGroup(studiedSpecimen));
        }
        else if(studiedSpecimen.getType().contains("https://openminds.ebrains.eu/core/TissueSample")){
            el.setData(translateTissueSample(studiedSpecimen));
        }
        else if(studiedSpecimen.getType().contains("https://openminds.ebrains.eu/core/TissueSampleCollection")){
            el.setData(translateTissueSampleCollection(studiedSpecimen));
        }

        List<BasicHierarchyElement> children = new ArrayList<>();
        final List<DatasetVersionV3.StudiedSpecimen> partOfRelation = partOfRelations.get(studiedSpecimen.getId());
        if(!CollectionUtils.isEmpty(partOfRelation)){
            children.addAll(partOfRelation.stream().map(p -> translateToBasicHierarchyElement(p, descendentStates, partOfRelations, legendCollector, false)).collect(Collectors.toList()));
        }
        if(!attachRootElementAsChild){
            if(canMergeHierarchyElements(studiedSpecimen)){
                final BasicHierarchyElement singleState = translateToBasicHierarchyElement(studiedSpecimen.getStudiedState().get(0), descendentStates, partOfRelations, legendCollector, false);
                el = mergeBasicHierarchyElements(el, singleState, children, false);
            }
            else {
                children.addAll(studiedSpecimen.getStudiedState().stream().map(st -> translateToBasicHierarchyElement(st, descendentStates, partOfRelations, legendCollector, false)).collect(Collectors.toList()));
            }
        }
        if(!CollectionUtils.isEmpty(children)){
            el.setChildren(children);
        }
        return el;
    }


    //fe3da318-c8aa-4c83-ba64-1f0c74b00700
    //0bf058d2-6bf7-4e0f-8067-345e07109bf8
    //d71d369a-c401-4d7e-b97a-3fb78eed06c5
    //7f6e14f0-ab5c-4328-9e0e-01b260edd357
    //4660e79b-a731-40ac-905e-46d0d11c0dd5


    //TODO shall we support the indirect linking of subjects and subject groups or do we require them to be stated explictly?
    //Tissue sample only (indirect link to subjects)
    //89ddf976-e732-4eef-be48-08af62cfe40b
    //65a1a3ca-a8e7-4bc0-a60b-3cffc9122b0e
    //51ad4f55-6fe2-4513-85fc-449fead4c998
    //98541109-aa4a-4813-85e2-2ad915659553
    //df91d605-e261-4c3b-a954-2acd9adec042
    //bb024285-ab31-48fa-86ff-5450b5e64cd7
    //1899b724-1100-43ba-940f-4aab44080e9f
    //0abb83f5-07f1-41b8-bdb3-f245f4c4e673
    //95f44822-6247-4d7a-a232-23a5247dd91d
    //103068ab-8993-4f0c-94a8-b55a6b99f109
    //b4a37f80-e231-4a27-92ca-f47de7b2208d
    //1cfea99c-b9ee-4748-b900-6ed6db944435
    //2d3757b5-afc8-470d-988e-f382884cf382
    //62bb226b-47f7-4294-bdff-66662d86e4d5

    private BasicHierarchyElement specimenBySubject(List<DatasetVersionV3.StudiedSpecimen> studiedSpecimen) {
        BasicHierarchyElement e = new BasicHierarchyElement();
        e.setKey("root");
        e.setTitle("Specimen");
        final Set<String> explicitlyStatedSpecimen = studiedSpecimen.stream().map(DatasetVersionV3.StudiedSpecimen::getId).collect(Collectors.toSet());

        final Stream<DatasetVersionV3.StudiedSpecimen> implicitSubElements = studiedSpecimen.stream().map(DatasetVersionV3.StudiedSpecimen::getSubElements)
                // If one of the sub elements is stated explicitly, we interpret this as a specific subselection for a dataset and are
                // not taking into account any other potential elements which are part of the general collection.
                .filter(subElements -> !CollectionUtils.isEmpty(subElements) && subElements.stream().noneMatch(sub -> explicitlyStatedSpecimen.contains(sub.getId())))
                .flatMap(Collection::stream);
        final List<DatasetVersionV3.StudiedSpecimen> collectedSpecimen = Stream.concat(studiedSpecimen.stream(), implicitSubElements).collect(Collectors.toList());

        //Remove all "isPartOf" relationships for those studiedSpecimen which are not explicitly defined, since we want
        //all tissue sample collections and subject groups to be stated explicitly if they shall be visualized
        collectedSpecimen.forEach(s ->s.getIsPartOf().removeIf(partOf -> !explicitlyStatedSpecimen.contains(partOf)));

        //Also remove all descended from relations which are not resolved (e.g. because its counterpart is not mapped as a study target)
        final Set<String> allStateIds = collectedSpecimen.stream().map(s -> s.getStudiedState().stream().map(DatasetVersionV3.StudiedState::getId).collect(Collectors.toSet())).flatMap(Collection::stream).collect(Collectors.toSet());
        collectedSpecimen.forEach(s -> s.getStudiedState().forEach(st -> st.getDescendedFrom().removeIf(descFrom -> !allStateIds.contains(descFrom))));

        final Map<String, Set<DatasetVersionV3.StudiedState>> descendentStates = collectedSpecimen.stream().map(s -> s.getStudiedState().stream().peek(st -> st.setParent(s)).collect(Collectors.toList())).flatMap(Collection::stream).collect(Collectors.toMap(DatasetVersionV3.StudiedState::getId, v -> findDescendentStates(v, collectedSpecimen)));
        final Map<String, List<DatasetVersionV3.StudiedSpecimen>> partOfRelations = collectedSpecimen.stream().collect(Collectors.toMap(DatasetVersionV3.StudiedSpecimen::getId, v -> findPartOfRelations(v, collectedSpecimen)));

        TreeMap<String, String> legendCollector = new TreeMap<>();
        //Start the recursive resolution with the root elements (the ones that either don't have states at all or whose states are disconnected)
        e.setChildren(studiedSpecimen.stream().filter(s -> CollectionUtils.isEmpty(s.getIsPartOf()) && s.getStudiedState().stream().allMatch(st -> CollectionUtils.isEmpty(st.getDescendedFrom()))).map(s -> translateToBasicHierarchyElement(s, descendentStates, partOfRelations, legendCollector, false)).collect(Collectors.toList()));
        e.setLegend(legendCollector);
        return e;
    }


    private String stripFileExtension(File file) {
        return stripFileExtension(file.getIri());
    }

    private String stripFileExtension(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    private static <U, T> boolean sameAsParent(Function<? super T, ? extends U> f, T child, T parent) {
        if (child == null || parent == null) {
            return false;
        }
        final U childValue = f.apply(child);
        U parentValue = f.apply(parent);
        if (!(childValue instanceof List) && parentValue instanceof List && ((List<?>) parentValue).size() == 1) {
            parentValue = (U) ((List<?>) parentValue).get(0);
        }
        return (childValue == null && parentValue == null) || (childValue != null && childValue.equals(parentValue));
    }

    @SuppressWarnings("java:S3740") // we keep the generics intentionally
    private <T extends DatasetVersion.AbstractTissueSampleOrTissueSampleCollection> T fillIndividualTissueSampleInformation(T tissueSample, DatasetVersionV3.TissueSampleOrTissueSampleCollection t, DatasetVersion.AbstractTissueSampleOrTissueSampleCollection parent) {
        String type = "Tissue sample";
        if (t.getTissueSampleType() != null && t.getTissueSampleType().contains(Constants.OPENMINDS_ROOT + "core/TissueSampleCollection")) {
            type = "Tissue sample collection";
        }
        tissueSample.setLabel(new TargetInternalReference(IdUtils.getUUID(t.getId()), t.getInternalIdentifier() != null ? String.format("%s %s", type, t.getInternalIdentifier()) : type));
        tissueSample.setSex(ref(t.getBiologicalSex()));
        tissueSample.setTsType(ref(t.getTsType()));
        List<FullNameRef> species = t.getSpecies();
        if (CollectionUtils.isEmpty(species)) {
            species = t.getStrain().stream().map(DatasetVersionV3.Strain::getSpecies).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        }
        tissueSample.setSpecies(ref(species));
        tissueSample.setStrain(ref(t.getStrain()));
        if (t.getStrain() != null) {
            tissueSample.setGeneticStrainType(ref(t.getStrain().stream().map(DatasetVersionV3.Strain::getGeneticStrainType).filter(Objects::nonNull).collect(Collectors.toList())));
        }
        tissueSample.setOrigin(ref(t.getOrigin()));
        tissueSample.setLaterality(ref(t.getLaterality()));
        if (!CollectionUtils.isEmpty(t.getAnatomicalLocation())) {
            tissueSample.setAnatomicalLocation(t.getAnatomicalLocation().stream().filter(Objects::nonNull).map(this::refAnatomical).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(t.getStates())) {
            if (t.getStates().size() > 1) {
                //If we have more than one state, we're going to expand them.
                tissueSample.setChildren(t.getStates().stream().map(state -> fillTissueSampleStateInformation(t.getInternalIdentifier() != null ? t.getInternalIdentifier() : null, state)).collect(Collectors.toList()));
            } else {
                final DatasetVersionV3.SpecimenOrSpecimenGroupState onlyState = t.getStates().get(0);
                if (onlyState.getAge() != null) {
                    final Value<String> age = value(onlyState.getAge().displayString());
                    tissueSample.setAge(age == null ? null : Collections.singletonList(age));
                }
                final List<TargetInternalReference> ageCategories = ref(onlyState.getAgeCategory());
                tissueSample.setAgeCategory(CollectionUtils.isEmpty(ageCategories) ? null : Collections.singletonList(ageCategories));
                if (onlyState.getWeight() != null) {
                    final Value<String> weight = value(onlyState.getWeight().displayString());
                    tissueSample.setWeight(weight == null ? null : Collections.singletonList(weight));
                }
                final List<TargetInternalReference> ref = ref(onlyState.getPathology());
                tissueSample.setPathology(CollectionUtils.isEmpty(ref) ? null : Collections.singletonList(ref));
            }
        }
        if (sameAsParent(DatasetVersion.AbstractTissueSampleOrTissueSampleCollection::getTsType, tissueSample, parent)) {
            tissueSample.setTsType(null);
        }
        if (sameAsParent(DatasetVersion.AbstractTissueSampleOrTissueSampleCollection::getGeneticStrainType, tissueSample, parent)) {
            tissueSample.setGeneticStrainType(null);
        }
        if (sameAsParent(DatasetVersion.AbstractTissueSampleOrTissueSampleCollection::getSex, tissueSample, parent)) {
            tissueSample.setSex(null);
        }
        if (sameAsParent(DatasetVersion.AbstractTissueSampleOrTissueSampleCollection::getSpecies, tissueSample, parent)) {
            tissueSample.setSpecies(null);
        }
        if (sameAsParent(DatasetVersion.AbstractTissueSampleOrTissueSampleCollection::getStrain, tissueSample, parent)) {
            tissueSample.setStrain(null);
        }
        if (sameAsParent(DatasetVersion.AbstractTissueSampleOrTissueSampleCollection::getOrigin, tissueSample, parent)) {
            tissueSample.setOrigin(null);
        }
        if (sameAsParent(DatasetVersion.AbstractTissueSampleOrTissueSampleCollection::getLaterality, tissueSample, parent)) {
            tissueSample.setLaterality(null);
        }
        if (sameAsParent(DatasetVersion.AbstractTissueSampleOrTissueSampleCollection::getAnatomicalLocation, tissueSample, parent)) {
            tissueSample.setAnatomicalLocation(null);
        }
        return tissueSample;
    }

    @SuppressWarnings("java:S3740") // we keep the generics intentionally
    private <T extends DatasetVersion.AbstractSubject> T fillIndividualSubjectInformation(T subj, DatasetVersionV3.SubjectOrSubjectGroup s, DatasetVersion.AbstractSubject parent) {
        String type = "Subject";
        if (s.getSubjectType() != null && s.getSubjectType().contains(Constants.OPENMINDS_ROOT + "core/SubjectGroup")) {
            type = "Subject group";
        }
        subj.setLabel(new TargetInternalReference(IdUtils.getUUID(s.getId()), s.getInternalIdentifier() != null ? String.format("%s %s", type, s.getInternalIdentifier()) : type));
        List<FullNameRef> species = s.getSpecies();
        if (CollectionUtils.isEmpty(species)) {
            species = s.getStrain().stream().map(DatasetVersionV3.Strain::getSpecies).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        }
        subj.setSpecies(ref(species));
        subj.setStrain(ref(s.getStrain()));
        if (s.getStrain() != null) {
            subj.setGeneticStrainType(ref(s.getStrain().stream().map(DatasetVersionV3.Strain::getGeneticStrainType).filter(Objects::nonNull).collect(Collectors.toList())));
        }
        subj.setSex(ref(s.getBiologicalSex()));
        if (!CollectionUtils.isEmpty(s.getStates())) {
            if (s.getStates().size() > 1) {
                //If we have more than one state, we're going to expand them.
                subj.setChildren(s.getStates().stream().map(state -> fillSubjectStateInformation(s.getInternalIdentifier() != null ? s.getInternalIdentifier() : null, state)).collect(Collectors.toList()));
            } else {
                final DatasetVersionV3.SpecimenOrSpecimenGroupState onlyState = s.getStates().get(0);
                if (onlyState.getAge() != null) {
                    subj.setAge(Collections.singletonList(value(onlyState.getAge().displayString())));
                }
                subj.setAgeCategory(Collections.singletonList(ref(onlyState.getAgeCategory())));
                if (onlyState.getWeight() != null) {
                    subj.setWeight(Collections.singletonList(value(onlyState.getWeight().displayString())));
                }
            }
        }
        if (sameAsParent(DatasetVersion.AbstractSubject::getSpecies, subj, parent)) {
            subj.setSpecies(null);
        }
        if (sameAsParent(DatasetVersion.AbstractSubject::getStrain, subj, parent)) {
            subj.setStrain(null);
        }
        if (sameAsParent(DatasetVersion.AbstractSubject::getSex, subj, parent)) {
            subj.setSex(null);
        }
        if (sameAsParent(DatasetVersion.AbstractSubject::getAge, subj, parent)) {
            subj.setAge(null);
        }
        if (sameAsParent(DatasetVersion.AbstractSubject::getAttributes, subj, parent)) {
            subj.setAttributes(null);
        }
        if (sameAsParent(DatasetVersion.AbstractSubject::getAgeCategory, subj, parent)) {
            subj.setAgeCategory(null);
        }
        if (sameAsParent(DatasetVersion.AbstractSubject::getGeneticStrainType, subj, parent)) {
            subj.setGeneticStrainType(null);
        }
        if (sameAsParent(DatasetVersion.AbstractSubject::getWeight, subj, parent)) {
            subj.setWeight(null);
        }

        return subj;
    }

    private DatasetVersion.TissueSampleState fillTissueSampleStateInformation(String subjectName, DatasetVersionV3.SpecimenOrSpecimenGroupState state) {
        DatasetVersion.TissueSampleState result = new DatasetVersion.TissueSampleState();
        result.setLabel(subjectName != null ? value(String.format("Tissue sample state of %s", subjectName)) : value("Tissue sample state"));
        if (state.getAge() != null) {
            result.setAge(value(state.getAge().displayString()));
        }
        result.setAgeCategory(ref(state.getAgeCategory()));
        if (state.getWeight() != null) {
            result.setWeight(value(state.getWeight().displayString()));
        }
        result.setPathology(ref(state.getPathology()));
        return result;
    }

    private DatasetVersion.SubjectState fillSubjectStateInformation(String subjectName, DatasetVersionV3.SpecimenOrSpecimenGroupState state) {
        DatasetVersion.SubjectState result = new DatasetVersion.SubjectState();
        result.setLabel(subjectName != null ? value(String.format("Subject state of %s", subjectName)) : value("Subject state"));
        if (state.getAge() != null) {
            result.setAge(value(state.getAge().displayString()));
        }
        result.setAdditionalRemarks(value(state.getAdditionalRemarks()));
        result.setAttributes(value(state.getAttribute()));
        result.setAgeCategory(ref(state.getAgeCategory()));
        if (state.getWeight() != null) {
            result.setWeight(value(state.getWeight().displayString()));
        }
        return result;
    }
}
