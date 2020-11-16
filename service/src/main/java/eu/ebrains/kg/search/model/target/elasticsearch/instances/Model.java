package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

public class Model {

    private List<ExternalReference> producedDataset;

    private List<ExternalReference> allFiles;

    private List<Value<String>> modelFormat;

    private Value<String> description;

    @JsonProperty("license_info")
    private ExternalReference licenseInfo;

    private List<InternalReference> owners;

    private List<Value<String>> abstractionLevel;

    private List<InternalReference> mainContact;

    private List<Value<String>> brainStructures;

    private List<InternalReference> usedDataset;

    private Value<String> version;

    private List<InternalReference> publications;

    private List<InternalReference> studyTarget;

    private List<Value<String>> modelScope;

    private Value<String> title;

    private List<InternalReference> contributors;

    private List<Value<String>> cellularTarget;

    @JsonProperty("first_release")
    private Software.Value<Date> firstRelease;

    @JsonProperty("last_release")
    private Software.Value<Date> lastRelease;

    private Software.Value<String> type;

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

    public List<InternalReference> getOwners() {
        return owners;
    }

    public void setOwners(List<InternalReference> owners) {
        this.owners = owners;
    }

    public List<Value<String>> getAbstractionLevel() {
        return abstractionLevel;
    }

    public void setAbstractionLevel(List<Value<String>> abstractionLevel) {
        this.abstractionLevel = abstractionLevel;
    }

    public List<InternalReference> getMainContact() {
        return mainContact;
    }

    public void setMainContact(List<InternalReference> mainContact) {
        this.mainContact = mainContact;
    }

    public List<Value<String>> getBrainStructures() {
        return brainStructures;
    }

    public void setBrainStructures(List<Value<String>> brainStructures) {
        this.brainStructures = brainStructures;
    }

    public List<InternalReference> getUsedDataset() {
        return usedDataset;
    }

    public void setUsedDataset(List<InternalReference> usedDataset) {
        this.usedDataset = usedDataset;
    }

    public Value<String> getVersion() {
        return version;
    }

    public void setVersion(Value<String> version) {
        this.version = version;
    }

    public List<InternalReference> getPublications() {
        return publications;
    }

    public void setPublications(List<InternalReference> publications) {
        this.publications = publications;
    }

    public List<InternalReference> getStudyTarget() {
        return studyTarget;
    }

    public void setStudyTarget(List<InternalReference> studyTarget) {
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

    public List<InternalReference> getContributors() {
        return contributors;
    }

    public void setContributors(List<InternalReference> contributors) {
        this.contributors = contributors;
    }

    public List<Value<String>> getCellularTarget() {
        return cellularTarget;
    }

    public void setCellularTarget(List<Value<String>> cellularTarget) {
        this.cellularTarget = cellularTarget;
    }

    public Software.Value<Date> getFirstRelease() {
        return firstRelease;
    }

    public void setFirstRelease(Software.Value<Date> firstRelease) {
        this.firstRelease = firstRelease;
    }

    public Software.Value<Date> getLastRelease() {
        return lastRelease;
    }

    public void setLastRelease(Software.Value<Date> lastRelease) {
        this.lastRelease = lastRelease;
    }

    public Software.Value<String> getType() {
        return type;
    }

    public void setType(Software.Value<String> type) {
        this.type = type;
    }

    public static class ExternalReference {

        public ExternalReference() {}

        public ExternalReference(String url, String value) {
            this.url = url;
            this.value = value;
        }

        private String url;
        private String value;

        public String getUrl() { return url; }

        public void setUrl(String url) { this.url = url; }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class InternalReference {

        public InternalReference() {
        }

        public InternalReference(String reference, String value) {
            this.reference = reference;
            this.value = value;
        }

        private String reference;
        private String value;

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }


    public static class Value<T>{

        public Value() {}

        public Value(T value) {
            this.value = value;
        }

        private T value;

        public T getValue() {
            return value;
        }
    }
}
