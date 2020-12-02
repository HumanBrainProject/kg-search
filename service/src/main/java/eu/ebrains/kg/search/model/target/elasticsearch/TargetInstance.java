package eu.ebrains.kg.search.model.target.elasticsearch;

import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;

public interface TargetInstance {

    Value<String> getIdentifier();
}
