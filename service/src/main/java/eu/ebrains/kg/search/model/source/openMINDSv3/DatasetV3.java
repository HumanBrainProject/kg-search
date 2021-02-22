package eu.ebrains.kg.search.model.source.openMINDSv3;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.constants.EBRAINSVocab;
import eu.ebrains.kg.search.model.source.SourceInstance;

import java.util.List;

public class DatasetV3 implements SourceInstance {
    @JsonProperty(EBRAINSVocab.OPENMINDS_ID)
    private String id;

    @JsonProperty(EBRAINSVocab.OPENMINDS_IDENTIFIER)
    private List<String> identifier;

    @JsonProperty(EBRAINSVocab.OPENMINDS_DESCRIPTION)
    private String description;

    @JsonProperty(EBRAINSVocab.OPENMINDS_DIGITAL_IDENTIFIER)
    private List<DigitalIdentifierV3> digitalIdentifier;

    @JsonProperty(EBRAINSVocab.OPENMINDS_FULL_NAME)
    private String fullName;

    @JsonProperty(EBRAINSVocab.OPENMINDS_DATASET_VERSIONS)
    private List<DatasetVersionV3> datasetVersions;

    @JsonProperty(EBRAINSVocab.OPENMINDS_HOMEPAGE)
    private String homepage;

    @JsonProperty(EBRAINSVocab.OPENMINDS_SHORT_NAME)
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
        this.datasetVersions = datasetVersions;
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
