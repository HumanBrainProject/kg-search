package eu.ebrains.kg.search.model.source.openMINDSv3.commons;

import java.util.*;
import java.util.stream.Collectors;

public class InternalDatasetVersion {
    private String id;
    private String fullName;
    private String versionIdentifier;
    private String previousVersionIdentifier;

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

    public String getPreviousVersionIdentifier() {
        return previousVersionIdentifier;
    }

    public void setPreviousVersionIdentifier(String previousVersionIdentifier) {
        this.previousVersionIdentifier = previousVersionIdentifier;
    }
}
