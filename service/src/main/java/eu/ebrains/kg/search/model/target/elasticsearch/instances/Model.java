package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.ExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference;

import java.util.Date;
import java.util.List;

@MetaInfo(name="Model", identifier = "uniminds/core/modelinstance/v1.0.0/search", order=5)
public class Model {
    private Value<String> type = new Value<>("Model");

    @FieldInfo(visible = false, ignoreForSearch = true)
    private Value<String> identifier;

    @FieldInfo(layout = FieldInfo.Layout.HEADER)
    private Value<String> editorId;

    @FieldInfo(label = "Files", layout = FieldInfo.Layout.GROUP, markdown = true)
    private Value<String> embargo;

    @FieldInfo(label = "Produced datasets", layout = FieldInfo.Layout.GROUP)
    private List<ExternalReference> producedDataset;

    @FieldInfo(label = "Download model", isButton=true, termsOfUse=true)
    private List<ExternalReference> allFiles;

    @FieldInfo(label = "Model format", layout = FieldInfo.Layout.SUMMARY, separator = "; ")
    private List<Value<String>> modelFormat;

    @FieldInfo(label = "Description", markdown = true, boost = 2, labelHidden = true)
    private Value<String> description;

    @JsonProperty("license_info")
    @FieldInfo(label = "License", type = FieldInfo.Type.TEXT, facetOrder= FieldInfo.FacetOrder.BYVALUE)
    private ExternalReference licenseInfo;

    @FieldInfo(label = "Custodian", layout = FieldInfo.Layout.SUMMARY, separator = "; ", type = FieldInfo.Type.TEXT, hint = "A custodian is the person responsible for the data bundle.")
    private List<InternalReference> owners;

    @FieldInfo(label = "Abstraction level", layout = FieldInfo.Layout.SUMMARY, separator = "; ", facet = FieldInfo.Facet.LIST)
    private List<Value<String>> abstractionLevel;

    @FieldInfo(label = "Main contact", layout = FieldInfo.Layout.SUMMARY, separator = "; ", type = FieldInfo.Type.TEXT)
    private List<InternalReference> mainContact;

    @FieldInfo(label = "Brain structure", layout = FieldInfo.Layout.SUMMARY, facet = FieldInfo.Facet.LIST)
    private List<Value<String>> brainStructures;

    @FieldInfo(label = "Used datasets", layout = FieldInfo.Layout.GROUP)
    private List<InternalReference> usedDataset;

    @FieldInfo(label = "Version", layout = FieldInfo.Layout.SUMMARY)
    private Value<String> version;

    @FieldInfo(label = "Publications", layout = FieldInfo.Layout.GROUP, markdown = true, hint = "List of publications that have been published as a part of this model.")
    private List<InternalReference> publications;

    @FieldInfo(label = "Study target", layout = FieldInfo.Layout.SUMMARY)
    private List<InternalReference> studyTarget;

    @FieldInfo(label = "Model scope", layout = FieldInfo.Layout.SUMMARY, facet = FieldInfo.Facet.LIST)
    private List<Value<String>> modelScope;

    @FieldInfo(label = "Name", optional = false, sort = true, boost = 20)
    private Value<String> title;

    @FieldInfo(label = "Contributors", layout = FieldInfo.Layout.HEADER, separator = "; ", type = FieldInfo.Type.TEXT, labelHidden = true, boost = 10)
    private List<InternalReference> contributors;

    @FieldInfo(label = "(Sub)cellular target", layout = FieldInfo.Layout.SUMMARY)
    private List<Value<String>> cellularTarget;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private Value<Date> firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private Value<Date> lastRelease;

    public Value<String> getIdentifier() { return identifier; }

    public void setIdentifier(Value<String> identifier) { this.identifier = identifier; }

    public Value<String> getEditorId() { return editorId; }

    public void setEditorId(Value<String> editorId) { this.editorId = editorId; }

    public Value<String> getEmbargo() { return embargo; }

    public void setEmbargo(Value<String> embargo) { this.embargo = embargo; }

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
