package eu.ebrains.kg.search.model.target.elasticsearch.instances.commons;

public class ExternalReference {

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