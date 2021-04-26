package eu.ebrains.kg.search.controller.translators;

import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;

import java.util.List;

public class TargetInstancesResult {
    private List<TargetInstance> targetInstances;
    private int from = 0;
    private int size = 0;
    private int total = 0;

    public List<TargetInstance> getTargetInstances() {
        return targetInstances;
    }

    public void setTargetInstances(List<TargetInstance> targetInstances) {
        this.targetInstances = targetInstances;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
