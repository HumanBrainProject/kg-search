package eu.ebrains.kg.search.model.source.openMINDSv2;

import eu.ebrains.kg.search.model.source.SourceInstance;

import java.util.Date;
import java.util.List;

public class SoftwareV2 implements SourceInstance {
    private Date firstReleaseAt;
    private String description;
    private Date lastReleaseAt;
    private List<Version> versions;
    private String title;
    private String identifier;
    private String editorId;

    public Date getFirstReleaseAt() { return firstReleaseAt; }

    public void setFirstReleaseAt(Date firstReleaseAt) { this.firstReleaseAt = firstReleaseAt; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public Date getLastReleaseAt() { return lastReleaseAt; }

    public void setLastReleaseAt(Date lastReleaseAt) { this.lastReleaseAt = lastReleaseAt; }

    public List<Version> getVersions() { return versions; }

    public void setVersions(List<Version> versions) { this.versions = versions; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getIdentifier() { return identifier; }

    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getEditorId() { return editorId; }

    public void setEditorId(String editorId) { this.editorId = editorId; }

    public static class Version {
        private List<String> applicationCategory;
        private List<String> documentation;
        private String description;
        private List<String> license;
        private List<String> features;
        private List<String> operatingSystem;
        private List<Contributor> contributors;
        private List<String> homepage;
        private List<String> sourceCode;
        private String version;

        public List<String> getApplicationCategory() { return applicationCategory; }

        public void setApplicationCategory(List<String> applicationCategory) { this.applicationCategory = applicationCategory; }

        public List<String> getDocumentation() { return documentation; }

        public void setDocumentation(List<String> documentation) { this.documentation = documentation; }

        public String getDescription() { return description; }

        public void setDescription(String description) { this.description = description; }

        public List<String> getLicense() { return license; }

        public void setLicense(List<String> license) { this.license = license; }

        public List<String> getFeatures() { return features; }

        public void setFeatures(List<String> features) { this.features = features; }

        public List<String> getOperatingSystem() { return operatingSystem; }

        public void setOperatingSystem(List<String> operatingSystem) { this.operatingSystem = operatingSystem; }

        public List<Contributor> getContributors() { return contributors; }

        public void setContributors(List<Contributor> contributors) { this.contributors = contributors; }

        public List<String> getHomepage() { return homepage; }

        public void setHomepage(List<String> homepage) { this.homepage = homepage; }

        public List<String> getSourceCode() { return sourceCode; }

        public void setSourceCode(List<String> sourceCode) { this.sourceCode = sourceCode; }

        public String getVersion() { return version; }

        public void setVersion(String version) { this.version = version; }
    }

    public static class Contributor {
        private String givenName;
        private String familyName;

        public String getGivenName() { return givenName; }

        public void setGivenName(String givenName) { this.givenName = givenName; }

        public String getFamilyName() { return familyName; }

        public void setFamilyName(String familyName) { this.familyName = familyName; }

    }
}
