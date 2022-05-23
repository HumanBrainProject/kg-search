package eu.ebrains.kg.search.model.target.elasticsearch.instances.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BasicHierarchyElement {

    private String key;
    private String parentRelationType;
    private String title;
    private String color;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object data;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> legend;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<? extends BasicHierarchyElement> children;
}
