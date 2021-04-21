package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.SourceInstance;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Author;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Component;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;

import java.util.*;

public class DatasetV3 implements SourceInstance {
    private String id;
    private List<String> identifier;
    private String description;
    private List<DigitalIdentifierV3> digitalIdentifier;
    private String fullName;
    private String homepage;
    private String shortName;
    private List<Version> datasetVersions;
    private List<Author> authors;
    private List<Component> components;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public List<String> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(List<String> identifier) {
        this.identifier = identifier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DigitalIdentifierV3> getDigitalIdentifier() {
        return digitalIdentifier;
    }

    public void setDigitalIdentifier(List<DigitalIdentifierV3> digitalIdentifier) {
        this.digitalIdentifier = digitalIdentifier;
    }

    public String getFullName() {
        return fullName;
    }

    public List<Version> getDatasetVersions() {
        return datasetVersions;
    }

    public void setDatasetVersions(List<Version> datasetVersions) {
        this.datasetVersions = datasetVersions;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

}
