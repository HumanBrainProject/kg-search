package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetFile;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@MetaInfo(name = "Model", identifier = "uniminds/core/modelinstance/v1.0.0/search", order = 5)
public class Model {
    private Value<String> type = new Value<>("Model");

    @FieldInfo(visible = false, ignoreForSearch = true)
    private Value<String> identifier;

    @FieldInfo(layout = FieldInfo.Layout.HEADER)
    private Value<String> editorId;

    @FieldInfo(label = "Files", layout = FieldInfo.Layout.GROUP, markdown = true)
    private Value<String> embargo;

    @FieldInfo(label = "Produced datasets", layout = FieldInfo.Layout.GROUP)
    private List<TargetInternalReference> producedDataset;

    @JsonProperty("allfiles") //TODO: capitalize
    @FieldInfo(label = "Download model", isButton = true, termsOfUse = true)
    private List<TargetExternalReference> allFiles;

    @FieldInfo(label = "Files", layout = FieldInfo.Layout.GROUP, termsOfUse = true)
    private List<TargetFile> files;

    @FieldInfo(label = "Model format", layout = FieldInfo.Layout.SUMMARY, separator = "; ")
    private List<Value<String>> modelFormat;

    @FieldInfo(label = "Description", markdown = true, boost = 2, labelHidden = true)
    private Value<String> description;

    @JsonProperty("license_info")
    @FieldInfo(label = "License", type = FieldInfo.Type.TEXT, facetOrder = FieldInfo.FacetOrder.BYVALUE)
    private TargetExternalReference licenseInfo;

    @FieldInfo(label = "Custodian", layout = FieldInfo.Layout.SUMMARY, separator = "; ", type = FieldInfo.Type.TEXT, hint = "A custodian is the person responsible for the data bundle.")
    private List<TargetInternalReference> owners;

    @FieldInfo(label = "Abstraction level", layout = FieldInfo.Layout.SUMMARY, separator = "; ", facet = FieldInfo.Facet.LIST)
    private List<Value<String>> abstractionLevel;

    @FieldInfo(label = "Main contact", layout = FieldInfo.Layout.SUMMARY, separator = "; ", type = FieldInfo.Type.TEXT)
    private List<TargetInternalReference> mainContact;

    @FieldInfo(label = "Brain structure", layout = FieldInfo.Layout.SUMMARY, facet = FieldInfo.Facet.LIST)
    private List<Value<String>> brainStructures;

    @FieldInfo(label = "Used datasets", layout = FieldInfo.Layout.GROUP)
    private List<TargetInternalReference> usedDataset;

    @FieldInfo(label = "Version", layout = FieldInfo.Layout.SUMMARY)
    private Value<String> version;

    @FieldInfo(label = "Publications", layout = FieldInfo.Layout.GROUP, markdown = true, hint = "List of publications that have been published as a part of this model.")
    private List<String> publications;

    @FieldInfo(label = "Study target", layout = FieldInfo.Layout.SUMMARY)
    private List<Value<String>> studyTarget;

    @FieldInfo(label = "Model scope", layout = FieldInfo.Layout.SUMMARY, facet = FieldInfo.Facet.LIST)
    private List<Value<String>> modelScope;

    @FieldInfo(label = "Name", optional = false, sort = true, boost = 20)
    private Value<String> title;

    @FieldInfo(label = "Contributors", layout = FieldInfo.Layout.HEADER, separator = "; ", type = FieldInfo.Type.TEXT, labelHidden = true, boost = 10)
    private List<TargetInternalReference> contributors;

    @FieldInfo(label = "(Sub)cellular target", layout = FieldInfo.Layout.SUMMARY)
    private List<Value<String>> cellularTarget;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private Value<Date> firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private Value<Date> lastRelease;

    public void setType(String type) {
        setType(type != null ? new Value<>(type) : null);
    }

    public void setIdentifier(String identifier) {
        setIdentifier(identifier != null ? new Value<>(identifier) : null);
    }

    public void setEditorId(String editorId) {
        setEditorId(editorId != null ? new Value<>(editorId) : null);
    }

    public void setEmbargo(String embargo) {
        setEmbargo(embargo != null ? new Value<>(embargo) : null);
    }

    public void setDescription(String description) {
        setDescription(description != null ? new Value<>(description) : null);
    }

    public void setVersion(String version) {
        setVersion(version != null ? new Value<>(version) : null);
    }

    public void setTitle(String title) {
        setTitle(title != null ? new Value<>(title) : null);
    }

