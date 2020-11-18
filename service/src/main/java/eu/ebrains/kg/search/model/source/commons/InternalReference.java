package eu.ebrains.kg.search.model.source.commons;

import java.util.List;

public class InternalReference {

    public InternalReference() {}

    public InternalReference(String identifier, String name, String relativeUrl) {
        this.identifier = identifier;
        this.name = name;
        this.relativeUrl = relativeUrl;
    }

    public InternalReference(String identifier, String name, String relativeUrl, List<String> datasetComponent) {
        this.identifier = identifier;
        this.name = name;
        this.relativeUrl = relativeUrl;
        this.datasetComponent = datasetComponent;
    }

    private String identifier;
    private String name;
    private String relativeUrl;
    private List<String> datasetComponent;

    public String getRelativeUrl() {
        return relativeUrl;
    }

    public void setRelativeUrl(String relativeUrl) {
        this.relativeUrl = relativeUrl;
    }

    public List<String> getDatasetComponent() {
        return datasetComponent;
    }

    public void setDatasetComponent(List<String> datasetComponent) {
        this.datasetComponent = datasetComponent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}