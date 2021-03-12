package eu.ebrains.kg.search.model.source.commons;

import java.util.List;

public class SpecimenGroup {
    private List<Instance> instances;
    private List<String> componentName;

    public List<Instance> getInstances() {
        return instances;
    }

    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }

    public List<String> getComponentName() {
        return componentName;
    }

    public void setComponentName(List<String> componentName) {
        this.componentName = componentName;
    }

    public static class Instance extends SourceInternalReference {
    }

}