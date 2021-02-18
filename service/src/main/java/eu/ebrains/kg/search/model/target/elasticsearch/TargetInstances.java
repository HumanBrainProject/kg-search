package eu.ebrains.kg.search.model.target.elasticsearch;

import java.util.ArrayList;
import java.util.List;

public class TargetInstances {
    private List<TargetInstance> searchableInstances;
    private List<TargetInstance> allInstances;

    public TargetInstances() {
        this.searchableInstances = new ArrayList<>();
        this.allInstances = new ArrayList<>();
    }

    public List<TargetInstance> getSearchableInstances() { return searchableInstances; }

    public void setSearchableInstances(List<TargetInstance> searchableInstances) {
        this.searchableInstances = searchableInstances;
    }

    public List<TargetInstance> getAllInstances() { return allInstances; }

    public void setAllInstances(List<TargetInstance> allInstances) { this.allInstances = allInstances; }

    public void addInstance(TargetInstance instance, boolean searchable) {
        if(searchable) {
            this.searchableInstances.add(instance);
        }
        this.allInstances.add(instance);
    }
}
