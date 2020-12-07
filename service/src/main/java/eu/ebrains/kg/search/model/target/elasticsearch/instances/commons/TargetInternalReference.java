package eu.ebrains.kg.search.model.target.elasticsearch.instances.commons;

import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchInfo;
import com.fasterxml.jackson.annotation.JsonInclude;

public class TargetInternalReference {

    public TargetInternalReference() {
    }

    public TargetInternalReference(String reference, String value) {
        this.reference = reference;
        this.value = value;
    }

    public TargetInternalReference(String reference, String value, String uuid) {
        this.reference = reference;
        this.value = value;
        this.uuid = uuid;
    }

    @ElasticSearchInfo(ignoreAbove = 256)
    private String reference;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String value;

    @ElasticSearchInfo(ignoreAbove = 256)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String uuid;

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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
