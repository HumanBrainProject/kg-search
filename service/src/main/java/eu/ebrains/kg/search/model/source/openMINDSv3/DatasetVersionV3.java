package eu.ebrains.kg.search.model.source.openMINDSv3;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.constants.EBRAINSVocab;
import eu.ebrains.kg.search.model.source.SourceInstance;

import java.util.Date;
import java.util.List;

public class DatasetVersionV3 implements SourceInstance {
    @JsonProperty(EBRAINSVocab.OPENMINDS_DESCRIPTION)
    private String description;

//    @JsonProperty(EBRAINSVocab.OPENMINDS_DIGITAL_IDENTIFIER)
//    private List<DigitalIdentifierV3> digitalIdentifier;

    @JsonProperty(EBRAINSVocab.OPENMINDS_FULL_NAME)
    private String fullName;

    @JsonProperty(EBRAINSVocab.OPENMINDS_HOMEPAGE)
    private String homepage;

    @JsonProperty(EBRAINSVocab.OPENMINDS_KEYWORD)
    private List<String> keyword;

    @JsonProperty(EBRAINSVocab.OPENMINDS_RELEASE_DATE)
    private Date releaseDate;

    @JsonProperty(EBRAINSVocab.OPENMINDS_SHORT_NAME)
    private String shortName;

    @JsonProperty(EBRAINSVocab.OPENMINDS_VERSION_IDENTIFIER)
    private String versionIdentifier;

    @JsonProperty(EBRAINSVocab.OPENMINDS_VERSION_INNOVATION)
    private String versionInnovation;

    @JsonProperty(EBRAINSVocab.OPENMINDS_PREVIOUS_VERSION_IDENTIFIER)
    private String previousVersionIdentifier;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    public List<DigitalIdentifierV3> getDigitalIdentifier() {
//        return digitalIdentifier;
//    }
//
//    public void setDigitalIdentifier(List<DigitalIdentifierV3> digitalIdentifier) {
//        this.digitalIdentifier = digitalIdentifier;
//    }

    public String getFullName() {
        return fullName;
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

    public List<String> getKeyword() {
        return keyword;
    }

    public void setKeyword(List<String> keyword) {
        this.keyword = keyword;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getVersionIdentifier() {
        return versionIdentifier;
    }

    public void setVersionIdentifier(String versionIdentifier) {
        this.versionIdentifier = versionIdentifier;
    }

    public String getVersionInnovation() {
        return versionInnovation;
    }

    public void setVersionInnovation(String versionInnovation) {
        this.versionInnovation = versionInnovation;
    }

    public String getPreviousVersionIdentifier() {
        return previousVersionIdentifier;
    }

    public void setPreviousVersionIdentifier(String previousVersionIdentifier) {
        this.previousVersionIdentifier = previousVersionIdentifier;
    }
}


