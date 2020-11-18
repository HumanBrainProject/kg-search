package eu.ebrains.kg.search.model.source.openMINDSv1;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.source.commons.Publication;
import eu.ebrains.kg.search.model.source.commons.Reference;
import eu.ebrains.kg.search.model.source.commons.Subject;
import eu.ebrains.kg.search.model.source.commons.File;
import eu.ebrains.kg.search.model.source.commons.ParcellationRegion;

import java.util.Date;
import java.util.List;

public class DatasetV1 {
    private List<String> methods;
    private Date lastReleaseAt;
    private List<Reference> component;
    private List<String> embargoRestrictedAccess;
    @JsonProperty("external_datalink") //TODO: Capitalize the property
    private String externalDatalink;
    private List<String> embargoForFilter;
    private List<String> doi;
    private List<File> files;
    private List<String> parcellationAtlas;
    private List<ExternalReference> neuroglancer;
    private List<Publication> publications;
    private List<Subject> subjects;
    private List<Reference> contributors;
    private String identifier;
    private List<String> speciesFilter;
    private Date firstReleaseAt;
    private List<String> citation;
    private List<ExternalReference> brainViewer;
    private List<String> preparation;
    private String title;
    private String editorId;
    private List<Reference> owners;
    private Boolean containerUrlAsZIP;
    private List<String> protocols;
    @JsonProperty("container_url")
    private String containerUrl;
    private String dataDescriptorURL;
    private List<ParcellationRegion> parcellationRegion;
    private String description;
    private List<ExternalReference> license;
    private List<String> modalityForFilter;
    private List<String> embargo;

    public List<String> getMethods() { return methods; }

    public void setMethods(List<String> methods) { this.methods = methods; }

    public Date getLastReleaseAt() { return lastReleaseAt; }

    public void setLastReleaseAt(Date lastReleaseAt) { this.lastReleaseAt = lastReleaseAt; }

    public List<Reference> getComponent() { return component; }

    public void setComponent(List<Reference> component) { this.component = component; }

    public List<String> getEmbargoRestrictedAccess() { return embargoRestrictedAccess; }

    public void setEmbargoRestrictedAccess(List<String> embargoRestrictedAccess) { this.embargoRestrictedAccess = embargoRestrictedAccess; }

    public String getExternalDatalink() { return externalDatalink; }

    public void setExternalDatalink(String externalDatalink) { this.externalDatalink = externalDatalink; }

    public List<String> getEmbargoForFilter() { return embargoForFilter; }

    public void setEmbargoForFilter(List<String> embargoForFilter) { this.embargoForFilter = embargoForFilter; }

    public List<String> getDoi() { return doi; }

    public void setDoi(List<String> doi) { this.doi = doi; }

    public List<File> getFiles() { return files; }

    public void setFiles(List<File> files) { this.files = files; }

    public List<String> getParcellationAtlas() { return parcellationAtlas; }

    public void setParcellationAtlas(List<String> parcellationAtlas) { this.parcellationAtlas = parcellationAtlas; }

    public List<ExternalReference> getNeuroglancer() { return neuroglancer; }

    public void setNeuroglancer(List<ExternalReference> neuroglancer) { this.neuroglancer = neuroglancer; }

    public List<Publication> getPublications() { return publications; }

    public void setPublications(List<Publication> publications) { this.publications = publications; }

    public List<Subject> getSubjects() { return subjects; }

    public void setSubjects(List<Subject> subjects) { this.subjects = subjects; }

    public List<Reference> getContributors() { return contributors; }

    public void setContributors(List<Reference> contributors) { this.contributors = contributors; }

    public String getIdentifier() { return identifier; }

    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public List<String> getSpeciesFilter() { return speciesFilter; }

    public void setSpeciesFilter(List<String> speciesFilter) { this.speciesFilter = speciesFilter; }

    public Date getFirstReleaseAt() { return firstReleaseAt; }

    public void setFirstReleaseAt(Date firstReleaseAt) { this.firstReleaseAt = firstReleaseAt; }

    public List<String> getCitation() { return citation; }

    public void setCitation(List<String> citation) { this.citation = citation; }

    public List<ExternalReference> getBrainViewer() { return brainViewer; }

    public void setBrainViewer(List<ExternalReference> brainViewer) { this.brainViewer = brainViewer; }

    public List<String> getPreparation() { return preparation; }

    public void setPreparation(List<String> preparation) { this.preparation = preparation; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getEditorId() { return editorId; }

    public void setEditorId(String editorId) { this.editorId = editorId; }

    public List<Reference> getOwners() { return owners; }

    public void setOwners(List<Reference> owners) { this.owners = owners; }

    public Boolean getContainerUrlAsZIP() { return containerUrlAsZIP; }

    public void setContainerUrlAsZIP(Boolean containerUrlAsZIP) { this.containerUrlAsZIP = containerUrlAsZIP; }

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

    public List<ExternalReference> getLicense() { return license; }

    public void setLicense(List<ExternalReference> license) { this.license = license; }

    public List<String> getModalityForFilter() { return modalityForFilter; }

    public void setModalityForFilter(List<String> modalityForFilter) { this.modalityForFilter = modalityForFilter; }

    public List<String> getEmbargo() { return embargo; }

    public void setEmbargo(List<String> embargo) { this.embargo = embargo; }

    public static class ExternalReference {
        private String url;
        private String name;

        public String getUrl() { return url; }

        public void setUrl(String url) { this.url = url; }

        public String getName() { return name; }

        public void setName(String name) { this.name = name; }

    }
}
