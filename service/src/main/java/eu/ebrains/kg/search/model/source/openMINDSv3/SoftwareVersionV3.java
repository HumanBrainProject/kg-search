package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.SourceInstance;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Author;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Versions;

import java.util.List;

public class SoftwareVersionV3 extends SourceInstanceV3 {
    private String version;
    private String digitalIdentifier;
    private String howToCite;
    private String description;
    private String title;
    private SoftwareVersions software;
    private List<String> applicationCategory;
    private List<String> operatingSystem;
    private String homepage;
    private List<Author> developer;
    private List<Author> custodian;
    private List<String> sourceCode;
    private List<String> documentation;
    private List<String> features;
    private List<String> license;

    public String getVersion() { return version; }

    public void setVersion(String version) { this.version = version; }

    public String getDigitalIdentifier() {
        return digitalIdentifier;
    }

    public void setDigitalIdentifier(String digitalIdentifier) {
        this.digitalIdentifier = digitalIdentifier;
    }

    public String getHowToCite() {
        return howToCite;
    }

    public void setHowToCite(String howToCite) {
        this.howToCite = howToCite;
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

    public SoftwareVersions getSoftware() {
        return software;
    }

    public void setSoftware(SoftwareVersions software) {
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

    public List<Author> getDeveloper() {
        return developer;
    }

    public void setDeveloper(List<Author> developer) {
        this.developer = developer;
    }

    public List<Author> getCustodian() {
        return custodian;
    }

    public void setCustodian(List<Author> custodian) {
        this.custodian = custodian;
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

    public static class SoftwareVersions extends Versions {

        private List<Author> custodian;
        private List<Author> developer;

        public List<Author> getCustodian() {
            return custodian;
        }

        public void setCustodian(List<Author> custodian) {
            this.custodian = custodian;
        }

        public List<Author> getDeveloper() {
            return developer;
        }

        public void setDeveloper(List<Author> developer) {
            this.developer = developer;
        }
    }
}
