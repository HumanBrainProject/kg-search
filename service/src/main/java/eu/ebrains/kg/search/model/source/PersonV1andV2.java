package eu.ebrains.kg.search.model.source;

import java.util.Date;
import java.util.List;

public class PersonV1andV2 {

    private String identifier;
    private String editorId;
    private String title;
    private List<CustodianOf> custodianOf;
    private List<CustodianOf> custodianOfModel;
    private List<Contribution> modelContributions;
    private List<Contribution> contributions;
    private List<Publication> publications;
    private Date firstReleaseAt;
    private Date lastReleaseAt;


    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }


    public String getEditorId() {
        return editorId;
    }

    public void setEditorId(String editorId) {
        this.editorId = editorId;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public List<CustodianOf> getCustodianOf() {
        return custodianOf;
    }

    public void setCustodianOf(List<CustodianOf> custodianOf) {
        this.custodianOf = custodianOf;
    }


    public List<CustodianOf> getCustodianOfModel() {
        return custodianOfModel;
    }

    public void setCustodianOfModel(List<CustodianOf> custodianOfModel) {
        this.custodianOfModel = custodianOfModel;
    }


    public List<Contribution> getModelContributions() {
        return modelContributions;
    }

    public void setModelContributions(List<Contribution> modelContributions) {
        this.modelContributions = modelContributions;
    }


    public List<Contribution> getContributions() {
        return contributions;
    }

    public void setContributions(List<Contribution> contributions) {
        this.contributions = contributions;
    }


    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }


    public Date getFirstReleaseAt() {
        return firstReleaseAt;
    }

    public void setFirstReleaseAt(Date firstReleaseAt) {
        this.firstReleaseAt = firstReleaseAt;
    }


    public Date getLastReleaseAt() {
        return lastReleaseAt;
    }

    public void setLastReleaseAt(Date lastReleaseAt) {
        this.lastReleaseAt = lastReleaseAt;
    }

    public static class Publication {
        private String citation;
        private String doi;

        public String getCitation() {
            return citation;
        }

        public void setCitation(String citation) {
            this.citation = citation;
        }

        public String getDoi() {
            return doi;
        }

        public void setDoi(String doi) {
            this.doi = doi;
        }
    }

    public static class Contribution extends Reference {
    }

    public static class CustodianOf extends Reference {
    }

    public static abstract class Reference {
        private String relativeUrl;
        private List<String> datasetComponent;
        private String name;
        private String identifier;

        public String getRelativeUrl() {
            return relativeUrl;
        }

        public void setRelativeUrl(String relativeUrl) {
            this.relativeUrl = relativeUrl;
        }

        public List<String> getDatasetComponent() {
            return datasetComponent;
        }

        public void setDatasetComponent(List<String> datasetComponent) {
            this.datasetComponent = datasetComponent;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }
    }


}
