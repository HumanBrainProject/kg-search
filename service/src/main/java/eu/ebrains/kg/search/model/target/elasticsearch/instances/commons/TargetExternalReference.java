package eu.ebrains.kg.search.model.target.elasticsearch.instances.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchInfo;

public class TargetExternalReference {

    public TargetExternalReference() {}

    public TargetExternalReference(String url, String value) {
        this.url = url;
        this.value = value;
    }

    @ElasticSearchInfo(ignoreAbove = 256)
    @JsonInclude(JsonInclude.Include.NON_NULL)
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