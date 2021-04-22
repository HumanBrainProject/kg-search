package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.SourceInstance;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Author;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Component;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Versions;

import java.util.Date;
import java.util.List;

public class DatasetVersionV3 implements SourceInstance {
    private String id;
    private List<String> identifier;
    private List<DigitalIdentifierV3> digitalIdentifier;
    private String description;
    private String fullName;
    private String homepage;
    private List<String> keyword;
    private Date releaseDate;
    private String shortName;
    private String version;
    private String versionInnovation;
    private List<Author> authors;
    private List<Component> components;
    private Versions dataset;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getIdentifier() { return identifier; }

    public void setIdentifier(List<String> identifier) { this.identifier = identifier; }

    public List<DigitalIdentifierV3> getDigitalIdentifier() {
        return digitalIdentifier;
    }

    public void setDigitalIdentifier(List<DigitalIdentifierV3> digitalIdentifier) {
        this.digitalIdentifier = digitalIdentifier;
    }

    public String getFullName() {
        return fullName;
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

    public List<String> getKeyword() {
        return keyword;
    }

    public void setKeyword(List<String> keyword) {
        this.keyword = keyword;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionInnovation() {
        return versionInnovation;
    }

    public void setVersionInnovation(String versionInnovation) {
        this.versionInnovation = versionInnovation;
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

    public Versions getDataset() {
        return dataset;
    }

    public void setDataset(Versions dataset) {
        this.dataset = dataset;
    }

}


