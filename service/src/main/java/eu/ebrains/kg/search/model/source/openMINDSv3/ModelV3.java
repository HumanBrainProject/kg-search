package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.SourceInstance;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Author;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;

import java.util.List;

public class ModelV3 implements SourceInstance {
    private String id;
    private List<String> identifier;
    private String description;
    private List<Author> contributors;
    private List<Author> custodian;
    private String title;
    private List<Version> versions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Version> getVersions() {
        return versions;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }

    public List<Author> getContributors() { return contributors; }

    public void setContributors(List<Author> contributors) { this.contributors = contributors; }

    public List<Author> getCustodian() { return custodian; }

    public void setCustodian(List<Author> custodian) { this.custodian = custodian; }
}