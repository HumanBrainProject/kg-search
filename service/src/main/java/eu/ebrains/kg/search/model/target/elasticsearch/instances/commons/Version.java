package eu.ebrains.kg.search.model.target.elasticsearch.instances.commons;


import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;

public class Version {
    public Version() {
    }

    @FieldInfo(label = "Version", groupBy = true)
    private TargetInternalReference version;

    @FieldInfo(label = "Innovation", markdown = true)
    private Value<String> innovation;


    public TargetInternalReference getVersion() {
        return version;
    }

    public void setVersion(TargetInternalReference version) {
        this.version = version;
    }

    public Value<String> getInnovation() {
        return innovation;
    }

    public void setInnovation(Value<String> innovation) {
        this.innovation = innovation;
    }
}
