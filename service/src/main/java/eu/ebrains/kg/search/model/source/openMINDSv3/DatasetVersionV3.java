package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.SourceInstance;

import java.util.Date;
import java.util.List;

public class DatasetVersionV3 implements SourceInstance {
    private String id;
    private String description;
    private String fullName;
    private String homepage;
    private List<String> keyword;
    private Date releaseDate;
    private String shortName;
    private String versionIdentifier;
    private String versionInnovation;
    private String previousVersionIdentifier;
    private List<String> identifier;

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

    public String getVersionIdentifier() {
        return versionIdentifier;
    }

    public void setVersionIdentifier(String versionIdentifier) {
        this.versionIdentifier = versionIdentifier;
    }

    public String getVersionInnovation() {
        return versionInnovation;
    }

    public void setVersionInnovation(String versionInnovation) {
        this.versionInnovation = versionInnovation;
    }

    public String getPreviousVersionIdentifier() {
        return previousVersionIdentifier;
    }

    public void setPreviousVersionIdentifier(String previousVersionIdentifier) {
        this.previousVersionIdentifier = previousVersionIdentifier;
    }
}


