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
import eu.ebrains.kg.search.model.source.openMINDSv3.DatasetVersionV3;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.PersonOrOrganizationRef;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.DatasetVersion;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.search.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

//Test ids
// 4ac9f0bc-560d-47e0-8916-7b24da9bb0ce (multiple versions)
// 4840dd00-058b-437c-9d0f-091b482d51b8 (experimental approaches and techniques)


public class DatasetVersionOfKGV3Translator implements Translator<DatasetVersionV3, DatasetVersion> {

    private enum Accessibility{
        FREE_ACCESS("https://openminds.ebrains.eu/instances/productAccessibility/freeAccess"),
        CONTROLLED_ACCESS("https://openminds.ebrains.eu/instances/productAccessibility/controlledAccess"),
        RESTRICTED_ACCESS("https://openminds.ebrains.eu/instances/productAccessibility/restrictedAccess"),
        UNDER_EMBARGO("https://openminds.ebrains.eu/instances/productAccessibility/underEmbargo");

        private final String identifier;

        Accessibility(String identifier){
            this.identifier = identifier;
        }

        public static Accessibility fromPayload(DatasetVersionV3 datasetVersion){
            if(datasetVersion!=null && datasetVersion.getAccessibility()!=null && datasetVersion.getAccessibility().getIdentifier()!=null){
                return Arrays.stream(Accessibility.values()).filter(a -> datasetVersion.getAccessibility().getIdentifier().contains(a.identifier)).findFirst().orElse(null);
            }
            return null;
        }

    }

