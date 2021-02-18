package eu.ebrains.kg.search.model.source.openMINDSv3;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.constants.EBRAINSVocab;
import eu.ebrains.kg.search.model.source.SourceInstance;

public class DigitalIdentifierV3 implements SourceInstance {
    @JsonProperty(EBRAINSVocab.OPENMINDS_IDENTIFIER)
    private String identifier;

    @JsonProperty(EBRAINSVocab.OPENMINDS_HOW_TO_CITE)
    private String howToCite;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getHowToCite() {
        return howToCite;
    }

    public void setHowToCite(String howToCite) {
        this.howToCite = howToCite;
    }
}
