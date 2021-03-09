package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.target.elasticsearch.*;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.ISODateValue;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@MetaInfo(name="Project", identifier = "minds/core/placomponent/v1.0.0/search", order=1)
@RibbonInfo(content="Datasets", aggregation="count", dataField="search:datasets", singular="dataset", plural="datasets", icon="<i class=\"fa fa-download\" aria-hidden=\"true\"></i>")
public class Project implements TargetInstance {
    @ElasticSearchInfo(type = "keyword")
    private Value<String> type = new Value<>("Project");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @FieldInfo(visible = false)
    private List<String> identifier;

    @FieldInfo(sort = true, label = "Name", boost = 20)
    private Value<String> title;

    @FieldInfo(layout=FieldInfo.Layout.HEADER)
    private Value<String> editorId;

    @FieldInfo(label = "Description", markdown =  true, boost =  7.5f, labelHidden = true)
    private Value<String> description;

    @FieldInfo(label = "Datasets", layout = FieldInfo.Layout.GROUP)
    private List<TargetInternalReference> dataset;

    @FieldInfo(label = "Related publications", markdown = true, hint = "List of publications that have been published as a part of this project.", layout=FieldInfo.Layout.GROUP)
    private List<Value<String>> publications;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    @Override
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public void setType(String type) {
        setType(StringUtils.isBlank(type) ? null : new Value<>(type));
    }

    public void setIdentifier(List<String> identifier) {
        this.identifier = identifier;
    }

    public void setEditorId(String editorId){
        setEditorId(StringUtils.isBlank(editorId) ? null : new Value<>(editorId));
    }

    public void setTitle(String title){
        setTitle(StringUtils.isBlank(title) ? null : new Value<>(title));
    }

    public void setDescription(String description){
        setDescription(StringUtils.isBlank(description) ? null : new Value<>(description));
    }

    public List<Value<String>> getPublications() {
        return publications;
    }

    public void setPublications(List<String> publications) {
        this.publications = publications == null ? null : publications.stream().map(Value::new).collect(Collectors.toList());
    }

    public Value<String> getType() {
        return type;
    }

    public void setType(Value<String> type) {
        this.type = type;
    }

    @Override
    public List<String> getIdentifier() {
        return identifier;
    }

    public Value<String> getDescription() {
        return description;
    }

    public void setDescription(Value<String> description) {
        this.description = description;
    }

    public List<TargetInternalReference> getDataset() {
        return dataset;
    }

    public void setDataset(List<TargetInternalReference> dataset) {
        this.dataset = dataset;
    }

    public Value<String> getTitle() {
        return title;
    }

    public void setTitle(Value<String> title) {
        this.title = title;
    }

    public ISODateValue getFirstRelease() {
        return firstRelease;
    }

    public void setFirstRelease(ISODateValue firstRelease) {
        this.firstRelease = firstRelease;
    }

    public void setFirstRelease(Date firstRelease) {
        this.setFirstRelease(firstRelease != null ? new ISODateValue(firstRelease) : null);
    }

    public ISODateValue getLastRelease() {
        return lastRelease;
    }

    public void setLastRelease(ISODateValue lastRelease) {
        this.lastRelease = lastRelease;
    }

    public void setLastRelease(Date lastRelease) {
        this.setLastRelease(lastRelease != null ? new ISODateValue(lastRelease) : null);
    }

    public Value<String> getEditorId() {
        return editorId;
    }

    public void setEditorId(Value<String> editorId) {
        this.editorId = editorId;
    }
}
