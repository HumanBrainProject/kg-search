package eu.ebrains.kg.search.model.target.elasticsearch;

import java.util.ArrayList;
import java.util.List;

public class TargetInstances {
    private List<TargetInstance> searchableInstances;
    private List<TargetInstance> nonSearchableInstances;

    public TargetInstances() {
        this.searchableInstances = new ArrayList<>();
        this.nonSearchableInstances = new ArrayList<>();
    }

    public List<TargetInstance> getSearchableInstances() { return searchableInstances; }

    public void setSearchableInstances(List<TargetInstance> searchableInstances) {
        this.searchableInstances = searchableInstances;
    }

    public List<TargetInstance> getNonSearchableInstances() { return nonSearchableInstances; }

    public void setNonSearchableInstances(List<TargetInstance> nonSearchableInstances) { this.nonSearchableInstances = nonSearchableInstances; }

    public void addInstance(TargetInstance instance, boolean searchable) {
        if(searchable) {
            this.searchableInstances.add(instance);
        } else {
            this.nonSearchableInstances.add(instance);
        }
    }

}
