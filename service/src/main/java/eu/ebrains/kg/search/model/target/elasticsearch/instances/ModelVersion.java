package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@MetaInfo(name = "Model", identifier = "uniminds/core/modelinstance/v1.0.0/search", order = 5)
public class ModelVersion implements TargetInstance, Searchable {
    @ElasticSearchInfo(type = "keyword")
    private Value<String> type = new Value<>("Model");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @FieldInfo(visible = false, ignoreForSearch = true)
    private List<String> identifier;

    @FieldInfo(layout = FieldInfo.Layout.HEADER)
    private Value<String> editorId;

    @FieldInfo(label = "Name", sort = true, boost = 20)
    private Value<String> title;

    @FieldInfo(label = "Description", markdown = true, boost = 2, labelHidden = true)
    private Value<String> description;

    @FieldInfo(label = "Version", layout = FieldInfo.Layout.SUMMARY)
    private Value<String> version;

    @FieldInfo(label = "Model Versions")
    private TargetInternalReference model;

    @FieldInfo(label = "Contributors", layout = FieldInfo.Layout.HEADER, separator = "; ", type = FieldInfo.Type.TEXT, labelHidden = true, boost = 10)
    private List<TargetInternalReference> contributors;

    @FieldInfo(label = "Custodian", layout = FieldInfo.Layout.SUMMARY, separator = "; ", type = FieldInfo.Type.TEXT, hint = "A custodian is the person responsible for the data bundle.")
    private List<TargetInternalReference> owners;

    @FieldInfo(label = "Main contact", layout = FieldInfo.Layout.SUMMARY, separator = "; ", type = FieldInfo.Type.TEXT)
    private List<TargetInternalReference> mainContact;

    @FieldInfo(label = "Files", layout = FieldInfo.Layout.GROUP, markdown = true)
    private Value<String> embargo;

    @JsonProperty("allfiles") //TODO: capitalize
    @FieldInfo(label = "Download model", isButton = true, termsOfUse = true, icon="download")
    private List<TargetExternalReference> allFiles;

    @FieldInfo(label = "Publications", layout = FieldInfo.Layout.GROUP, markdown = true, hint = "List of publications that have been published as a part of this model.")
    private List<Value<String>> publications;

    @FieldInfo(label = "Brain structure", layout = FieldInfo.Layout.SUMMARY, facet = FieldInfo.Facet.LIST)
    private List<Value<String>> brainStructures;

    @FieldInfo(label = "(Sub)cellular target", layout = FieldInfo.Layout.SUMMARY)
    private List<Value<String>> cellularTarget;

    @FieldInfo(label = "Study target", layout = FieldInfo.Layout.SUMMARY)
    private List<Value<String>> studyTarget;

    @FieldInfo(label = "Model scope", layout = FieldInfo.Layout.SUMMARY, facet = FieldInfo.Facet.LIST)
    private List<Value<String>> modelScope;

    @FieldInfo(label = "Abstraction level", layout = FieldInfo.Layout.SUMMARY, separator = "; ", facet = FieldInfo.Facet.LIST)
    private List<Value<String>> abstractionLevel;

    @FieldInfo(label = "Model format", layout = FieldInfo.Layout.SUMMARY, separator = "; ")
    private List<Value<String>> modelFormat;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    @FieldInfo(label = "Used datasets", layout = FieldInfo.Layout.GROUP)
    private List<TargetInternalReference> usedDataset;

    @FieldInfo(label = "Produced datasets", layout = FieldInfo.Layout.GROUP)
    private List<TargetInternalReference> producedDataset;

    @JsonProperty("license_info")
    @FieldInfo(label = "License", type = FieldInfo.Type.TEXT, facetOrder = FieldInfo.FacetOrder.BYVALUE)
    private TargetExternalReference licenseInfo;

    private List<TargetInternalReference> versions;

    @Override
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public void setType(String type) {
        setType(StringUtils.isBlank(type) ? null : new Value<>(type));
    }

