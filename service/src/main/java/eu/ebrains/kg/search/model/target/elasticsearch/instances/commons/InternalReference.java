package eu.ebrains.kg.search.model.target.elasticsearch.instances.commons;

public class InternalReference {

    public InternalReference() {
    }

    public InternalReference(String reference, String value, String uuid) {
        this.reference = reference;
        this.value = value;
        this.uuid = uuid;
    }

    private String reference;
    private String value;
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