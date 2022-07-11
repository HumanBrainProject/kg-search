package eu.ebrains.kg.common.model.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Bucket {

    private String key;

    @JsonProperty("doc_count")
    private int docCount;
}
