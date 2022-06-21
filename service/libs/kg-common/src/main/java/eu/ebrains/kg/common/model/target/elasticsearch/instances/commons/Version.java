package eu.ebrains.kg.common.model.target.elasticsearch.instances.commons;


import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Version {

    @FieldInfo(label = "Version")
    private TargetInternalReference version;

    @FieldInfo(label = "Innovation", markdown = true)
    private Value<String> innovation;

}
