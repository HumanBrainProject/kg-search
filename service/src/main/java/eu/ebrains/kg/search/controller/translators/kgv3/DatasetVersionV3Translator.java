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
import eu.ebrains.kg.search.model.DataStage;
import eu.ebrains.kg.search.model.source.ResultsOfKGv3;
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetVersionV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.FullNameRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.PersonOrOrganizationRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.DatasetVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.AmbiguousDataException;
import eu.ebrains.kg.search.utils.IdUtils;
import eu.ebrains.kg.search.utils.TranslationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

//Test ids
// 4ac9f0bc-560d-47e0-8916-7b24da9bb0ce (multiple versions)
// 4840dd00-058b-437c-9d0f-091b482d51b8 (experimental approaches and techniques)


public class DatasetVersionV3Translator extends TranslatorV3<DatasetVersionV3, DatasetVersion, DatasetVersionV3Translator.Result> {

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
        return Collections.singletonList("https://openminds.ebrains.eu/core/DatasetVersion");
    }


    private enum Accessibility {
        FREE_ACCESS("https://openminds.ebrains.eu/instances/productAccessibility/freeAccess"),
        CONTROLLED_ACCESS("https://openminds.ebrains.eu/instances/productAccessibility/controlledAccess"),
        RESTRICTED_ACCESS("https://openminds.ebrains.eu/instances/productAccessibility/restrictedAccess"),
        UNDER_EMBARGO("https://openminds.ebrains.eu/instances/productAccessibility/underEmbargo");

        private final String identifier;

        Accessibility(String identifier) {
            this.identifier = identifier;
        }

        public static Accessibility fromPayload(DatasetVersionV3 datasetVersion) {
            if (datasetVersion != null && datasetVersion.getAccessibility() != null && datasetVersion.getAccessibility().getIdentifier() != null) {
                return Arrays.stream(Accessibility.values()).filter(a -> datasetVersion.getAccessibility().getIdentifier().contains(a.identifier)).findFirst().orElse(null);
            }
            return null;
        }

    }

    public DatasetVersion translate(DatasetVersionV3 datasetVersion, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        DatasetVersion d = new DatasetVersion();
        DatasetVersionV3.DatasetVersions dataset = datasetVersion.getDataset();
        Accessibility accessibility = Accessibility.fromPayload(datasetVersion);
        String containerUrl = datasetVersion.getFileRepository() != null ? datasetVersion.getFileRepository().getIri() : null;
        if (accessibility != null) {
            switch (accessibility) {
                case CONTROLLED_ACCESS:
                    d.setEmbargo(value(DatasetVersion.createHDGMessage(datasetVersion.getUUID(), true)));
                    break;
                case UNDER_EMBARGO:
                    d.setEmbargo(value(DatasetVersion.EMBARGO_MESSAGE));
                    if (dataStage == DataStage.IN_PROGRESS && containerUrl != null) {
                        d.setEmbargoRestrictedAccess(value(DatasetVersion.createEmbargoInProgressMessage(containerUrl)));
                    }
                    break;
                case RESTRICTED_ACCESS:
                    d.setEmbargo(value(DatasetVersion.RESTRICTED_ACCESS_MESSAGE));
                    break;
                default:
                    if (datasetVersion.getFileRepository() != null) {
                        //TODO handle indexed mode
                        if (liveMode) {
                            d.setFilesAsyncUrl(String.format("/api/repositories/%s/files/live", IdUtils.getUUID(datasetVersion.getFileRepository().getId())));
                        }
                    }
            }
            d.setDataAccessibility(value(datasetVersion.getAccessibility().getName()));
        }

        d.setId(datasetVersion.getUUID());
        //TODO d.setExternalDatalink
        //d.setFiles -> replaced with d.setFileRepository






        if (datasetVersion.getExperimentalApproach() != null) {
            final List<TargetInternalReference> experimentalApproach = datasetVersion.getExperimentalApproach().stream().filter(Objects::nonNull).filter(e -> StringUtils.isNotBlank(e.getFullName()))
                    .map(e -> new TargetInternalReference(IdUtils.getUUID(e.getId()), e.getFullName())).collect(Collectors.toList());
            if (!experimentalApproach.isEmpty()) {
                d.setExperimentalApproach(experimentalApproach);
            }
        }

        if (datasetVersion.getTechnique() != null) {
            final List<TargetInternalReference> technique = datasetVersion.getTechnique().stream().filter(Objects::nonNull).filter(e -> StringUtils.isNotBlank(e.getFullName()))
                    .map(e -> new TargetInternalReference(IdUtils.getUUID(e.getId()), e.getFullName())).collect(Collectors.toList());
            if (!technique.isEmpty()) {
                d.setTechnique(technique);
            }
        }
        d.setIdentifier(datasetVersion.getSimpleIdentifiers());
        List<Version> versions = dataset == null ? null : dataset.getVersions();
        if (!CollectionUtils.isEmpty(versions) && versions.size() > 1) {
            d.setVersion(datasetVersion.getVersion());
            List<Version> sortedVersions = Helpers.sort(versions);
            d.setVersions(sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList()));
            d.setAllVersionRef(new TargetInternalReference(IdUtils.getUUID(dataset.getId()), "All versions"));
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
        if (StringUtils.isNotBlank(datasetVersion.getVersionInnovation())) {
            d.setNewInThisVersion(new Value<>(datasetVersion.getVersionInnovation()));
        }

        if (StringUtils.isNotBlank(datasetVersion.getFullName())) {
            d.setTitle(value(datasetVersion.getFullName()));
        } else if (dataset != null && StringUtils.isNotBlank(dataset.getFullName())) {
            d.setTitle(value(dataset.getFullName()));
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
        String citation = datasetVersion.getHowToCite();
        String doi = datasetVersion.getDoi();
        if (StringUtils.isNotBlank(doi)) {
            final String doiWithoutPrefix = Helpers.stripDOIPrefix(doi);
            //TODO do we want to keep this one? It's actually redundant with what we have in "cite dataset"
            d.setDoi(value(doiWithoutPrefix));
            if (StringUtils.isNotBlank(citation)) {
                d.setCitation(value(String.format("%s [DOI: %s](%s)", citation, doiWithoutPrefix, doi)));
            } else {
                //TODO resolve by service
                d.setCitation(value(String.format("[DOI: %s](%s)", doiWithoutPrefix, doi)));
            }
        } else if (StringUtils.isNotBlank(citation)) {
            d.setCitation(value(citation));
        }

        d.setLicenseInfo(link(datasetVersion.getLicense()));
        d.setProjects(ref(datasetVersion.getProjects()));


        List<PersonOrOrganizationRef> custodians = datasetVersion.getCustodians();
        if (CollectionUtils.isEmpty(custodians) && datasetVersion.getDataset() != null) {
            custodians = datasetVersion.getDataset().getCustodians();
        }

        if (!CollectionUtils.isEmpty(custodians)) {
            d.setCustodians(custodians.stream().map(c -> new TargetInternalReference(IdUtils.getUUID(c.getId()), Helpers.getFullName(c.getFullName(), c.getFamilyName(), c.getGivenName()))).collect(Collectors.toList()));
        }

        if (!CollectionUtils.isEmpty(datasetVersion.getKeyword())) {
            Collections.sort(datasetVersion.getKeyword());
            d.setKeywords(value(datasetVersion.getKeyword()));
        }
        if (!CollectionUtils.isEmpty(datasetVersion.getSubjects())) {
            d.setSpeciesFilter(value(datasetVersion.getSubjects().stream().map(DatasetVersionV3.SubjectOrSubjectGroup::getSpecies).flatMap(Collection::stream).map(FullNameRef::getFullName).filter(Objects::nonNull).distinct().sorted().collect(Collectors.toList())));
            final Set<String> groupedSubjects = datasetVersion.getSubjects().stream().map(DatasetVersionV3.SubjectOrSubjectGroup::getChildren).filter(children -> !CollectionUtils.isEmpty(children)).flatMap(Collection::stream).map(DatasetVersionV3.SubjectOrSubjectGroup::getId).collect(Collectors.toSet());
            final List<DatasetVersion.SubjectGroupOrSingleSubject> subjects = datasetVersion.getSubjects().stream()
                    //We don't want individual subjects to appear on the root hierarchy level if they also have a representation inside the groups...
                    .filter(s -> !groupedSubjects.contains(s.getId()))
                    .map(s ->
                    {
                        //Some subject groups contain individual subject information. Let's populate the values before we start to translate
                        s.calculateSubjectGroupInformationFromChildren();

                        DatasetVersion.SubjectGroupOrSingleSubject subj = new DatasetVersion.SubjectGroupOrSingleSubject();

                        if (!CollectionUtils.isEmpty(s.getChildren())) {
                            //This is a subject group with individual information.
                            subj.setChildren(s.getChildren().stream().map(child -> fillIndividualSubjectInformation(new DatasetVersion.SingleSubject(), child)).collect(Collectors.toList()));
                        }
                        fillIndividualSubjectInformation(subj, s);
                        return subj;
                    }
            ).collect(Collectors.toList());
            if (!subjects.isEmpty()) {
                d.setSubjectGroupOrSingleSubject(children(subjects));
            }
        }


        final List<DatasetVersionV3.File> specialFiles = datasetVersion.getSpecialFiles();
        final List<DatasetVersionV3.File> dataDescriptors = specialFiles.stream().filter(s -> s.getRoles().contains("https://openminds.ebrains.eu/instances/fileUsageRole/dataDescriptor")).collect(Collectors.toList());
        if(!dataDescriptors.isEmpty()) {
            if (dataDescriptors.size() > 1) {
                throw new AmbiguousDataException(String.format("The dataset version contains multiple data descriptors: %s", dataDescriptors.stream().map(DatasetVersionV3.File::getIri).collect(Collectors.joining(", "))), datasetVersion.getUUID());
            } else {
                d.setDataDescriptor(new TargetExternalReference(dataDescriptors.get(0).getIri(), dataDescriptors.get(0).getName()));
                if(datasetVersion.getFullDocumentationFile() != null && !dataDescriptors.get(0).getIri().equals(datasetVersion.getFullDocumentationFile().getIri())){
                    throw new AmbiguousDataException(String.format("The dataset has a file (%s) flagged with the role data descriptor and another one (%s) for the full documentation. This is invalid!", dataDescriptors.get(0).getIri(), datasetVersion.getFullDocumentationFile().getIri()), datasetVersion.getUUID());
                }
                else if(datasetVersion.getFullDocumentationDOI()!=null){
                    throw new AmbiguousDataException(String.format("The dataset has a file (%s) flagged with the role data descriptor and a DOI (%s) for the full documentation. This is invalid!", dataDescriptors.get(0).getIri(), datasetVersion.getFullDocumentationDOI()), datasetVersion.getUUID());
                }
                else if(datasetVersion.getFullDocumentationUrl()!=null){
                    throw new AmbiguousDataException(String.format("The dataset has a file (%s) flagged with the role data descriptor and a DOI (%s) for the full documentation. This is invalid!", dataDescriptors.get(0).getIri(), datasetVersion.getFullDocumentationUrl()), datasetVersion.getUUID());
                }
            }
        }
        else if(datasetVersion.getFullDocumentationFile()!=null){
            d.setDataDescriptor(new TargetExternalReference(datasetVersion.getFullDocumentationFile().getIri(), datasetVersion.getFullDocumentationFile().getName()));
        }
        else if(datasetVersion.getFullDocumentationUrl()!=null){
            d.setDataDescriptor(new TargetExternalReference(datasetVersion.getFullDocumentationUrl(), datasetVersion.getFullDocumentationUrl()));
        }
        else if(datasetVersion.getFullDocumentationDOI()!=null){
            //TODO resolve the DOI
            d.setDataDescriptor(new TargetExternalReference(datasetVersion.getFullDocumentationDOI(), datasetVersion.getFullDocumentationDOI()));
        }

        final List<DatasetVersion.PreviewObject> previewObjects = specialFiles.stream().filter(s -> s.getRoles().contains("https://openminds.ebrains.eu/instances/fileUsageRole/preview") || s.getRoles().contains("https://openminds.ebrains.eu/instances/fileUsageRole/screenshot")).map(f -> {
            DatasetVersion.PreviewObject o  = new DatasetVersion.PreviewObject();
            o.setUrl(value(f.getIri()));
            o.setValue(o.getValue());
            o.setPreviewUrl(value(f.getIri()));
            o.setThumbnailUrl(value(f.getIri()));
            //TODO make this more reliable
            o.setIsAnimated(value(f.getIri().endsWith(".mp4")));
            return o;
        }).collect(Collectors.toList());
        if(!previewObjects.isEmpty()){
            d.setPreviewObjects(previewObjects);
        }

        d.setStudiedBrainRegion(refVersion(datasetVersion.getParcellationEntityFromStudyTarget()));

        return d;
    }


    private <T extends DatasetVersion.SingleSubject> T fillIndividualSubjectInformation(T subj, DatasetVersionV3.SubjectOrSubjectGroup s) {
        subj.setSubjectName(new TargetInternalReference(IdUtils.getUUID(s.getId()), s.getInternalIdentifier()));
        subj.setNumberOfSubjects(value(s.getQuantity()!=null ? String.valueOf(s.getQuantity()) : null));
        subj.setSpecies(ref(s.getSpecies()));
        subj.setSex(ref(s.getBiologicalSex()));
        List<DatasetVersionV3.SpecimenOrSpecimenGroupState> states = s.getStates();
        //TODO sort states
        //To represent states, we're going to provide a list for the state specific entries in a single "subject row".
        List<String> ages = new ArrayList<>();
        List<List<TargetInternalReference>> ageCategories = new ArrayList<>();
        List<String> weights = new ArrayList<>();
        for (DatasetVersionV3.SpecimenOrSpecimenGroupState state : states) {
            ages.add(state.getAge()!=null ? state.getAge().displayString() : "-");
            //TODO how can we enforce this inner list to be comma separated instead?
            ageCategories.add(state.getAgeCategory()!=null ? ref(state.getAgeCategory()) : null);
            weights.add(state.getWeight()!=null ? state.getWeight().displayString() : "-");
        }
        subj.setAge(value(ages));
        subj.setAgeCategory(ageCategories);
        subj.setWeight(value(weights));
        return subj;
    }


}
