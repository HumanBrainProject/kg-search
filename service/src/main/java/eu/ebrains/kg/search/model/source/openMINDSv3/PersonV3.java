package eu.ebrains.kg.search.model.source.openMINDSv3;

import java.util.List;

public class PersonV3 extends SourceInstanceV3 {
    private String familyName;
    private String givenName;
    private List<CustodianOfModel> custodianOfModel;
    private List<CustodianOf> custodianOf;
    private List<RelatedPublication> publications;
    private List<ModelContribution> modelContributions;
    private List<Contribution> contributions;

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public List<CustodianOfModel> getCustodianOfModel() {
        return custodianOfModel;
    }

    public void setCustodianOfModel(List<CustodianOfModel> custodianOfModel) {
        this.custodianOfModel = custodianOfModel;
    }

    public List<CustodianOf> getCustodianOf() {
        return custodianOf;
    }

    public void setCustodianOf(List<CustodianOf> custodianOf) {
        this.custodianOf = custodianOf;
    }

    public List<RelatedPublication> getPublications() {
        return publications;
    }

    public void setPublications(List<RelatedPublication> publications) {
        this.publications = publications;
    }

    public List<ModelContribution> getModelContributions() {
        return modelContributions;
    }

    public void setModelContributions(List<ModelContribution> modelContributions) {
        this.modelContributions = modelContributions;
    }

    public List<Contribution> getContributions() {
        return contributions;
    }

    public void setContributions(List<Contribution> contributions) {
        this.contributions = contributions;
    }

    public static class CustodianOfModel {
        private String id;
        private List<String> identifier;
        private String name;
        private List<String> hasComponent;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<String> getIdentifier() {
            return identifier;
        }

        public void setIdentifier(List<String> identifier) {
            this.identifier = identifier;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getHasComponent() {
            return hasComponent;
        }

        public void setHasComponent(List<String> hasComponent) {
            this.hasComponent = hasComponent;
        }
    }

    public static class CustodianOf {
        private String id;
        private List<String> identifier;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<String> getIdentifier() {
            return identifier;
        }

        public void setIdentifier(List<String> identifier) {
            this.identifier = identifier;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class RelatedPublication {
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

    public static class ModelContribution {
        private String id;
        private List<String> identifier;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<String> getIdentifier() {
            return identifier;
        }

        public void setIdentifier(List<String> identifier) {
            this.identifier = identifier;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Contribution {
        private String id;
        private List<String> identifier;
        private String name;
        List<String> datasetComponent;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<String> getIdentifier() {
            return identifier;
        }

        public void setIdentifier(List<String> identifier) {
            this.identifier = identifier;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getDatasetComponent() {
            return datasetComponent;
        }

        public void setDatasetComponent(List<String> datasetComponent) {
            this.datasetComponent = datasetComponent;
        }
    }
}
