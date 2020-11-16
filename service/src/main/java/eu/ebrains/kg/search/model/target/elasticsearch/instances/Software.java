package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;

import java.util.Date;
import java.util.List;

public class Software {

    private List<Value<String>> appCategory;

    private Value<String> identifier;

    private Value<String> description;

    private List<ExternalReference> sourceCode;

    private List<Value<String>> features;

    private List<ExternalReference> documentation;

    private List<Value<String>> license;

    private List<Value<String>> operatingSystem;

    private List<Value<String>> version;

    private List<ExternalReference> homepage;

    private Value<String> title;

    @JsonProperty("first_release")
    private Value<Date> firstRelease;

    @JsonProperty("last_release")
    private Value<Date> lastRelease;

    private Value<String> type;

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
