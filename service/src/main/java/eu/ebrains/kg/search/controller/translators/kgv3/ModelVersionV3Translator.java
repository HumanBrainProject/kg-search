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
import eu.ebrains.kg.search.model.source.openMINDSv3.ModelVersionV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.FullNameRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.PersonOrOrganizationRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.StudyTarget;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.ModelVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.search.services.DOICitationFormatter;
import eu.ebrains.kg.search.utils.IdUtils;
import eu.ebrains.kg.search.utils.TranslationException;
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


    public ModelVersion translate(ModelVersionV3 modelVersion, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        ModelVersion m = new ModelVersion();
        ModelVersionV3.ModelVersions model = modelVersion.getModel();
        Accessibility accessibility = Accessibility.fromPayload(modelVersion);
        m.setId(IdUtils.getUUID(modelVersion.getId()));
        m.setAllIdentifiers(modelVersion.getIdentifier());
        m.setIdentifier(IdUtils.getIdentifiersWithPrefix("Model", modelVersion.getIdentifier()));
        List<Version> versions = model == null ? null : model.getVersions();
        boolean hasMultipleVersions = !CollectionUtils.isEmpty(versions) && versions.size() > 1;
        if (hasMultipleVersions) {
            m.setVersion(modelVersion.getVersion());
            List<Version> sortedVersions = Helpers.sort(versions);
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            references.add(new TargetInternalReference(IdUtils.getUUID(model.getId()), "All versions"));
            m.setVersions(references);
            m.setAllVersionRef(new TargetInternalReference(IdUtils.getUUID(model.getId()), "All versions"));
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
                            String endpoint;
                            if (liveMode) {
                                endpoint = String.format("/api/repositories/%s/files/live", IdUtils.getUUID(modelVersion.getFileRepository().getId()));
                            } else {
                                endpoint = String.format("/api/groups/%s/repositories/%s/files", dataStage == DataStage.IN_PROGRESS ? "curated" : "public", IdUtils.getUUID(modelVersion.getFileRepository().getId()));
                            }
                            m.setFilesAsyncUrl(endpoint);
                        } else {
                            m.setExternalDownload(link(modelVersion.getFileRepository()));
                        }
                    }
                }
            }
        }

        String citation = modelVersion.getHowToCite();
        String doi = modelVersion.getDoi();
        if (StringUtils.isNotBlank(doi)) {
            final String doiWithoutPrefix = Helpers.stripDOIPrefix(doi);
            //TODO do we want to keep this one? It's actually redundant with what we have in "cite dataset"
            m.setDoi(value(doiWithoutPrefix));
            if (StringUtils.isNotBlank(citation)) {
                m.setCitation(value(citation));
            } else {
                m.setCitation(value(Helpers.getFormattedDOI(doiCitationFormatter, doi)));
            }
        } else if (StringUtils.isNotBlank(citation)) {
            m.setCitation(value(citation));
        }
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
            m.setPublications(modelVersion.getRelatedPublications().stream().map(p -> Helpers.getFormattedDOI(doiCitationFormatter, p)).filter(Objects::nonNull).map(Value::new).collect(Collectors.toList()));
        }

        if (!CollectionUtils.isEmpty(modelVersion.getKeyword())) {
            Collections.sort(modelVersion.getKeyword());
            m.setKeywords(value(modelVersion.getKeyword()));
        }
        m.setUsedDataset(refVersion(modelVersion.getUsedDatasets()));
        m.setProducedDataset(refVersion(modelVersion.getProducedDatasets()));
        m.setModelFormat(ref(createList(modelVersion.getModelFormat())));
        if (modelVersion.getModel() != null) {
            m.setAbstractionLevel(ref(createList(modelVersion.getModel().getAbstractionLevel())));
            List<String> brainStructureStudyTargets = Arrays.asList(Constants.OPENMINDS_ROOT+"controlledTerms/UBERONParcellation");
            final Map<Boolean, List<StudyTarget>> brainStructureOrNot = modelVersion.getModel().getStudyTarget().stream().collect(Collectors.groupingBy(s -> s.getStudyTargetType() != null && s.getStudyTargetType().stream().anyMatch(brainStructureStudyTargets::contains)));
            m.setStudyTargets(refVersion(brainStructureOrNot.get(Boolean.FALSE)));
            if(!CollectionUtils.isEmpty(brainStructureOrNot.get(Boolean.TRUE))){
                m.setStudiedBrainRegion(brainStructureOrNot.get(Boolean.TRUE).stream().map(this::refAnatomical).collect(Collectors.toList()));
            }
            m.setModelScope(ref(createList(modelVersion.getModel().getScope())));
        }
        return m;
    }
}
