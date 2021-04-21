package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.SourceInstance;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;

import java.util.List;

public class SoftwareV3 implements SourceInstance {
    private String id;
    private List<String> identifier;
    private String description;
    private List<DigitalIdentifierV3> projectDoi;
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

    public List<DigitalIdentifierV3> getProjectDoi() {
        return projectDoi;
    }

    public void setProjectDoi(List<DigitalIdentifierV3> projectDoi) {
        this.projectDoi = projectDoi;
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
}
