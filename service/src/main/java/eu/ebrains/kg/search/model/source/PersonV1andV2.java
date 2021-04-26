package eu.ebrains.kg.search.model.source;

import eu.ebrains.kg.search.model.source.commonsV1andV2.Publication;
import eu.ebrains.kg.search.model.source.commonsV1andV2.SourceInternalReference;

import java.util.List;

public class PersonV1andV2 extends SourceInstanceV1andV2 {
    private String title;
    private List<CustodianOf> custodianOf;
    private List<CustodianOf> custodianOfModel;
    private List<Contribution> modelContributions;
    private List<Contribution> contributions;
    private List<Publication> publications;

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

    public static class Contribution extends SourceInternalReference {}

    public static class CustodianOf extends SourceInternalReference {}

}
