package eu.ebrains.kg.common.model.target.elasticsearch.instances.schemaorg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SchemaOrgDataset implements SchemaOrgInstance {

    @JsonProperty("@context")
    private final String context = "https://schema.org/";

    @JsonProperty("@type")
    private final String type = "Dataset";

    private String name;
    private String description;
    private String url;
    private List<String> identifier;
    private List<String> keywords;
    private String license;
    private List<Creator> creator;
    private String version;

    public interface Creator{}


    @Getter
    @Setter
    public static class Organization implements Creator {
        @JsonProperty("@type")
        private final String type = "Organization";
        private String url;
        private String name;
    }


    @Getter
    @Setter
    public static class Person implements Creator {
        @JsonProperty("@type")
        private final String type = "Person";
        private String sameAs;
        private String givenName;
        private String familyName;
        private String name;
    }



}
