package eu.ebrains.kg.common.model.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeywordsBucket extends Bucket {

    private Reverse reverse;
    private KeywordsAgg keywords;

    @Getter
    @Setter
    public static class Reverse {

        @JsonProperty("doc_count")
        private int docCount;
    }
}
