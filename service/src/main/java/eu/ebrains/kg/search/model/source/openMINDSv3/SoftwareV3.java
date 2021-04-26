package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.SourceInstance;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Author;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Version;

import java.util.List;

public class SoftwareV3 extends SourceInstanceV3 {
    private String description;
    private List<String> digitalIdentifier;
    private String howToCite;
    private String title;
    private List<Author> developer;
    private List<Author> custodian;
    private List<Version> versions;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public List<Version> getVersions() {
        return versions;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }
}