    public DatasetVersion translate(DatasetVersionV3 datasetVersion, DataStage dataStage, boolean liveMode) {
        DatasetVersion d = new DatasetVersion();
        DatasetVersionV3.DatasetVersions dataset = datasetVersion.getDataset();
        Accessibility accessibility = Accessibility.fromPayload(datasetVersion);
        String containerUrl = datasetVersion.getFileRepository()!=null ? datasetVersion.getFileRepository().getIri() : null;
        if(accessibility!=null){
            switch (accessibility){
                case CONTROLLED_ACCESS:
                    d.setUseHDG(DatasetVersion.createHDGMessage(datasetVersion.getUUID(), false));
                    d.setEmbargo(DatasetVersion.createHDGMessage(datasetVersion.getUUID(), true));
                    break;
                case UNDER_EMBARGO:
                    d.setEmbargo(DatasetVersion.EMBARGO_MESSAGE);
                    if(dataStage == DataStage.IN_PROGRESS && containerUrl!=null){
                        d.setEmbargoRestrictedAccess(DatasetVersion.createEmbargoInProgressMessage(containerUrl));
                    }
                    break;
                case RESTRICTED_ACCESS:
                    d.setEmbargo(DatasetVersion.RESTRICTED_ACCESS_MESSAGE);
                    break;
            }
            d.setEmbargoForFilter(new Value<>(datasetVersion.getAccessibility().getName()));
            d.setDataAccessibility(datasetVersion.getAccessibility().getName());
        }

        final String uuid = IdUtils.getUUID(datasetVersion.getId());
        d.setId(uuid);
        if (dataStage == DataStage.IN_PROGRESS) {
            d.setEditorId(uuid);
        }
        //TODO d.setExternalDatalink
        //d.setFiles -> replaced with d.setFileRepository

        if(datasetVersion.getProtocols() != null){
            d.setMethods(datasetVersion.getProtocols().stream().map(DatasetVersionV3.Protocol::getTechnique)
                    .filter(Objects::nonNull).flatMap(Collection::stream)
                    .map(DatasetVersionV3.OntologicalTerm::getName)
                    .filter(Objects::nonNull).distinct().sorted().collect(Collectors.toList()));

        }

        d.setModalityForFilter(datasetVersion.getExperimentalApproach());

        //TODO d.setZip -> goes to the file download section (file repository) -> shouldn't we materialize this?
        //TODO d.setDataDescriptor ->
        //TODO d.setPublications -> Where do we store the citation?
        //TODO d.setAtlas
        //TODO d.setRegion
        //TODO d.setPreparation
        //TODO d.setViewer -> Linked to files / filebundles (data missing)
        //TODO d.setSubjects -> Becomes "specimens"
        //TODO d.setFirstRelease
        //TODO d.setLastRelease
        //TODO d.setDataset


        d.setIdentifier(IdUtils.getUUID(datasetVersion.getIdentifier()));
        List<Version> versions = dataset == null?null:dataset.getVersions();
        if (!CollectionUtils.isEmpty(versions) && versions.size() > 1) {
            d.setVersion(datasetVersion.getVersion());
            List<Version> sortedVersions = Helpers.sort(versions);
            List<TargetInternalReference> references = sortedVersions.stream().map(v -> new TargetInternalReference(IdUtils.getUUID(v.getId()), v.getVersionIdentifier())).collect(Collectors.toList());
            references.add(new TargetInternalReference(IdUtils.getUUID(dataset.getId()), "All versions"));
            d.setVersions(references);
            // if versions cannot be sorted (sortedVersions == versions) we flag it as searchable
            d.setSearchable(sortedVersions == versions || sortedVersions.get(0).getId().equals(datasetVersion.getId()));
        } else {
            d.setSearchable(true);
        }

        if (StringUtils.isNotBlank(datasetVersion.getDescription())) {
            d.setDescription(datasetVersion.getDescription());
        } else if (dataset != null) {
            d.setDescription(dataset.getDescription());
        }
        if(StringUtils.isNotBlank(datasetVersion.getVersionInnovation())) {
            d.setNewInThisVersion(new Value<>(datasetVersion.getVersionInnovation()));
        }


        if (dataset != null && StringUtils.isNotBlank(dataset.getFullName())) {
            d.setTitle(dataset.getFullName());
        } else if (StringUtils.isNotBlank(datasetVersion.getFullName())) {
            d.setTitle(datasetVersion.getFullName());
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
            d.setDoi(doiWithoutPrefix);
            if (StringUtils.isNotBlank(citation)) {
                d.setCitation(String.format("%s [DOI: %s](%s)", citation, doiWithoutPrefix, doi));
            }
            else{
                d.setCitation(String.format("[DOI: %s](%s)", doiWithoutPrefix, doi));
            }
        }
        else if (StringUtils.isNotBlank(citation)) {
            d.setCitation(citation);
        }

        if(datasetVersion.getLicense()!=null && StringUtils.isNotBlank(datasetVersion.getLicense().getLegalCode())){
            d.setLicenseInfo(new TargetExternalReference(datasetVersion.getLicense().getLegalCode(), datasetVersion.getLicense().getFullName()!=null ? datasetVersion.getLicense().getFullName() : datasetVersion.getLicense().getLegalCode()));
        }

        if(datasetVersion.getProjects()!=null){
            d.setProjects(datasetVersion.getProjects().stream().map(r -> new TargetInternalReference(IdUtils.getUUID(r.getId()), r.getFullName())).collect(Collectors.toList()));
        }

        if (datasetVersion.getFileRepository() != null) {
            d.setFileRepository(new TargetInternalReference(datasetVersion.getFileRepository().getId(), datasetVersion.getFileRepository().getFullName()));
        }

        List<PersonOrOrganizationRef> custodians = datasetVersion.getCustodians();
        if(CollectionUtils.isEmpty(custodians) && datasetVersion.getDataset() != null){
            custodians = datasetVersion.getDataset().getCustodians();
        }

        if(!CollectionUtils.isEmpty(custodians) ){
            d.setCustodians(custodians.stream().map(c -> new TargetInternalReference(IdUtils.getUUID(c.getId()), Helpers.getFullName(c.getFullName(), c.getFamilyName(), c.getGivenName()))).collect(Collectors.toList()));
        }

        if(!CollectionUtils.isEmpty(datasetVersion.getKeyword())){
            Collections.sort(datasetVersion.getKeyword());
            d.setKeywords(datasetVersion.getKeyword());
        }
        if(!CollectionUtils.isEmpty(datasetVersion.getStudiedSpecimen())){
            d.setSpeciesFilter(datasetVersion.getStudiedSpecimen().stream().map(DatasetVersionV3.Specimen::getSpecies).filter(Objects::nonNull).distinct().sorted().collect(Collectors.toList()));
        }

        return d;
    }
}
