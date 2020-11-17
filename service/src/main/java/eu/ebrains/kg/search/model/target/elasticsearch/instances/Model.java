package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.ExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;

import java.util.Date;
import java.util.List;

public class Model {

    private List<ExternalReference> producedDataset;

    private List<ExternalReference> allFiles;

    private List<Value<String>> modelFormat;

    private Value<String> description;

    @JsonProperty("license_info")
    private ExternalReference licenseInfo;

    private List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> owners;

    private List<Value<String>> abstractionLevel;

    private List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> mainContact;

    private List<Value<String>> brainStructures;

    private List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> usedDataset;

    private Value<String> version;

    private List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> publications;

    private List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> studyTarget;

    private List<Value<String>> modelScope;

    private Value<String> title;

    private List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> contributors;

    private List<Value<String>> cellularTarget;

    @JsonProperty("first_release")
    private Value<Date> firstRelease;

    @JsonProperty("last_release")
    private Value<Date> lastRelease;

    private Value<String> type;

    public List<ExternalReference> getProducedDataset() {
        return producedDataset;
    }

    public void setProducedDataset(List<ExternalReference> producedDataset) {
        this.producedDataset = producedDataset;
    }

    public List<ExternalReference> getAllFiles() {
        return allFiles;
    }

    public void setAllFiles(List<ExternalReference> allFiles) {
        this.allFiles = allFiles;
    }

    public List<Value<String>> getModelFormat() {
        return modelFormat;
    }

    public void setModelFormat(List<Value<String>> modelFormat) {
        this.modelFormat = modelFormat;
    }

    public Value<String> getDescription() {
        return description;
    }

    public void setDescription(Value<String> description) {
        this.description = description;
    }

    public ExternalReference getLicenseInfo() {
        return licenseInfo;
    }

    public void setLicenseInfo(ExternalReference licenseInfo) {
        this.licenseInfo = licenseInfo;
    }

    public List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> getOwners() {
        return owners;
    }

    public void setOwners(List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> owners) {
        this.owners = owners;
    }

    public List<Value<String>> getAbstractionLevel() {
        return abstractionLevel;
    }

    public void setAbstractionLevel(List<Value<String>> abstractionLevel) {
        this.abstractionLevel = abstractionLevel;
    }

    public List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> getMainContact() {
        return mainContact;
    }

    public void setMainContact(List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> mainContact) {
        this.mainContact = mainContact;
    }

    public List<Value<String>> getBrainStructures() {
        return brainStructures;
    }

    public void setBrainStructures(List<Value<String>> brainStructures) {
        this.brainStructures = brainStructures;
    }

    public List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> getUsedDataset() {
        return usedDataset;
    }

    public void setUsedDataset(List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> usedDataset) {
        this.usedDataset = usedDataset;
    }

    public Value<String> getVersion() {
        return version;
    }

    public void setVersion(Value<String> version) {
        this.version = version;
    }

    public List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> getPublications() {
        return publications;
    }

    public void setPublications(List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> publications) {
        this.publications = publications;
    }

    public List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> getStudyTarget() {
        return studyTarget;
    }

    public void setStudyTarget(List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> studyTarget) {
        this.studyTarget = studyTarget;
    }

    public List<Value<String>> getModelScope() {
        return modelScope;
    }

    public void setModelScope(List<Value<String>> modelScope) {
        this.modelScope = modelScope;
    }

    public Value<String> getTitle() {
        return title;
    }

    public void setTitle(Value<String> title) {
        this.title = title;
    }

    public List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> getContributors() {
        return contributors;
    }

    public void setContributors(List<eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference> contributors) {
        this.contributors = contributors;
    }

    public List<Value<String>> getCellularTarget() {
        return cellularTarget;
    }

    public void setCellularTarget(List<Value<String>> cellularTarget) {
        this.cellularTarget = cellularTarget;
    }

    public Value<Date> getFirstRelease() {
        return firstRelease;
    }

    public void setFirstRelease(Value<Date> firstRelease) {
        this.firstRelease = firstRelease;
    }

    public Value<Date> getLastRelease() {
        return lastRelease;
    }

    public void setLastRelease(Value<Date> lastRelease) {
        this.lastRelease = lastRelease;
    }

    public Value<String> getType() {
        return type;
    }

    public void setType(Value<String> type) {
        this.type = type;
    }

}