    public void setFirstRelease(Date firstRelease) {
        setFirstRelease(firstRelease != null ? new Value<>(firstRelease) : null);
    }

    public void setLastRelease(Date lastRelease) {
        setLastRelease(lastRelease != null ? new Value<>(lastRelease) : null);
    }

    public Value<String> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Value<String> identifier) {
        this.identifier = identifier;
    }

    public Value<String> getEditorId() {
        return editorId;
    }

    public void setEditorId(Value<String> editorId) {
        this.editorId = editorId;
    }

    public Value<String> getEmbargo() {
        return embargo;
    }

    public void setEmbargo(Value<String> embargo) {
        this.embargo = embargo;
    }

    public List<TargetInternalReference> getProducedDataset() {
        return producedDataset;
    }

    public void setProducedDataset(List<TargetInternalReference> producedDataset) {
        this.producedDataset = producedDataset.isEmpty() ? null : producedDataset; // TODO: Should we completely skip the value if it is null ?
    }

    public List<TargetExternalReference> getAllFiles() {
        return allFiles;
    }

    public void setAllFiles(List<TargetExternalReference> allFiles) {
        this.allFiles = allFiles;
    }

    public List<Value<String>> getModelFormat() {
        return modelFormat;
    }

    public void setModelFormat(List<String> modelFormat) {
        this.modelFormat = modelFormat.isEmpty() ? null : modelFormat.stream().map(Value::new).collect(Collectors.toList());
    }

    public Value<String> getDescription() {
        return description;
    }

    public void setDescription(Value<String> description) {
        this.description = description;
    }

    public TargetExternalReference getLicenseInfo() {
        return licenseInfo;
    }

    public void setLicenseInfo(TargetExternalReference licenseInfo) {
        this.licenseInfo = licenseInfo;
    }

    public List<TargetInternalReference> getOwners() {
        return owners;
    }

    public void setOwners(List<TargetInternalReference> owners) {
        this.owners = owners;
    }

    public List<Value<String>> getAbstractionLevel() {
        return abstractionLevel;
    }

    public void setAbstractionLevel(List<String> abstractionLevel) {
        this.abstractionLevel = abstractionLevel.isEmpty() ? null : abstractionLevel.stream().map(Value::new).collect(Collectors.toList()); // TODO: Remove null values
    }

    public List<TargetInternalReference> getMainContact() {
        return mainContact;
    }

    public void setMainContact(List<TargetInternalReference> mainContact) {
        this.mainContact = mainContact.isEmpty() ? null : mainContact;
    }

    public List<Value<String>> getBrainStructures() {
        return brainStructures;
    }

    public void setBrainStructures(List<String> brainStructures) {
        this.brainStructures = brainStructures.isEmpty() ? null : brainStructures.stream().map(Value::new).collect(Collectors.toList());
    }

    public List<TargetInternalReference> getUsedDataset() {
        return usedDataset;
    }

    public void setUsedDataset(List<TargetInternalReference> usedDataset) {
        this.usedDataset = usedDataset.isEmpty() ? null : usedDataset;
    }

    public Value<String> getVersion() {
        return version;
    }

    public void setVersion(Value<String> version) {
        this.version = version;
    }

    public List<String> getPublications() {
        return publications;
    }

    public void setPublications(List<String> publications) {
        this.publications = publications.isEmpty() ? null : publications;
    }

    public List<Value<String>> getStudyTarget() {
        return studyTarget;
    }

    public void setStudyTarget(List<String> studyTarget) {
        this.studyTarget = studyTarget.isEmpty() ? null : studyTarget.stream().map(Value::new).collect(Collectors.toList());
    }

    public List<Value<String>> getModelScope() {
        return modelScope;
    }

    public void setModelScope(List<String> modelScope) {
        this.modelScope = modelScope.isEmpty() ? null : modelScope.stream().map(Value::new).collect(Collectors.toList());
    }

    public Value<String> getTitle() {
        return title;
    }

    public void setTitle(Value<String> title) {
        this.title = title;
    }

    public List<TargetInternalReference> getContributors() {
        return contributors;
    }

    public void setContributors(List<TargetInternalReference> contributors) {
        this.contributors = contributors;
    }

    public List<Value<String>> getCellularTarget() {
        return cellularTarget;
    }

    public void setCellularTarget(List<String> cellularTarget) {
        this.cellularTarget = cellularTarget.isEmpty() ? null : cellularTarget.stream().map(Value::new).collect(Collectors.toList());
        ;
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

    public List<TargetFile> getFiles() {
        return files;
    }

    public void setFiles(List<TargetFile> files) {
        this.files = files;
    }

}
