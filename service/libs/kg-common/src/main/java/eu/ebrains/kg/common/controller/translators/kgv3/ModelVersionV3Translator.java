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
import eu.ebrains.kg.common.model.DataStage;
import eu.ebrains.kg.common.model.source.ResultsOfKGv3;
import eu.ebrains.kg.common.model.source.openMINDSv3.ModelVersionV3;
import eu.ebrains.kg.common.model.source.openMINDSv3.commons.*;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.ModelVersion;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.utils.IdUtils;
import eu.ebrains.kg.common.utils.MetaBadgeUtils;
import eu.ebrains.kg.common.utils.TranslationException;
import eu.ebrains.kg.common.utils.TranslatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModelVersionV3Translator extends TranslatorV3<ModelVersionV3, ModelVersion, ModelVersionV3Translator.Result> {

    public static class Result extends ResultsOfKGv3<ModelVersionV3> {
    }

    @Override
    public Class<ModelVersionV3> getSourceType() {
        return ModelVersionV3.class;
    }

    @Override
    public Class<ModelVersion> getTargetType() {
        return ModelVersion.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("87858583-1462-4952-9e71-90b159a7a1ed");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/ModelVersion");
    }


    public ModelVersion translate(ModelVersionV3 modelVersion, DataStage dataStage, boolean liveMode, TranslatorUtils translatorUtils) throws TranslationException {
        ModelVersion m = new ModelVersion();

        m.setCategory(new Value<>("Model"));
        m.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));

        ModelVersionV3.ModelVersions model = modelVersion.getModel();
        Accessibility accessibility = Accessibility.fromPayload(modelVersion);
        m.setId(IdUtils.getUUID(modelVersion.getId()));
        final Date releaseDate = modelVersion.getReleaseDate() != null && modelVersion.getReleaseDate().before(new Date()) ? modelVersion.getReleaseDate() : modelVersion.getFirstReleasedAt();
        final String releaseDateForSorting = translatorUtils.getReleasedDateForSorting(modelVersion.getIssueDate(), releaseDate);
        m.setFirstRelease(value(releaseDate));
        m.setLastRelease(value(modelVersion.getLastReleasedAt()));
        m.setReleasedAt(value(modelVersion.getIssueDate()));
        m.setReleasedDateForSorting(value(releaseDateForSorting));
        m.setAllIdentifiers(modelVersion.getIdentifier());
        m.setIdentifier(IdUtils.getIdentifiersWithPrefix("Model", modelVersion.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        List<Version> versions = model == null ? null : model.getVersions();
        boolean hasMultipleVersions = !CollectionUtils.isEmpty(versions) && versions.size() > 1;
        if (hasMultipleVersions) {
            m.setVersion(modelVersion.getVersion());
            List<Version> sortedVersions = Helpers.sort(versions, translatorUtils.getErrors());
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            references.add(new TargetInternalReference(IdUtils.getUUID(model.getId()), "version overview"));
            m.setVersions(references);
            m.setAllVersionRef(new TargetInternalReference(IdUtils.getUUID(model.getId()), "version overview"));
            // if versions cannot be sorted (sortedVersions == versions) we flag it as searchable
            m.setSearchable(sortedVersions == versions || sortedVersions.get(0).getId().equals(modelVersion.getId()));
        } else {
            m.setSearchable(true);
        }

        if (StringUtils.isNotBlank(modelVersion.getDescription())) {
            m.setDescription(value(modelVersion.getDescription()));
        } else if (model != null) {
            m.setDescription(value(model.getDescription()));
        }

        if (StringUtils.isNotBlank(modelVersion.getVersionInnovation()) && !Constants.VERSION_INNOVATION_DEFAULTS.contains(StringUtils.trim(modelVersion.getVersionInnovation()).toLowerCase())) {
            m.setNewInThisVersion(new Value<>(modelVersion.getVersionInnovation()));
        }
        m.setHomepage(link(modelVersion.getHomepage()));

        if (StringUtils.isNotBlank(modelVersion.getFullName())) {
            if (hasMultipleVersions || StringUtils.isBlank(modelVersion.getVersion())) {
                m.setTitle(value(modelVersion.getFullName()));
            } else {
                m.setTitle(value(String.format("%s (%s)", modelVersion.getFullName(), modelVersion.getVersion())));
            }
        } else if (model != null && StringUtils.isNotBlank(model.getFullName())) {
            if (hasMultipleVersions || StringUtils.isBlank(modelVersion.getVersion())) {
                m.setTitle(value(model.getFullName()));
            } else {
                m.setTitle(value(String.format("%s (%s)", model.getFullName(), modelVersion.getVersion())));
            }
        }

        if (!CollectionUtils.isEmpty(modelVersion.getDeveloper())) {
            m.setContributors(modelVersion.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        } else if (model != null && !CollectionUtils.isEmpty(model.getDeveloper())) {
            m.setContributors(model.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }

        if (accessibility == Accessibility.UNDER_EMBARGO) {
            m.setEmbargo(value(Helpers.createEmbargoMessage("model", modelVersion.getFileRepository(), dataStage)));
        } else {
            if (modelVersion.getFileRepository() != null && modelVersion.getFileRepository().getIri() != null) {
                final String iri = modelVersion.getFileRepository().getIri();
                final boolean allowEmbedding = Constants.DOMAINS_ALLOWING_EMBEDDING.stream().anyMatch(iri::startsWith);
                if (allowEmbedding) {
                    m.setEmbeddedModelSource(new TargetExternalReference(iri, modelVersion.getFileRepository().getFullName()));
                } else {
                    if (modelVersion.getFileRepository() != null) {
                        if (Helpers.isCscsContainer(modelVersion.getFileRepository()) || Helpers.isDataProxyBucket(modelVersion.getFileRepository())) {
                            m.setFileRepositoryId(IdUtils.getUUID(modelVersion.getFileRepository().getId()));
                        } else {
                            m.setExternalDownload(link(modelVersion.getFileRepository()));
                        }
                    }
                }
            }
        }

        handleCitation(modelVersion, m);

        m.setLicenseInfo(link(modelVersion.getLicense()));
        List<FullNameRef> projects = null;
        if (modelVersion.getProjects() != null && model != null && model.getProjects() != null) {
            projects = Stream.concat(modelVersion.getProjects().stream(), model.getProjects().stream()).distinct().collect(Collectors.toList());
        } else if (modelVersion.getProjects() != null) {
            projects = modelVersion.getProjects();
        } else if (model != null && model.getProjects() != null) {
            projects = model.getProjects();
        }
        m.setProjects(ref(projects));


        List<PersonOrOrganizationRef> custodians = modelVersion.getCustodian();
        if (CollectionUtils.isEmpty(custodians) && modelVersion.getModel() != null) {
            custodians = modelVersion.getModel().getCustodian();
        }

        if (!CollectionUtils.isEmpty(custodians)) {
            m.setCustodians(custodians.stream().map(c -> new TargetInternalReference(IdUtils.getUUID(c.getId()), Helpers.getFullName(c.getFullName(), c.getFamilyName(), c.getGivenName()))).collect(Collectors.toList()));
        }

        if (!CollectionUtils.isEmpty(modelVersion.getRelatedPublications())) {
            m.setPublications(modelVersion.getRelatedPublications().stream().map(p -> Helpers.getFormattedDigitalIdentifier(translatorUtils.getDoiCitationFormatter(), p.getIdentifier(), p.resolvedType())).filter(Objects::nonNull).map(Value::new).collect(Collectors.toList()));
        }

        if (!CollectionUtils.isEmpty(modelVersion.getKeyword())) {
            Collections.sort(modelVersion.getKeyword());
            m.setKeywords(value(modelVersion.getKeyword()));
        }
        m.setModelFormat(ref(createList(modelVersion.getModelFormat())));
        if (modelVersion.getModel() != null) {
            m.setAbstractionLevel(ref(createList(modelVersion.getModel().getAbstractionLevel())));
            List<String> brainStructureStudyTargets = List.of(Constants.OPENMINDS_ROOT + "controlledTerms/UBERONParcellation");
            final Map<Boolean, List<StudyTarget>> brainStructureOrNot = modelVersion.getModel().getStudyTarget().stream().collect(Collectors.groupingBy(s -> s.getStudyTargetType() != null && s.getStudyTargetType().stream().anyMatch(brainStructureStudyTargets::contains)));
            m.setStudyTargets(refVersion(brainStructureOrNot.get(Boolean.FALSE), false));
            if(!CollectionUtils.isEmpty(brainStructureOrNot.get(Boolean.TRUE))){
                m.setBrainStructures(brainStructureOrNot.get(Boolean.TRUE).stream().map(this::refAnatomical).collect(Collectors.toList()));
            }
            m.setModelScope(ref(createList(modelVersion.getModel().getScope())));
        }

        Map<String, FullNameRefForResearchProduct> inputResearchProducts = new HashMap<>();
        Helpers.addResearchProductsFromDOIs(inputResearchProducts, modelVersion.getInputDOIs());
        Helpers.addResearchProducts(inputResearchProducts, modelVersion.getInputResearchProductsFromInputFiles());
        Helpers.addResearchProducts(inputResearchProducts, modelVersion.getInputResearchProductsFromInputFileBundles());
        Helpers.addResearchProducts(inputResearchProducts, modelVersion.getInputResearchProductsFromReverseOutputDOIs());
        Helpers.addResearchProducts(inputResearchProducts, modelVersion.getInputResearchProductsFromReverseOutputFiles());
        Helpers.addResearchProducts(inputResearchProducts, modelVersion.getInputResearchProductsFromReverseOutputFileBundles());
        m.setInputData(refVersion(new ArrayList<>(inputResearchProducts.values()), true));

        Set<TargetExternalReference> externalInputData = new HashSet<>();
        Set<String> externalDOIs = Helpers.getExternalDOIs(modelVersion.getInputDOIs());
        if (!CollectionUtils.isEmpty(externalDOIs)) {
            List<TargetExternalReference> externalDOIUrls = externalDOIs.stream().map(eid -> new TargetExternalReference(eid, eid)).collect(Collectors.toList());
            externalInputData.addAll(externalDOIUrls);
        }
        if (!CollectionUtils.isEmpty(modelVersion.getInputURLs())) {
            Set<TargetExternalReference> externalInputURLs = modelVersion.getInputURLs().stream().map(eid -> new TargetExternalReference(eid, eid)).collect(Collectors.toSet());
            externalInputData.addAll(externalInputURLs);
        }
        if(!CollectionUtils.isEmpty(externalInputData)) {
            m.setExternalInputData(externalInputData.stream().sorted(Comparator.comparing(TargetExternalReference::getValue)).collect(Collectors.toList()));
        }

        Map<String, FullNameRefForResearchProduct> outputResearchProducts = new HashMap<>();
        Helpers.addResearchProducts(outputResearchProducts, modelVersion.getOutputResearchProductsFromReverseInputDOIs());
        Helpers.addResearchProducts(outputResearchProducts, modelVersion.getOutputResearchProductsFromReverseInputFiles());
        Helpers.addResearchProducts(outputResearchProducts, modelVersion.getOutputResearchProductsFromReverseInputFileBundles());
        Helpers.addResearchProductsFromDOIs(outputResearchProducts, modelVersion.getOutputDOIs());
        Helpers.addResearchProducts(outputResearchProducts, modelVersion.getOutputResearchProductsFromOutputFiles());
        Helpers.addResearchProducts(outputResearchProducts, modelVersion.getOutputResearchProductsFromOutputFileBundles());
        m.setOutputData(refVersion(new ArrayList<>(outputResearchProducts.values()), true));

        Set<TargetExternalReference> externalOutputData = new HashSet<>();
        Set<String> externalOutputDOIs = Helpers.getExternalDOIs(modelVersion.getOutputDOIs());
        if (!CollectionUtils.isEmpty(externalOutputDOIs)) {
            List<TargetExternalReference> externalOutputDOIUrls = externalOutputDOIs.stream().map(eid -> new TargetExternalReference(eid, eid)).collect(Collectors.toList());
            externalOutputData.addAll(externalOutputDOIUrls);
        }
        if (!CollectionUtils.isEmpty(modelVersion.getOutputURLs())) {
            Set<TargetExternalReference> externalUrlsOutput = modelVersion.getOutputURLs().stream().map(eid -> new TargetExternalReference(eid, eid)).collect(Collectors.toSet());
            externalOutputData.addAll(externalUrlsOutput);
        }
        if(!CollectionUtils.isEmpty(externalOutputData)) {
            m.setExternalOutputData(externalOutputData.stream().sorted(Comparator.comparing(TargetExternalReference::getValue)).collect(Collectors.toList()));
        }
        translatorUtils.defineBadgesAndTrendingState(m, modelVersion.getIssueDate(), releaseDate, modelVersion.getLast30DaysViews(), MetaBadgeUtils.evaluateMetaBadgeUtils(modelVersion, !CollectionUtils.isEmpty(m.getOutputData()), !CollectionUtils.isEmpty(m.getInputData())));
        if(!CollectionUtils.isEmpty(modelVersion.getLearningResource())) {
            m.setLearningResources(modelVersion.getLearningResource().stream().map(LearningResource::toReference).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        m.setLivePapers(link(modelVersion.getLivePapers()));
        m.setQueryBuilderText(value(TranslatorUtils.createQueryBuilderText(modelVersion.getPrimaryType(), m.getId())));
        return m;
    }
}
