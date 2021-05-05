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

package eu.ebrains.kg.search.model.source.openMINDSv1;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.ebrains.kg.search.model.source.SourceInstance;
import eu.ebrains.kg.search.model.source.commons.*;

import java.util.Date;
import java.util.List;

public class DatasetV1 implements HasEmbargo, SourceInstance {
    private String id;
    private List<String> methods;
    private Date lastReleaseAt;
    private List<SourceInternalReference> component;
    private List<String> embargoRestrictedAccess;
    @JsonProperty("external_datalink") //TODO: Capitalize the property
    @JsonDeserialize(using = ListOrSingleStringAsListDeserializer.class)
    private List<String> externalDatalink;
    private List<String> embargoForFilter;
    private List<String> doi;
    private List<SourceFile> files;
    private List<String> parcellationAtlas;
    private List<SourceExternalReference> neuroglancer;
    private List<Publication> publications;
    private List<Subject> subjects;
    private List<SourceInternalReference> contributors;
    private String identifier;
    private List<String> speciesFilter;
    private Date firstReleaseAt;
    private List<String> citation;
    @JsonProperty("brainviewer")
    private List<SourceExternalReference> brainViewer;
    private List<String> preparation;
    private String title;
    private String editorId;
    private List<SourceInternalReference> owners;
    private boolean containerUrlAsZIP;
    private List<String> protocols;
    @JsonProperty("container_url")
    private String containerUrl;
    private String dataDescriptorURL;
    private List<ParcellationRegion> parcellationRegion;
    private String description;
    private List<SourceExternalReference> license;
    private List<String> modalityForFilter;
    private List<String> embargo;
    private boolean useHDG;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public boolean isUseHDG() { return useHDG; }

    public void setUseHDG(boolean useHDG) { this.useHDG = useHDG; }

    public List<String> getMethods() { return methods; }

    public void setMethods(List<String> methods) { this.methods = methods; }

    public Date getLastReleaseAt() { return lastReleaseAt; }

    public void setLastReleaseAt(Date lastReleaseAt) { this.lastReleaseAt = lastReleaseAt; }

    public List<SourceInternalReference> getComponent() { return component; }

    public void setComponent(List<SourceInternalReference> component) { this.component = component; }

    public List<String> getEmbargoRestrictedAccess() { return embargoRestrictedAccess; }

    public void setEmbargoRestrictedAccess(List<String> embargoRestrictedAccess) { this.embargoRestrictedAccess = embargoRestrictedAccess; }

    public List<String> getExternalDatalink() { return externalDatalink; }

    public void setExternalDatalink(List<String> externalDatalink) { this.externalDatalink = externalDatalink; }

    public List<String> getEmbargoForFilter() { return embargoForFilter; }

    public void setEmbargoForFilter(List<String> embargoForFilter) { this.embargoForFilter = embargoForFilter; }

    public List<String> getDoi() { return doi; }

    public void setDoi(List<String> doi) { this.doi = doi; }

    public List<SourceFile> getFiles() { return files; }

    public void setFiles(List<SourceFile> files) { this.files = files; }

    public List<String> getParcellationAtlas() { return parcellationAtlas; }

    public void setParcellationAtlas(List<String> parcellationAtlas) { this.parcellationAtlas = parcellationAtlas; }

    public List<SourceExternalReference> getNeuroglancer() { return neuroglancer; }

    public void setNeuroglancer(List<SourceExternalReference> neuroglancer) { this.neuroglancer = neuroglancer; }

    public List<Publication> getPublications() { return publications; }

    public void setPublications(List<Publication> publications) { this.publications = publications; }

    public List<Subject> getSubjects() { return subjects; }

    public void setSubjects(List<Subject> subjects) { this.subjects = subjects; }

    public List<SourceInternalReference> getContributors() { return contributors; }

    public void setContributors(List<SourceInternalReference> contributors) { this.contributors = contributors; }

    public String getIdentifier() { return identifier; }

    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public List<String> getSpeciesFilter() { return speciesFilter; }

    public void setSpeciesFilter(List<String> speciesFilter) { this.speciesFilter = speciesFilter; }

