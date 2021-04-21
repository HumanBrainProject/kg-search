package eu.ebrains.kg.search.model.source.openMINDSv3.commons;

public class Version {
    private String id;
    private String fullName;
    private String versionIdentifier;
    private String isNewVersionOf;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getVersionIdentifier() {
        return versionIdentifier;
    }

    public void setVersionIdentifier(String versionIdentifier) {
        this.versionIdentifier = versionIdentifier;
    }

    public String getIsNewVersionOf() { return isNewVersionOf; }

    public void setIsNewVersionOf(String isNewVersionOf) { this.isNewVersionOf = isNewVersionOf; }
}
