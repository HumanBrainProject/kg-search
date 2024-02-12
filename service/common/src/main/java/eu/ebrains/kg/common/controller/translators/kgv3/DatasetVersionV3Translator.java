/*
 *  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *  Copyright 2021 - 2023 EBRAINS AISBL
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This open source software code was developed in part or in whole in the
 *  Human Brain Project, funded from the European Union's Horizon 2020
 *  Framework Programme for Research and Innovation under
 *  Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 *  (Human Brain Project SGA1, SGA2 and SGA3).
 */

package eu.ebrains.kg.common.controller.translators.kgv3;

import eu.ebrains.kg.common.controller.translators.Helpers;
import eu.ebrains.kg.common.controller.translators.kgv3.commons.Accessibility;
import eu.ebrains.kg.common.controller.translators.kgv3.commons.Constants;
import eu.ebrains.kg.common.controller.translators.kgv3.helpers.SchemaOrgConverter;
import eu.ebrains.kg.common.controller.translators.kgv3.helpers.SpecimenV3Resolver;
import eu.ebrains.kg.common.controller.translators.kgv3.helpers.SpecimenV3Translator;
import eu.ebrains.kg.common.model.DOIReference;
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.source.ResultsOfKGv3;
import eu.ebrains.kg.common.model.source.openMINDSv3.DatasetVersionV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.*;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.DatasetVersion;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.*;
import eu.ebrains.kg.common.services.ESServiceClient;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.MetaBadgeUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
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

    private final int TAGS_RESOLVED = 5;

    private final String SPECIMEN_LOOKUP_MAP = "specimenLookupMap";
    @Override
    public Map<String, Object> populateTranslationContext(ESServiceClient esServiceClient, DataStage stage) {
        return Collections.singletonMap(SPECIMEN_LOOKUP_MAP, new SpecimenV3Resolver(esServiceClient).loadSpecimenLookupMap(stage));
    }

    private Map<String, Set<DOIReference>> getSpecimenLookupMapFromContext(TranslatorUtils translatorUtils){
        if (translatorUtils.getTranslationContext()!=null) {
            final Object o = translatorUtils.getTranslationContext().get(SPECIMEN_LOOKUP_MAP);
            if(o instanceof Map) {
                return  (Map<String, Set<DOIReference>>) o;
            }
        }
        return Collections.emptyMap();
    }

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

    private String getRestrictedAccessMessage(String title, String id) {
        String restrictedAccessEndpoint = String.format("https://nettskjema.no/a/127835?CBDatasetTitle=%s&LCKDatasetTitle=true&CBDatasetID=%s&LCKDatasetID=true", title, id);
        return String.format("These data are access restricted and hosted by the data provider. <a class=\"btn btn-secondary\" style=color:#fff href=\"%s\" target=\"_blank\">Request access</a>", restrictedAccessEndpoint);

    }

    private String getTitle(DatasetVersionV3 datasetVersion, DatasetVersionV3.DatasetVersions dataset, boolean hasMultipleVersions) {
        String title = null;
        if (StringUtils.isNotBlank(datasetVersion.getFullName())) {
            if (hasMultipleVersions || StringUtils.isBlank(datasetVersion.getVersion())) {
                title = datasetVersion.getFullName();
            } else {
                title = String.format("%s (%s)", datasetVersion.getFullName(), datasetVersion.getVersion());
            }
        } else if (dataset != null && StringUtils.isNotBlank(dataset.getFullName())) {
            if (hasMultipleVersions || StringUtils.isBlank(datasetVersion.getVersion())) {
                title = dataset.getFullName();
            } else {
                title = String.format("%s (%s)", dataset.getFullName(), datasetVersion.getVersion());
            }
        }
        return title;
    }

    private void defineTags(DatasetVersionV3 datasetVersion, DatasetVersion d) {
        int total = 0;
        List<String> tags = new ArrayList<>();
        if (!CollectionUtils.isEmpty(datasetVersion.getKeyword())) {
            if (datasetVersion.getKeyword().size() > TAGS_RESOLVED) {
                tags.addAll(datasetVersion.getKeyword().subList(0,TAGS_RESOLVED));
            } else {
                tags.addAll(datasetVersion.getKeyword());
            }
            total += datasetVersion.getKeyword().size();
        }
        if (!CollectionUtils.isEmpty(datasetVersion.getStudyTarget())) {
            if (datasetVersion.getStudyTarget().size() > TAGS_RESOLVED) {
                tags.addAll(datasetVersion.getStudyTarget().stream().map(FullNameRef::getFullName).toList().subList(0,TAGS_RESOLVED));
            } else {
                tags.addAll(datasetVersion.getStudyTarget().stream().map(FullNameRef::getFullName).toList());
            }
            total += datasetVersion.getStudyTarget().size();
        }
        if (!CollectionUtils.isEmpty(datasetVersion.getTechnique())) {
            if (datasetVersion.getTechnique().size() > TAGS_RESOLVED) {
                tags.addAll(datasetVersion.getTechnique().stream().map(FullNameRef::getFullName).toList().subList(0,TAGS_RESOLVED));
            } else {
                tags.addAll(datasetVersion.getTechnique().stream().map(FullNameRef::getFullName).toList());
            }
            total += datasetVersion.getTechnique().size();
        }
        if (!CollectionUtils.isEmpty(tags)) {
            tags.sort(String.CASE_INSENSITIVE_ORDER);
            d.setTags(new DatasetVersion.Tags(tags, total, tags.size(), 0));
        }
    }

    public DatasetVersion translate(DatasetVersionV3 datasetVersion, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        DatasetVersion d = new DatasetVersion();
        logger.debug("Translating {}", datasetVersion.getId());
        d.setCategory(new Value<>("Dataset"));
        d.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));
        final Date releaseDate = datasetVersion.getReleaseDate() != null && datasetVersion.getReleaseDate().before(new Date()) ? datasetVersion.getReleaseDate() : datasetVersion.getFirstReleasedAt();
        final String releaseDateForSorting = translatorUtils.getReleasedDateForSorting(datasetVersion.getIssueDate(), releaseDate);
        defineTags(datasetVersion, d);
        String uuid = datasetVersion.getUUID();
        d.setId(uuid);
        d.setFirstRelease(value(releaseDate));
        d.setLastRelease(value(datasetVersion.getLastReleasedAt()));
        d.setReleasedAt(value(releaseDateForSorting != null ? releaseDateForSorting.split("T")[0] : null));
        d.setReleasedDateForSorting(value(releaseDateForSorting));
        DatasetVersionV3.DatasetVersions dataset = datasetVersion.getDataset();
        List<Version> versions = dataset == null ? null : dataset.getVersions();
        boolean hasMultipleVersions = !CollectionUtils.isEmpty(versions) && versions.size() > 1;
        if (hasMultipleVersions) {
            d.setVersion(datasetVersion.getVersion());
            List<Version> sortedVersions = Helpers.sort(versions, translatorUtils.getErrors());
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            references.add(new TargetInternalReference(IdUtils.getUUID(dataset.getId()), "version overview"));
            d.setVersions(references);
            d.setAllVersionRef(new TargetInternalReference(IdUtils.getUUID(dataset.getId()), "version overview"));
            // if versions cannot be sorted (sortedVersions == versions) we flag it as searchable
            d.setSearchable(sortedVersions == versions || sortedVersions.get(0).getId().equals(datasetVersion.getId()));
        } else {
            d.setSearchable(true);
        }

        String title = getTitle(datasetVersion, dataset, hasMultipleVersions);
        if (title != null) {
            d.setTitle(value(title));
        }
        Accessibility accessibility = Accessibility.fromPayload(datasetVersion);
        String containerUrl = datasetVersion.getFileRepository() != null ? datasetVersion.getFileRepository().getIri() : null;
        if (accessibility != null) {
            switch (accessibility) {
                case CONTROLLED_ACCESS:
                    d.setEmbargo(value(DatasetVersion.createHDGMessage(uuid, containerUrl)));
                    break;
                case UNDER_EMBARGO:
                    Value<String> embargoMessage = value(Helpers.createEmbargoMessage("dataset", datasetVersion.getFileRepository(), uuid));
                    if (dataStage == DataStage.IN_PROGRESS && containerUrl != null) {
                        d.setEmbargoRestrictedAccess(embargoMessage);
                    } else {
                        d.setEmbargo(embargoMessage);
                    }
                    break;
                case RESTRICTED_ACCESS:
                    String restrictedAccessMessage = getRestrictedAccessMessage(title, uuid);
                    d.setEmbargo(value(restrictedAccessMessage));
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
                                d.setFileRepositoryId(IdUtils.getUUID(datasetVersion.getFileRepository().getId()));
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
                d.setExperimentalApproachForFilter(value(experimentalApproachesForFilter));
            }
        }
        d.setBehavioralProtocols(ref(datasetVersion.getBehavioralProtocol()));
        d.setPreparation(ref(datasetVersion.getPreparationDesign()));
        d.setTechnique(ref(datasetVersion.getTechnique()));
        if (!CollectionUtils.isEmpty(datasetVersion.getTechnique())) {
            final List<String> techniquesForFilter = datasetVersion.getTechnique().stream().map(FullNameRef::getFullName).filter(Objects::nonNull).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(techniquesForFilter)) {
                d.setTechniquesForFilter(value(techniquesForFilter));
            }
        }
        d.setAllIdentifiers(datasetVersion.getIdentifier());
        d.setIdentifier(IdUtils.getIdentifiersWithPrefix("Dataset", datasetVersion.getIdentifier()).stream().distinct().collect(Collectors.toList()));

        if (StringUtils.isNotBlank(datasetVersion.getDescription())) {
            d.setDescription(value(datasetVersion.getDescription()));
        } else if (dataset != null) {
            d.setDescription(value(dataset.getDescription()));
        }
        if (StringUtils.isNotBlank(datasetVersion.getVersionInnovation()) && !Constants.VERSION_INNOVATION_DEFAULTS.contains(StringUtils.trim(datasetVersion.getVersionInnovation()).toLowerCase())) {
            d.setNewInThisVersion(new Value<>(datasetVersion.getVersionInnovation()));
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
            d.setPublications(datasetVersion.getRelatedPublications().stream().map(p -> Helpers.getFormattedDigitalIdentifier(translatorUtils.getDoiCitationFormatter(), p.getIdentifier(), p.resolvedType())).filter(Objects::nonNull).map(Value::new).collect(Collectors.toList()));
        }

        if (!CollectionUtils.isEmpty(datasetVersion.getKeyword())) {
            Collections.sort(datasetVersion.getKeyword());
            d.setKeywords(value(datasetVersion.getKeyword()));
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
                String warning = String.format("The dataset version contains multiple data descriptors: %s - picking the first one", dataDescriptors.stream().map(File::getIri).collect(Collectors.joining(", ")));
                logger.warn(warning, uuid);
                translatorUtils.getErrors().add(warning);
                reference = new TargetExternalReference(dataDescriptors.get(0).getIri(), dataDescriptors.get(0).getName());
            } else {
                if (datasetVersion.getFullDocumentationFile() != null && !dataDescriptors.get(0).getIri().equals(datasetVersion.getFullDocumentationFile().getIri())) {
                    final String warning = String.format("The dataset has a file (%s) flagged with the role data descriptor and another one (%s) for the full documentation. Falling back to the full documentation file!", dataDescriptors.get(0).getIri(), datasetVersion.getFullDocumentationFile().getIri());
                    logger.warn(warning, uuid);
                    translatorUtils.getErrors().add(warning);
                    reference = new TargetExternalReference(datasetVersion.getFullDocumentationFile().getIri(), datasetVersion.getFullDocumentationFile().getName());
                } else if (datasetVersion.getFullDocumentationDOI() != null) {
                    final String warning = String.format("The dataset has a file (%s) flagged with the role data descriptor and a DOI (%s) for the full documentation. Falling back to the full documentation DOI!", dataDescriptors.get(0).getIri(), datasetVersion.getFullDocumentationDOI());
                    logger.warn(warning, uuid);
                    translatorUtils.getErrors().add(warning);
                    reference = new TargetExternalReference(datasetVersion.getFullDocumentationDOI(), datasetVersion.getFullDocumentationDOI());
                } else if (datasetVersion.getFullDocumentationUrl() != null) {
                    final String warning = String.format("The dataset has a file (%s) flagged with the role data descriptor and a URL (%s) for the full documentation. Falling back to the full documentation URL!", dataDescriptors.get(0).getIri(), datasetVersion.getFullDocumentationUrl());
                    logger.warn(warning, uuid);
                    translatorUtils.getErrors().add(warning);
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

        List<String> videoExtensions = List.of(".mp4");
        List<String> imageExtensions = Arrays.asList(".gif", ".jpg", ".jpeg", ".png");


        final List<File> previewFiles = specialFiles.stream().filter(s -> s.getRoles().contains(Constants.OPENMINDS_INSTANCES + "/fileUsageRole/preview") || s.getRoles().contains(Constants.OPENMINDS_INSTANCES + "/fileUsageRole/screenshot")).collect(Collectors.toList());
        final List<File> previewImages = previewFiles.stream().filter(f -> imageExtensions.stream().anyMatch(i -> f.getIri().toLowerCase().endsWith(i))).collect(Collectors.toList());
        final Map<String, File> previewImagesByFileNameWithoutExtension = previewImages.stream().collect(Collectors.toMap(this::stripFileExtension, v -> v));


        List<PreviewObject> previews = previewFiles.stream().filter(f -> videoExtensions.stream().anyMatch(e -> f.getIri().toLowerCase().endsWith(e))).map(f -> {
            PreviewObject o = new PreviewObject();
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
                        PreviewObject previewObject = new PreviewObject();
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
                String label = s.getLabel();
                if(StringUtils.isBlank(label)) {
                    translatorUtils.getErrors().add(String.format("Service link %s is missing the label!", s.getUrl()));
                    label = s.getUrl();
                }
                targetExternalReferences.add(new TargetExternalReference(s.getUrl(), label));
                targetExternalReferences.sort(Comparator.comparing(TargetExternalReference::getValue));
            });
            d.setViewData(viewData);
        }

        previews.addAll(previewImages.stream().map(i -> {
            PreviewObject o = new PreviewObject();
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
        d.setContentTypes(value(datasetVersion.getAllContentTypes()));

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
        final BasicHierarchyElement<DatasetVersion.DSVSpecimenOverview> specimenBySubject = new SpecimenV3Translator(datasetVersion.getId(), translatorUtils.getErrors(), getSpecimenLookupMapFromContext(translatorUtils)).translateToHierarchy(datasetVersion.getStudiedSpecimen());
        if (specimenBySubject != null) {
            d.setSpecimenIds(specimenBySubject.getData().getAllSpecimenIds());
            if (specimenBySubject.getData().getSpecies() != null) {
                d.setSpeciesFilter(specimenBySubject.getData().getSpecies().stream().map(TargetInternalReference::getValue).filter(Objects::nonNull).distinct().map(Value::new).collect(Collectors.toList()));
            }
            final Set<TargetInternalReference> anatomicalLocationsOfTissueSamples = specimenBySubject.getData().getAnatomicalLocationsOfTissueSamples();
            if (!CollectionUtils.isEmpty(anatomicalLocationsOfTissueSamples)) {
                d.setAnatomicalLocationOfTissueSamples(anatomicalLocationsOfTissueSamples.stream().sorted().collect(Collectors.toList()));
            }
            d.setSpecimenBySubject(specimenBySubject);
        }

        Map<String, FullNameRefForResearchProduct> inputResearchProducts = new HashMap<>();
        Helpers.addResearchProductsFromDOIs(inputResearchProducts, datasetVersion.getInputDOIs());
        Helpers.addResearchProducts(inputResearchProducts, datasetVersion.getInputResearchProductsFromInputFiles());
        Helpers.addResearchProducts(inputResearchProducts, datasetVersion.getInputResearchProductsFromInputFileBundles());
        Helpers.addResearchProducts(inputResearchProducts, datasetVersion.getInputResearchProductsFromReverseOutputDOIs());
        Helpers.addResearchProducts(inputResearchProducts, datasetVersion.getInputResearchProductsFromReverseOutputFiles());
        Helpers.addResearchProducts(inputResearchProducts, datasetVersion.getInputResearchProductsFromReverseOutputFileBundles());
        Helpers.addResearchProducts(inputResearchProducts, datasetVersion.getInputResearchProductsFromInputBrainAtlasVersions(), "Versions");
        d.setInputData(refVersion(new ArrayList<>(inputResearchProducts.values()), true));

        Set<TargetExternalReference> externalInputData = new HashSet<>();
        Set<String> externalDOIs = Helpers.getExternalDOIs(datasetVersion.getInputDOIs());
        if (!CollectionUtils.isEmpty(externalDOIs)) {
            Set<TargetExternalReference> externalDOIUrls = externalDOIs.stream().map(eid -> new TargetExternalReference(eid, eid)).collect(Collectors.toSet());
            externalInputData.addAll(externalDOIUrls);
        }
        if (!CollectionUtils.isEmpty(datasetVersion.getInputURLs())) {
            Set<TargetExternalReference> externalInputURLs = datasetVersion.getInputURLs().stream().map(eid -> new TargetExternalReference(eid, eid)).collect(Collectors.toSet());
            externalInputData.addAll(externalInputURLs);
        }
        if (!CollectionUtils.isEmpty(externalInputData)) {
            d.setExternalInputData(externalInputData.stream().sorted(Comparator.comparing(TargetExternalReference::getValue)).collect(Collectors.toList()));
        }

        Map<String, FullNameRefForResearchProduct> outputResearchProducts = new HashMap<>();
        Helpers.addResearchProducts(outputResearchProducts, datasetVersion.getOutputResearchProductsFromReverseInputDOIs());
        Helpers.addResearchProducts(outputResearchProducts, datasetVersion.getOutputResearchProductsFromReverseInputFiles());
        Helpers.addResearchProducts(outputResearchProducts, datasetVersion.getOutputResearchProductsFromReverseInputFileBundles());
        d.setOutputData(refVersion(new ArrayList<>(outputResearchProducts.values()), true));

        d.setMeta(SchemaOrgConverter.translateDatasetVersion(datasetVersion));
        translatorUtils.defineBadgesAndTrendingState(d, datasetVersion.getIssueDate(), releaseDate, datasetVersion.getLast30DaysViews(), MetaBadgeUtils.evaluateMetaBadgeUtils(datasetVersion, !CollectionUtils.isEmpty(d.getOutputData()), !CollectionUtils.isEmpty(d.getInputData())));
        if(!CollectionUtils.isEmpty(datasetVersion.getLearningResource())) {
            d.setLearningResources(datasetVersion.getLearningResource().stream().map(LearningResource::toReference).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        d.setLivePapers(link(datasetVersion.getLivePapers()));

        d.setQueryBuilderText(value(TranslatorUtils.createQueryBuilderText(datasetVersion.getPrimaryType(), d.getId())));
        return d;
    }

    private String stripFileExtension(File file) {
        return stripFileExtension(file.getIri());
    }

    private String stripFileExtension(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }


}
