package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.ExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;

import java.util.Date;
import java.util.List;

@MetaInfo(name="Software", identifier = "softwarecatalog/software/softwareproject/v1.0.0/search", order=6)
public class Software {

    private Value<String> type = new Value<>("Software");

    @FieldInfo(layout = FieldInfo.Layout.HEADER)
    private String editorId;

    @FieldInfo(label = "Application Category", layout = FieldInfo.Layout.SUMMARY, separator = ", ")
    private List<Value<String>> appCategory;

    @FieldInfo(visible = false, ignoreForSearch = true)
    private Value<String> identifier;

    @FieldInfo(label = "Name", boost = 20, sort = true, optional = false)
    private Value<String> title;

    @FieldInfo(labelHidden = true, markdown = true, boost = 2)
    private Value<String> description;

    @FieldInfo(label = "Source code", layout = FieldInfo.Layout.SUMMARY)
    private List<ExternalReference> sourceCode;

    @FieldInfo(label = "Features", layout = FieldInfo.Layout.SUMMARY, tagIcon = "<svg width=\"50\" height=\"50\" viewBox=\"0 0 1792 1792\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M576 448q0-53-37.5-90.5t-90.5-37.5-90.5 37.5-37.5 90.5 37.5 90.5 90.5 37.5 90.5-37.5 37.5-90.5zm1067 576q0 53-37 90l-491 492q-39 37-91 37-53 0-90-37l-715-716q-38-37-64.5-101t-26.5-117v-416q0-52 38-90t90-38h416q53 0 117 26.5t102 64.5l715 714q37 39 37 91z\"/></svg>")
    private List<Value<String>> features;

    @FieldInfo(label = "Documentation", layout = FieldInfo.Layout.SUMMARY)
    private List<ExternalReference> documentation;

    @FieldInfo(label = "License")
    private List<Value<String>> license;

    @FieldInfo(label = "Operating System", layout = FieldInfo.Layout.SUMMARY, facet = FieldInfo.Facet.LIST, tagIcon = "<svg width=\"50\" height=\"50\" viewBox=\"0 0 11.377083 13.05244\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M 5.6585847,-3.1036376e-7 2.8334327,1.5730297 0.0088,3.1455497 0.0047,6.4719597 0,9.7983697 2.8323857,11.42515 l 2.831867,1.62729 1.070218,-0.60358 c 0.588756,-0.33201 1.874409,-1.06813 2.856675,-1.63608 L 11.377083,9.7797697 v -3.24735 -3.24786 l -0.992187,-0.62477 C 9.8391917,2.3160397 8.5525477,1.5769697 7.5256387,1.0175097 Z M 5.6580697,3.7398297 a 2.7061041,2.7144562 0 0 1 2.706293,2.71456 2.7061041,2.7144562 0 0 1 -2.706293,2.71456 2.7061041,2.7144562 0 0 1 -2.70578,-2.71456 2.7061041,2.7144562 0 0 1 2.70578,-2.71456 z\"/></svg>")
    private List<Value<String>> operatingSystem;

    @FieldInfo(label = "Latest Version", layout = FieldInfo.Layout.SUMMARY)
    private List<Value<String>> version;

    @FieldInfo(label = "Homepage", layout = FieldInfo.Layout.SUMMARY)
    private List<ExternalReference> homepage;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private Value<Date> firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private Value<Date> lastRelease;

    public String getEditorId() {
        return editorId;
    }

    public void setEditorId(String editorId) {
        this.editorId = editorId;
    }

    public List<Value<String>> getAppCategory() {
        return appCategory;
    }

    public void setAppCategory(List<Value<String>> appCategory) {
        this.appCategory = appCategory;
    }

    public Value<String> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Value<String> identifier) {
        this.identifier = identifier;
    }

    public Value<String> getDescription() {
        return description;
    }

    public void setDescription(Value<String> description) {
        this.description = description;
    }

    public List<ExternalReference> getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(List<ExternalReference> sourceCode) {
        this.sourceCode = sourceCode;
    }

    public List<Value<String>> getFeatures() {
        return features;
    }

    public void setFeatures(List<Value<String>> features) {
        this.features = features;
    }

    public List<ExternalReference> getDocumentation() {
        return documentation;
    }

    public void setDocumentation(List<ExternalReference> documentation) {
        this.documentation = documentation;
    }

    public List<Value<String>> getLicense() {
        return license;
    }

    public void setLicense(List<Value<String>> license) {
        this.license = license;
    }

    public List<Value<String>> getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(List<Value<String>> operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public List<Value<String>> getVersion() {
        return version;
    }

    public void setVersion(List<Value<String>> version) {
        this.version = version;
    }

    public List<ExternalReference> getHomepage() {
        return homepage;
    }

    public void setHomepage(List<ExternalReference> homepage) {
        this.homepage = homepage;
    }

    public Value<String> getTitle() {
        return title;
    }

    public void setTitle(Value<String> title) {
        this.title = title;
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
