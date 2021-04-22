package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.SourceInstance;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Author;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Versions;

import java.util.List;

public class ModelVersionV3 implements SourceInstance {
    private String title;
    private List<String> identifier;
    private String id;
    private String version;
    private String description;
    private Author mainContact;
    private List<Author> custodian;
    private List<Author> contributors;
    private Versions model;
    private List<String> applicationCategory;
    private List<String> operatingSystem;
    private List<String> format;
    private List<String> embargo;
    private List<String> fileBundle;
    private List<License> license;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(List<String> identifier) {
        this.identifier = identifier;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Author getMainContact() {
        return mainContact;
    }

    public void setMainContact(Author mainContact) {
        this.mainContact = mainContact;
    }

    public List<Author> getCustodian() {
        return custodian;
    }

    public void setCustodian(List<Author> custodian) {
        this.custodian = custodian;
    }

    public List<Author> getContributors() {
        return contributors;
    }

    public void setContributors(List<Author> contributors) {
        this.contributors = contributors;
    }

    public Versions getModel() {
        return model;
    }

    public void setModel(Versions model) {
        this.model = model;
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

    public List<String> getFormat() {
        return format;
    }

    public void setFormat(List<String> format) {
        this.format = format;
    }

    public List<String> getEmbargo() {
        return embargo;
    }

    public void setEmbargo(List<String> embargo) {
        this.embargo = embargo;
    }

    public List<String> getFileBundle() {
        return fileBundle;
    }

    public void setFileBundle(List<String> fileBundle) {
        this.fileBundle = fileBundle;
    }

    public List<License> getLicense() {
        return license;
    }

    public void setLicense(List<License> license) {
        this.license = license;
    }

    private static class License {
        private String fullName;
        private String webpage;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getWebpage() {
            return webpage;
        }

        public void setWebpage(String webpage) {
            this.webpage = webpage;
        }
    }

}
