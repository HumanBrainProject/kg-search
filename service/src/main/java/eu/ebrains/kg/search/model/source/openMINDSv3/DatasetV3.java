package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.SourceInstance;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Author;
import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Component;

import java.util.*;
import java.util.stream.Collectors;

public class DatasetV3 implements SourceInstance {
    private String id;
    private List<String> identifier;
    private String description;
    private List<DigitalIdentifierV3> digitalIdentifier;
    private String fullName;
    private String homepage;
    private String shortName;
    private List<DatasetVersionV3> datasetVersions;
    private List<Author> authors;
    private List<Component> components;

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
        List<DatasetVersionV3> datasetVersionsWithoutVersion = new ArrayList<>();
        Map<String, DatasetVersionV3> lookup = new HashMap<>();
        datasetVersions.forEach(dv -> {
            String id = dv.getVersionIdentifier();
            if (id != null) {
                lookup.put(id, dv);
                if (result.isEmpty()) {
                    result.add(id);
                } else {
                    String previousVersionIdentifier = dv.getPreviousVersionIdentifier();
                    if (previousVersionIdentifier != null) {
                        int i = result.indexOf(previousVersionIdentifier);
                        if (i == -1) {
                            result.addLast(id);
                        } else {
                            result.add(i, id);
                        }
                    } else {
                        result.addFirst(id);
                    }
                }
            } else {
                datasetVersionsWithoutVersion.add(dv);
            }
        });
        this.datasetVersions = result.stream().map(lookup::get).collect(Collectors.toList());
        this.datasetVersions.addAll(datasetVersionsWithoutVersion);
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

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

}
