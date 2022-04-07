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
import eu.ebrains.kg.search.model.source.openMINDSv3.MetadataModelVersionV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.FullNameRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.PersonOrOrganizationRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.RelatedPublication;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.MetaDataModelVersion;
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

public class MetaDataModelVersionV3Translator extends TranslatorV3<MetadataModelVersionV3, MetaDataModelVersion, MetaDataModelVersionV3Translator.Result> {

    public static class Result extends ResultsOfKGv3<MetadataModelVersionV3> {
    }

    @Override
    public Class<MetadataModelVersionV3> getSourceType() {
        return MetadataModelVersionV3.class;
    }

    @Override
    public Class<MetaDataModelVersion> getTargetType() {
        return MetaDataModelVersion.class;
    }

    @Override
    public Class<Result> getResultType() {
        return Result.class;
    }

    @Override
    public List<String> getQueryIds() {
        return Collections.singletonList("f58f0ea3-72f2-4da5-aeb6-ce7339595037");
    }

    @Override
    public List<String> semanticTypes() {
        return Collections.singletonList("https://openminds.ebrains.eu/core/MetaDataModelVersion");
    }


    public MetaDataModelVersion translate(MetadataModelVersionV3 metadataModelVersionV3, DataStage dataStage, boolean liveMode, DOICitationFormatter doiCitationFormatter) throws TranslationException {
        MetaDataModelVersion m = new MetaDataModelVersion();

        m.setCategory(new Value<>("Meta Data Model"));
        m.setDisclaimer(new Value<>("Please alert us at [curation-support@ebrains.eu](mailto:curation-support@ebrains.eu) for errors or quality concerns regarding the dataset, so we can forward this information to the Data Custodian responsible."));

        MetadataModelVersionV3.MetaDataModelVersions metaDataModel = metadataModelVersionV3.getMetaDataModel();
        Accessibility accessibility = Accessibility.fromPayload(metadataModelVersionV3);
        m.setId(IdUtils.getUUID(metadataModelVersionV3.getId()));
        m.setAllIdentifiers(metadataModelVersionV3.getIdentifier());
        m.setIdentifier(IdUtils.getUUID(metadataModelVersionV3.getIdentifier()).stream().distinct().collect(Collectors.toList()));
        List<Version> versions = metaDataModel == null ? null : metaDataModel.getVersions();
        boolean hasMultipleVersions = !CollectionUtils.isEmpty(versions) && versions.size() > 1;
        if (hasMultipleVersions) {
            m.setVersion(metadataModelVersionV3.getVersionIdentifier());
            List<Version> sortedVersions = Helpers.sort(versions);
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            references.add(new TargetInternalReference(IdUtils.getUUID(metaDataModel.getId()), "version overview"));
            m.setVersions(references);
            m.setAllVersionRef(new TargetInternalReference(IdUtils.getUUID(metaDataModel.getId()), "version overview"));
            // if versions cannot be sorted (sortedVersions == versions) we flag it as searchable
            m.setSearchable(sortedVersions == versions || sortedVersions.get(0).getId().equals(metadataModelVersionV3.getId()));
        } else {
            m.setSearchable(true);
        }

        if (StringUtils.isNotBlank(metadataModelVersionV3.getDescription())) {
            m.setDescription(value(metadataModelVersionV3.getDescription()));
        } else if (metaDataModel != null) {
            m.setDescription(value(metaDataModel.getDescription()));
        }

        if (StringUtils.isNotBlank(metadataModelVersionV3.getVersionInnovation()) && !Constants.VERSION_INNOVATION_DEFAULTS.contains(StringUtils.trim(metadataModelVersionV3.getVersionInnovation()).toLowerCase())) {
            m.setNewInThisVersion(new Value<>(metadataModelVersionV3.getVersionInnovation()));
        }
        m.setHomepage(link(metadataModelVersionV3.getHomepage()));

        if (StringUtils.isNotBlank(metadataModelVersionV3.getFullName())) {
            if (hasMultipleVersions || StringUtils.isBlank(metadataModelVersionV3.getVersionIdentifier())) {
                m.setTitle(value(metadataModelVersionV3.getFullName()));
            } else {
                m.setTitle(value(String.format("%s (%s)", metadataModelVersionV3.getFullName(), metadataModelVersionV3.getVersionIdentifier())));
            }
        } else if (metaDataModel != null && StringUtils.isNotBlank(metaDataModel.getFullName())) {
            if (hasMultipleVersions || StringUtils.isBlank(metadataModelVersionV3.getVersionIdentifier())) {
                m.setTitle(value(metaDataModel.getFullName()));
            } else {
                m.setTitle(value(String.format("%s (%s)", metaDataModel.getFullName(), metadataModelVersionV3.getVersionIdentifier())));
            }
        }

        if (!CollectionUtils.isEmpty(metadataModelVersionV3.getDeveloper())) {
            m.setContributors(metadataModelVersionV3.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        } else if (metaDataModel != null && !CollectionUtils.isEmpty(metaDataModel.getDeveloper())) {
            m.setContributors(metaDataModel.getDeveloper().stream()
                    .map(a -> new TargetInternalReference(
                            IdUtils.getUUID(a.getId()),
                            Helpers.getFullName(a.getFullName(), a.getFamilyName(), a.getGivenName())
                    )).collect(Collectors.toList()));
        }

        if (accessibility == Accessibility.UNDER_EMBARGO) {
            m.setEmbargo(value(Helpers.createEmbargoMessage("metadata model", metadataModelVersionV3.getFileRepository(), dataStage)));
        } else {
            if (metadataModelVersionV3.getFileRepository() != null && metadataModelVersionV3.getFileRepository().getIri() != null) {
                final String iri = metadataModelVersionV3.getFileRepository().getIri();
                final boolean allowEmbedding = Constants.DOMAINS_ALLOWING_EMBEDDING.stream().anyMatch(iri::startsWith);
                if (allowEmbedding) {
                    m.setEmbeddedModelSource(new TargetExternalReference(iri, metadataModelVersionV3.getFileRepository().getFullName()));
                } else {
                    if (metadataModelVersionV3.getFileRepository() != null) {
                        if (Helpers.isCscsContainer(metadataModelVersionV3.getFileRepository()) || Helpers.isDataProxyBucket(metadataModelVersionV3.getFileRepository())) {
                            String endpoint;
                            if (liveMode) {
                                endpoint = String.format("/api/repositories/%s/files/live", IdUtils.getUUID(metadataModelVersionV3.getFileRepository().getId()));
                            } else {
                                endpoint = String.format("/api/groups/%s/repositories/%s/files", dataStage == DataStage.IN_PROGRESS ? "curated" : "public", IdUtils.getUUID(metadataModelVersionV3.getFileRepository().getId()));
                            }
                            m.setFilesAsyncUrl(endpoint);
                        } else {
                            m.setExternalDownload(link(metadataModelVersionV3.getFileRepository()));
                        }
                    }
                }
            }
        }

        String citation = metadataModelVersionV3.getHowToCite();
        String doi = metadataModelVersionV3.getDoi();
        if (StringUtils.isNotBlank(doi)) {
            final String doiWithoutPrefix = Helpers.stripDOIPrefix(doi);
            //TODO do we want to keep this one? It's actually redundant with what we have in "cite dataset"
            m.setDoi(value(doiWithoutPrefix));
            if (StringUtils.isNotBlank(citation)) {
                m.setCitation(value(citation));
            } else {
                m.setCitation(value(Helpers.getFormattedDigitalIdentifier(doiCitationFormatter, doi, RelatedPublication.PublicationType.DOI)));
            }
        } else if (StringUtils.isNotBlank(citation)) {
            m.setCitation(value(citation));
        }
        m.setLicenseInfo(link(metadataModelVersionV3.getLicense()));
        List<FullNameRef> projects = null;
        if (metadataModelVersionV3.getProjects() != null && metaDataModel != null && metaDataModel.getProjects() != null) {
            projects = Stream.concat(metadataModelVersionV3.getProjects().stream(), metaDataModel.getProjects().stream()).distinct().collect(Collectors.toList());
        } else if (metadataModelVersionV3.getProjects() != null) {
            projects = metadataModelVersionV3.getProjects();
        } else if (metaDataModel != null && metaDataModel.getProjects() != null) {
            projects = metaDataModel.getProjects();
        }
        m.setProjects(ref(projects));


        List<PersonOrOrganizationRef> custodians = metadataModelVersionV3.getCustodian();
        if (CollectionUtils.isEmpty(custodians) && metadataModelVersionV3.getMetaDataModel() != null) {
            custodians = metadataModelVersionV3.getMetaDataModel().getCustodian();
        }

        if (!CollectionUtils.isEmpty(custodians)) {
            m.setCustodians(custodians.stream().map(c -> new TargetInternalReference(IdUtils.getUUID(c.getId()), Helpers.getFullName(c.getFullName(), c.getFamilyName(), c.getGivenName()))).collect(Collectors.toList()));
        }

        if (!CollectionUtils.isEmpty(metadataModelVersionV3.getRelatedPublications())) {
            m.setPublications(metadataModelVersionV3.getRelatedPublications().stream().map(p -> Helpers.getFormattedDigitalIdentifier(doiCitationFormatter, p.getIdentifier(), p.resolvedType())).filter(Objects::nonNull).map(Value::new).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(metadataModelVersionV3.getSupportChannel())){
            m.setSupport(metadataModelVersionV3.getSupportChannel().stream().map(s -> {
                if(s.contains("@")){
                    return new TargetExternalReference(String.format("mailto:%s", s), s);
                }
                else{
                    return new TargetExternalReference(s, s);
                }
            }).collect(Collectors.toList()));
        }
        m.setMetaDataModelType(ref(metadataModelVersionV3.getModelType()));
        m.setSerializationFormat(ref(metadataModelVersionV3.getSerializationFormat()));
        m.setSpecificationFormat(ref(metadataModelVersionV3.getSpecificationFormat()));
        if (!CollectionUtils.isEmpty(metadataModelVersionV3.getKeyword())) {
            Collections.sort(metadataModelVersionV3.getKeyword());
            m.setKeywords(value(metadataModelVersionV3.getKeyword()));
        }
        return m;
    }
}
