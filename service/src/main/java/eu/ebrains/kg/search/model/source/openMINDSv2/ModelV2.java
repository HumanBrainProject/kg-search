package eu.ebrains.kg.search.model.source.openMINDSv2;

import eu.ebrains.kg.search.model.source.commons.ExternalReference;
import eu.ebrains.kg.search.model.source.commons.Reference;

import java.util.List;

public class ModelV2 {
    private List<ExternalReference> fileBundle;
    private List<Reference> custodian;
    private List<Reference> mainContact;
    private String title;
    private List<String> modelFormat;
    private List<String> cellularTarget;
    private String editorId;
    private String version;
    private List<String> studyTarget;
    private List<String> usedDataset; //TODO: validate the type.
    private String description;
    private List<ExternalReference> license;
    private List<String> modelScope;
    private List<String> abstractionLevel;
    private List<Reference> producedDataset;
    private List<Publication> publications; //TODO: Check why doi is a List ?
    private List<String> brainStructure;
    private List<Reference> contributors;
    private List<String> embargo;
    private String identifier;

    public List<ExternalReference> getFileBundle() { return fileBundle; }

    public void setFileBundle(List<ExternalReference> fileBundle) { this.fileBundle = fileBundle; }

    public List<Reference> getCustodian() { return custodian; }

    public void setCustodian(List<Reference> custodian) { this.custodian = custodian; }

    public List<Reference> getMainContact() { return mainContact; }

    public void setMainContact(List<Reference> mainContact) { this.mainContact = mainContact; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public List<String> getModelFormat() { return modelFormat; }

    public void setModelFormat(List<String> modelFormat) { this.modelFormat = modelFormat; }

    public List<String> getCellularTarget() { return cellularTarget; }

    public void setCellularTarget(List<String> cellularTarget) { this.cellularTarget = cellularTarget; }

    public String getEditorId() { return editorId; }

    public void setEditorId(String editorId) { this.editorId = editorId; }

    public String getVersion() { return version; }

    public void setVersion(String version) { this.version = version; }

    public List<String> getStudyTarget() { return studyTarget; }

    public void setStudyTarget(List<String> studyTarget) { this.studyTarget = studyTarget; }

    public List<String> getUsedDataset() { return usedDataset; }

    public void setUsedDataset(List<String> usedDataset) { this.usedDataset = usedDataset; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public List<ExternalReference> getLicense() { return license; }

    public void setLicense(List<ExternalReference> license) { this.license = license; }

    public List<String> getModelScope() { return modelScope; }

    public void setModelScope(List<String> modelScope) { this.modelScope = modelScope; }

    public List<String> getAbstractionLevel() { return abstractionLevel; }

    public void setAbstractionLevel(List<String> abstractionLevel) { this.abstractionLevel = abstractionLevel; }

    public List<Reference> getProducedDataset() { return producedDataset; }

    public void setProducedDataset(List<Reference> producedDataset) { this.producedDataset = producedDataset; }

    public List<Publication> getPublications() { return publications; }

    public void setPublications(List<Publication> publications) { this.publications = publications; }

    public List<String> getBrainStructure() { return brainStructure; }

    public void setBrainStructure(List<String> brainStructure) { this.brainStructure = brainStructure; }

    public List<Reference> getContributors() { return contributors; }

    public void setContributors(List<Reference> contributors) { this.contributors = contributors; }

    public List<String> getEmbargo() { return embargo; }

    public void setEmbargo(List<String> embargo) { this.embargo = embargo; }

    public String getIdentifier() { return identifier; }

    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public static class Publication {
        private String url;
        private List<String> doi;

        public String getUrl() { return url; }

        public void setUrl(String url) { this.url = url; }

        public List<String> getDoi() { return doi; }

        public void setDoi(List<String> doi) { this.doi = doi; }
    }

}
