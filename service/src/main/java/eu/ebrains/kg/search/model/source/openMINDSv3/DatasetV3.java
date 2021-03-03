package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.SourceInstance;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DatasetV3 implements SourceInstance {
    private String id;
    private List<String> identifier;
    private String description;
    private List<DigitalIdentifierV3> digitalIdentifier;
    private String fullName;
    private List<DatasetVersionV3> datasetVersions;
    private String homepage;
    private String shortName;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

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

    public List<DigitalIdentifierV3> getDigitalIdentifier() {
        return digitalIdentifier;
    }

    public void setDigitalIdentifier(List<DigitalIdentifierV3> digitalIdentifier) {
        this.digitalIdentifier = digitalIdentifier;
    }

    public String getFullName() {
        return fullName;
    }

    public List<DatasetVersionV3> getDatasetVersions() {
        return datasetVersions;
    }

    public void setDatasetVersions(List<DatasetVersionV3> datasetVersions) {
        LinkedList<String> result = new LinkedList<>();
        Map<String, DatasetVersionV3> lookup = new HashMap<>();
        datasetVersions.forEach(dv -> {
            lookup.put(dv.getVersionIdentifier(), dv);
            if(result.isEmpty()) {
                result.add(dv.getVersionIdentifier());
            } else {
                String previousVersionIdentifier = dv.getPreviousVersionIdentifier();
                if(previousVersionIdentifier != null) {
                    int i = result.indexOf(previousVersionIdentifier);
                    if(i == -1) {
                        result.addLast(dv.getVersionIdentifier());
                    } else {
                        result.add(i, dv.getVersionIdentifier());
                    }
                } else {
                    result.addFirst(dv.getVersionIdentifier());
                }
            }
        });
        this.datasetVersions = result.stream().map(lookup::get).collect(Collectors.toList());
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
