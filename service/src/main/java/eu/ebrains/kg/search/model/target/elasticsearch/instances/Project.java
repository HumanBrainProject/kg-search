package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.RibbonInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.InternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;

import java.util.Date;
import java.util.List;

@MetaInfo(name="Project", identifier = "uniminds/core/placomponent/v1.0.0/search", order=1)
@RibbonInfo(content="Datasets", aggregation="count", dataField="search:datasets", singular="dataset", plural="datasets")
public class Project {

    private Value<String> type = new Value<>("Project");

    @FieldInfo(visible = false)
    private Value<String> identifier;

    @FieldInfo(label = "Description", markdown =  true, boost =  7.5f, labelHidden = true, type=FieldInfo.Type.TEXT)
    private Value<String> description;

    private List<Value<String>> publications;
    private List<InternalReference> dataset;
    private Value<String> title;
    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private Value<Date> firstRelease;
    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private Value<Date> lastRelease;

    public Value<String> getType() {
        return type;
    }

    public void setType(Value<String> type) {
        this.type = type;
    }

    public Value<String> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Value<String> identifier) {
        this.identifier = identifier;
    }

    public Value<String> getDescription() {
        return description;
    }

    public void setDescription(Value<String> description) {
        this.description = description;
    }

    public List<Value<String>> getPublications() {
        return publications;
    }

    public void setPublications(List<Value<String>> publications) {
        this.publications = publications;
    }

    public List<InternalReference> getDataset() {
        return dataset;
    }

    public void setDataset(List<InternalReference> dataset) {
        this.dataset = dataset;
    }

    public Value<String> getTitle() {
        return title;
    }

    public void setTitle(Value<String> title) {
        this.title = title;
    }

    public Value<Date> getFirstRelease() {
        return firstRelease;
    }

    public void setFirstRelease(Value<Date> firstRelease) {
        this.firstRelease = firstRelease;
    }

    public Value<Date> getLastRelease() {
        return lastRelease;
    }

    public void setLastRelease(Value<Date> lastRelease) {
        this.lastRelease = lastRelease;
    }
}
