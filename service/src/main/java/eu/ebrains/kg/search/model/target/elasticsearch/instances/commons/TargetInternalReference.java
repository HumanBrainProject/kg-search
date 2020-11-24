package eu.ebrains.kg.search.model.target.elasticsearch.instances.commons;

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

    private String reference;

    private String value;

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