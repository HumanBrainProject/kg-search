package eu.ebrains.kg.search.model.target.elasticsearch;

import java.util.List;

public interface TargetInstance {
    String getId();

    List<String> getIdentifier();

    boolean isSearchable();
}
