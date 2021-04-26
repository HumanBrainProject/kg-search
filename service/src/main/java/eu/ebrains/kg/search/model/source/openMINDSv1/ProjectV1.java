package eu.ebrains.kg.search.model.source.openMINDSv1;

import eu.ebrains.kg.search.model.source.commonsV1andV2.Publication;
import eu.ebrains.kg.search.model.source.SourceInstanceV1andV2;
import eu.ebrains.kg.search.model.source.commonsV1andV2.SourceInternalReference;

import java.util.List;

public class ProjectV1 extends SourceInstanceV1andV2 {
    private String description;
    private List<SourceInternalReference> datasets;
    private String title;
    private List<Publication> publications;

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public List<SourceInternalReference> getDatasets() { return datasets; }

    public void setDatasets(List<SourceInternalReference> datasets) { this.datasets = datasets; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public List<Publication> getPublications() { return publications; }

    public void setPublications(List<Publication> publications) { this.publications = publications; }
}
