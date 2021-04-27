package eu.ebrains.kg.search.model.source;

public class SourceInstanceIdentifierV1andV2 implements SourceInstance {
    private String id;
    private String identifier;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
