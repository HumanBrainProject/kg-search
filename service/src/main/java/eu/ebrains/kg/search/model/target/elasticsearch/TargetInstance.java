package eu.ebrains.kg.search.model.target.elasticsearch;

import eu.ebrains.kg.search.model.target.elasticsearch.instances.Searchable;

import java.util.Arrays;
import java.util.List;

public interface TargetInstance {
    String getId();

    List<String> getIdentifier();

    static boolean isSearchable(Class<?> clazz) {
        return Arrays.stream(clazz.getInterfaces()).anyMatch(i -> i == Searchable.class);
    }
}
