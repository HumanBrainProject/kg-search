package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;

public interface HasCitation {

    void setCitation(Value<String> citation);

    void setDoi(Value<String> doi);

    void setCustomCitation(Value<String> customCitation);

}
