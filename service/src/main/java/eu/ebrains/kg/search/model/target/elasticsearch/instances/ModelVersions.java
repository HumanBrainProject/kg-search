package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;

import java.util.List;

@MetaInfo(name = "ModelVersions", identifier = "https://openminds.ebrains.eu/core/Model", order = 2)
public class ModelVersions implements TargetInstance {
    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

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