    public Date getFirstReleaseAt() { return firstReleaseAt; }

    public void setFirstReleaseAt(Date firstReleaseAt) { this.firstReleaseAt = firstReleaseAt; }

    public List<String> getCitation() { return citation; }

    public void setCitation(List<String> citation) { this.citation = citation; }

    public List<SourceExternalReference> getBrainViewer() { return brainViewer; }

    public void setBrainViewer(List<SourceExternalReference> brainViewer) { this.brainViewer = brainViewer; }

    public List<String> getPreparation() { return preparation; }

    public void setPreparation(List<String> preparation) { this.preparation = preparation; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getEditorId() { return editorId; }

    public void setEditorId(String editorId) { this.editorId = editorId; }

    public List<SourceInternalReference> getOwners() { return owners; }

    public void setOwners(List<SourceInternalReference> owners) { this.owners = owners; }

    public boolean getContainerUrlAsZIP() { return containerUrlAsZIP; }

    public void setContainerUrlAsZIP(boolean containerUrlAsZIP) { this.containerUrlAsZIP = containerUrlAsZIP; }

    public List<String> getProtocols() { return protocols; }

    public void setProtocols(List<String> protocols) { this.protocols = protocols; }

    public String getContainerUrl() { return containerUrl; }

    public void setContainerUrl(String containerUrl) { this.containerUrl = containerUrl; }

    public String getDataDescriptorURL() { return dataDescriptorURL; }

    public void setDataDescriptorURL(String dataDescriptorURL) { this.dataDescriptorURL = dataDescriptorURL; }

    public List<ParcellationRegion> getParcellationRegion() { return parcellationRegion; }

    public void setParcellationRegion(List<ParcellationRegion> parcellationRegion) { this.parcellationRegion = parcellationRegion; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public List<SourceExternalReference> getLicense() { return license; }

    public void setLicense(List<SourceExternalReference> license) { this.license = license; }

    public List<String> getModalityForFilter() { return modalityForFilter; }

    public void setModalityForFilter(List<String> modalityForFilter) { this.modalityForFilter = modalityForFilter; }

    @Override
    public List<String> getEmbargo() { return embargo; }

    public void setEmbargo(List<String> embargo) { this.embargo = embargo; }

    public static class SourceFile {
        private String name;

        @JsonProperty("absolute_path")
        private String absolutePath;

        @JsonProperty("human_readable_size")
        private String humanReadableSize;

        @JsonProperty("static_image_url")
        private List<String> staticImageUrl;

        @JsonProperty("is_preview_animated")
        private List<Boolean> isPreviewAnimated;

        @JsonProperty("preview_url")
        private List<String> previewUrl;

        @JsonProperty("private_access")
        private boolean privateAccess;

        @JsonProperty("thumbnail_url")
        private List<String> thumbnailUrl;

        public String getAbsolutePath() { return absolutePath; }

        public void setAbsolutePath(String absolutePath) { this.absolutePath = absolutePath; }

        public String getHumanReadableSize() { return humanReadableSize; }

        public void setHumanReadableSize(String humanReadableSize) { this.humanReadableSize = humanReadableSize; }

        public List<String> getStaticImageUrl() { return staticImageUrl; }

        public void setStaticImageUrl(List<String> staticImageUrl) { this.staticImageUrl = staticImageUrl; }

        public List<Boolean> getPreviewAnimated() { return isPreviewAnimated; }

        public void setPreviewAnimated(List<Boolean> previewAnimated) { isPreviewAnimated = previewAnimated; }

        public String getName() { return name; }

        public void setName(String name) { this.name = name; }

        public List<String> getPreviewUrl() { return previewUrl; }

        public void setPreviewUrl(List<String> previewUrl) { this.previewUrl = previewUrl; }

        public boolean getPrivateAccess() { return privateAccess; }

        public void setPrivateAccess(boolean privateAccess) { this.privateAccess = privateAccess; }

        public List<String> getThumbnailUrl() { return thumbnailUrl; }

        public void setThumbnailUrl(List<String> thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    }


}
