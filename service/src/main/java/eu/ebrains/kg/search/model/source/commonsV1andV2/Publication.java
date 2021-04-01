package eu.ebrains.kg.search.model.source.commonsV1andV2;

public class Publication {

    public Publication() {}

    public Publication(String citation, String doi) {
        this.citation = citation;
        this.doi = doi;
    }

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
