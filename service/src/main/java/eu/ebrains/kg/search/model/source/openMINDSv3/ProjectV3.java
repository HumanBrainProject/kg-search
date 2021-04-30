package eu.ebrains.kg.search.model.source.openMINDSv3;

import eu.ebrains.kg.search.model.source.openMINDSv3.commons.Component;

import java.util.List;

public class ProjectV3 extends SourceInstanceV3 {
    private String title;
    private String description;
    private List<Publication> publications;
    private List<Component> datasets;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    public List<Component> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<Component> datasets) {
        this.datasets = datasets;
    }

    public static class Publication {
        private String digitalIdentifier;
        private String howToCite;

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
    }
}
