package eu.ebrains.kg.search.model.source.openMINDSv2;

import eu.ebrains.kg.search.model.source.SourceInstance;
import eu.ebrains.kg.search.model.source.commonsV1andV2.*;

import java.util.Date;
import java.util.List;

public class ModelV2 implements HasEmbargo, SourceInstance {
    private String id;
    private List<SourceExternalReference> fileBundle;
    private List<SourceInternalReference> custodian;
    private List<SourceInternalReference> mainContact;
    private String title;
    private List<String> modelFormat;
    private List<String> cellularTarget;
    private String editorId;
    private String version;
    private List<String> studyTarget;
    private List<SourceInternalReference> usedDataset; //TODO: validate the type.
    private String description;
    private List<SourceExternalReference> license;
    private List<String> modelScope;
    private List<String> abstractionLevel;
    private List<SourceInternalReference> producedDataset;
    private List<Publication> publications; //TODO: Check why doi is a List ?
    private List<String> brainStructure;
    private List<SourceInternalReference> contributors;
    private List<String> embargo;
    private String identifier;
    private Date lastReleaseAt;
    private Date firstReleaseAt;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public Date getLastReleaseAt() { return lastReleaseAt; }

    public void setLastReleaseAt(Date lastReleaseAt) { this.lastReleaseAt = lastReleaseAt; }

    public Date getFirstReleaseAt() { return firstReleaseAt; }

    public void setFirstReleaseAt(Date firstReleaseAt) { this.firstReleaseAt = firstReleaseAt; }

    public List<SourceExternalReference> getFileBundle() { return fileBundle; }

    public void setFileBundle(List<SourceExternalReference> fileBundle) { this.fileBundle = fileBundle; }

    public List<SourceInternalReference> getCustodian() { return custodian; }

    public void setCustodian(List<SourceInternalReference> custodian) { this.custodian = custodian; }

    public List<SourceInternalReference> getMainContact() { return mainContact; }

    public void setMainContact(List<SourceInternalReference> mainContact) { this.mainContact = mainContact; }

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

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public List<SourceExternalReference> getLicense() { return license; }

    public void setLicense(List<SourceExternalReference> license) { this.license = license; }

    public List<String> getModelScope() { return modelScope; }

    public void setModelScope(List<String> modelScope) { this.modelScope = modelScope; }

    public List<String> getAbstractionLevel() { return abstractionLevel; }

    public void setAbstractionLevel(List<String> abstractionLevel) { this.abstractionLevel = abstractionLevel; }

    public List<SourceInternalReference> getProducedDataset() { return producedDataset; }

    public void setProducedDataset(List<SourceInternalReference> producedDataset) { this.producedDataset = producedDataset; }

    public List<Publication> getPublications() { return publications; }

    public void setPublications(List<Publication> publications) { this.publications = publications; }

    public List<String> getBrainStructure() { return brainStructure; }

    public void setBrainStructure(List<String> brainStructure) { this.brainStructure = brainStructure; }

    public List<SourceInternalReference> getContributors() { return contributors; }

    public void setContributors(List<SourceInternalReference> contributors) { this.contributors = contributors; }

    @Override
    public List<String> getEmbargo() { return embargo; }

    public void setEmbargo(List<String> embargo) { this.embargo = embargo; }

    public String getIdentifier() { return identifier; }

    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public List<SourceInternalReference> getUsedDataset() { return usedDataset; }

    public void setUsedDataset(List<SourceInternalReference> usedDataset) { this.usedDataset = usedDataset; }
}