    public void setIdentifier(List<String> identifier) {
        this.identifier = identifier;
    }

    public void setEditorId(String editorId) {
        setEditorId(StringUtils.isBlank(editorId) ? null : new Value<>(editorId));
    }

    public TargetInternalReference getModel() { return model; }

    public void setModel(TargetInternalReference model) { this.model = model; }

    public void setEmbargo(String embargo) {
        setEmbargo(StringUtils.isBlank(embargo) ? null : new Value<>(embargo));
    }

    public void setDescription(String description) {
        setDescription(StringUtils.isBlank(description) ? null : new Value<>(description));
    }

    public void setVersion(String version) {
        setVersion(StringUtils.isBlank(version) ? null : new Value<>(version));
    }

    public void setTitle(String title) {
        setTitle(StringUtils.isBlank(title) ? null : new Value<>(title));
    }

    public ISODateValue getFirstRelease() {
        return firstRelease;
    }

    public void setFirstRelease(ISODateValue firstRelease) {
        this.firstRelease = firstRelease;
    }

    public void setFirstRelease(Date firstRelease) {
        this.setFirstRelease(firstRelease != null ? new ISODateValue(firstRelease) : null);
    }

    public ISODateValue getLastRelease() {
        return lastRelease;
    }

    public void setLastRelease(ISODateValue lastRelease) {
        this.lastRelease = lastRelease;
    }

    public void setLastRelease(Date lastRelease) {
        this.setLastRelease(lastRelease != null ? new ISODateValue(lastRelease) : null);
    }

    @Override
    public List<String> getIdentifier() {
        return identifier;
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
        this.producedDataset = producedDataset;
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
        this.modelFormat = modelFormat == null ? null : modelFormat.stream().filter(StringUtils::isNotBlank).map(Value::new).collect(Collectors.toList());
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
        this.abstractionLevel = abstractionLevel == null ? null : abstractionLevel.stream().map(Value::new).collect(Collectors.toList());
    }

    public List<TargetInternalReference> getMainContact() {
        return mainContact;
    }

    public void setMainContact(List<TargetInternalReference> mainContact) {
        this.mainContact = mainContact;
    }

    public List<Value<String>> getBrainStructures() {
        return brainStructures;
    }

    public void setBrainStructures(List<String> brainStructures) {
        this.brainStructures = brainStructures == null ? null : brainStructures.stream().map(Value::new).collect(Collectors.toList());
    }

    public List<TargetInternalReference> getUsedDataset() {
        return usedDataset;
    }

    public void setUsedDataset(List<TargetInternalReference> usedDataset) {
        this.usedDataset = usedDataset;
    }

    public Value<String> getVersion() {
        return version;
    }

    public void setVersion(Value<String> version) {
        this.version = version;
    }

    public List<Value<String>> getPublications() {
        return publications;
    }

    public void setPublications(List<String> publications) {
        this.publications = publications == null ? null : publications.stream().map(Value::new).collect(Collectors.toList());
    }

    public List<Value<String>> getStudyTarget() {
        return studyTarget;
    }

    public void setStudyTarget(List<String> studyTarget) {
        this.studyTarget = studyTarget == null ? null : studyTarget.stream().map(Value::new).collect(Collectors.toList());
    }

    public List<Value<String>> getModelScope() {
        return modelScope;
    }

    public void setModelScope(List<String> modelScope) {
        this.modelScope = modelScope == null ? null : modelScope.stream().map(Value::new).collect(Collectors.toList());
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
        this.cellularTarget = cellularTarget == null ? null : cellularTarget.stream().filter(StringUtils::isNotBlank).map(Value::new).collect(Collectors.toList());
    }

    public Value<String> getType() {
        return type;
    }

    public void setType(Value<String> type) {
        this.type = type;
    }

    public List<TargetInternalReference> getVersions() { return versions; }

    public void setVersions(List<TargetInternalReference> versions) { this.versions = versions; }
}
