package eu.ebrains.kg.search.model.target.elasticsearch.instances.commons;


import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Version {

    @FieldInfo(label = "Version", groupBy = true)
    private TargetInternalReference version;

    @FieldInfo(label = "Innovation", markdown = true)
    private Value<String> innovation;

}
