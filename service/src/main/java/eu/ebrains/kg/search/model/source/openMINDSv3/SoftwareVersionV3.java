package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.SourceInstance;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Author;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Versions;

import java.util.List;

public class SoftwareVersionV3 implements SourceInstance {
    private String id;
    private List<String> identifier;
    private String version;
    private String projectDoi;
    private String citation;
    private String description;
    private String title;
    private Versions software;
    private List<String> applicationCategory;
    private List<String> operatingSystem;
    private String homepage;
    private List<Author> contributors;
    private List<String> sourceCode;
    private List<String> documentation;
    private List<String> features;
    private List<String> license;

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

    public String getVersion() { return version; }

    public void setVersion(String version) { this.version = version; }

    public String getProjectDoi() {
        return projectDoi;
    }

    public void setProjectDoi(String projectDoi) {
        this.projectDoi = projectDoi;
    }

    public String getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        this.citation = citation;
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

    public Versions getSoftware() {
        return software;
    }

    public void setSoftware(Versions software) {
        this.software = software;
    }

    public List<String> getApplicationCategory() {
        return applicationCategory;
    }

    public void setApplicationCategory(List<String> applicationCategory) {
        this.applicationCategory = applicationCategory;
    }

    public List<String> getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(List<String> operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public List<Author> getContributors() {
        return contributors;
    }

    public void setContributors(List<Author> contributors) {
        this.contributors = contributors;
    }

    public List<String> getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(List<String> sourceCode) {
        this.sourceCode = sourceCode;
    }

    public List<String> getDocumentation() {
        return documentation;
    }

    public void setDocumentation(List<String> documentation) {
        this.documentation = documentation;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public List<String> getLicense() {
        return license;
    }

    public void setLicense(List<String> license) {
        this.license = license;
    }
}
