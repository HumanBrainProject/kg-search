package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.SourceInstance;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Author;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;

import java.util.List;

public class ModelV3 implements SourceInstance {
    private String id;
    private List<String> identifier;
    private String title;
    private String description;
    private List<String> digitalIdentifier;
    private String howToCite;
    private List<Author> developer;
    private List<Author> custodian;
    private List<String> studyTarget;
    private List<String> modelScope;
    private List<String> abstractionLevel;
    private String homepage;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getDigitalIdentifier() {
        return digitalIdentifier;
    }

    public void setDigitalIdentifier(List<String> digitalIdentifier) {
        this.digitalIdentifier = digitalIdentifier;
    }

    public String getHowToCite() {
        return howToCite;
    }

    public void setHowToCite(String howToCite) {
        this.howToCite = howToCite;
    }

    public List<Author> getDeveloper() { return developer; }

    public void setDeveloper(List<Author> developer) { this.developer = developer; }

    public List<Author> getCustodian() { return custodian; }

    public void setCustodian(List<Author> custodian) { this.custodian = custodian; }

    public List<String> getStudyTarget() {
        return studyTarget;
    }

    public void setStudyTarget(List<String> studyTarget) {
        this.studyTarget = studyTarget;
    }

    public List<String> getModelScope() {
        return modelScope;
    }

    public void setModelScope(List<String> modelScope) {
        this.modelScope = modelScope;
    }

    public List<String> getAbstractionLevel() {
        return abstractionLevel;
    }

    public void setAbstractionLevel(List<String> abstractionLevel) {
        this.abstractionLevel = abstractionLevel;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public List<Version> getVersions() {
        return versions;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }

}