package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;

import java.util.List;

@MetaInfo(name = "ModelVersions", identifier = "https://openminds.ebrains.eu/core/Model", order = 2)
public class ModelVersions implements TargetInstance {
    @ElasticSearchInfo(type = "keyword")
    private final Value<String> type = new Value<>("ModelVersions");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

    public Value<String> getType() {
        return type;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public List<String> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(List<String> identifier) {
        this.identifier = identifier;
    }
}
