package eu.ebrains.kg.search.model.source;

import java.util.Date;
import java.util.List;

import eu.ebrains.kg.search.model.source.commons.Publication;
import eu.ebrains.kg.search.model.source.commons.SourceInternalReference;

public class PersonV1andV2 implements SourceInstance {

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

    public static class Contribution extends SourceInternalReference {
    }

    public static class CustodianOf extends SourceInternalReference {
    }

}
