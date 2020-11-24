package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.RibbonInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@MetaInfo(name="Project", identifier = "uniminds/core/placomponent/v1.0.0/search", order=1)
@RibbonInfo(content="Datasets", aggregation="count", dataField="search:datasets", singular="dataset", plural="datasets", icon="<i class=\"fa fa-download\" aria-hidden=\"true\"></i>")
public class Project {

    private Value<String> type = new Value<>("Project");

    @FieldInfo(visible = false)
    private Value<String> identifier;

    @FieldInfo(layout=FieldInfo.Layout.HEADER)
    private Value<String> editorId;

    @FieldInfo(optional = false, sort = true, label = "Name", boost = 20f)
    private Value<String> title;

    @FieldInfo(label = "Description", markdown =  true, boost =  7.5f, labelHidden = true, type=FieldInfo.Type.TEXT)
    private Value<String> description;

    @FieldInfo(label = "Related publications", markdown = true, hint = "List of publications that have been published as a part of this project.", layout=FieldInfo.Layout.GROUP)
    private List<Value<String>> publications;

    @FieldInfo(label = "Datasets", layout = FieldInfo.Layout.GROUP)
    private List<TargetInternalReference> dataset;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private Value<Date> firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private Value<Date> lastRelease;

    public void setType(String type) {
        setType(type!=null ? new Value<>(type) : null);
    }

    public void setIdentifier(String identifier) {
        setIdentifier(identifier!=null ? new Value<>(identifier) : null);
    }

    public void setEditorId(String editorId){
        setEditorId(editorId!=null ? new Value<>(editorId) : null);
    }

    public void setTitle(String title){
        setTitle(title!=null ? new Value<>(title) : null);
    }

    public void setDescription(String description){
        setDescription(description!=null ? new Value<>(description) : null);
    }

    public void setFirstRelease(Date firstRelease){
        setFirstRelease(firstRelease!=null ? new Value<>(firstRelease) : null);
    }

    public void setLastRelease(Date lastRelease){
        setLastRelease(lastRelease!=null ? new Value<>(lastRelease) : null);
    }

    public List<Value<String>> getPublications() {
        return publications;
    }

    public void setPublications(List<String> publications) {
        this.publications = (publications == null || publications.isEmpty())? null : publications.stream().map(Value::new).collect(Collectors.toList());
    }

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

    public List<TargetInternalReference> getDataset() {
        return dataset;
    }

    public void setDataset(List<TargetInternalReference> dataset) {
        this.dataset = (dataset == null || dataset.isEmpty())? null : dataset;
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

    public Value<String> getEditorId() {
        return editorId;
    }

    public void setEditorId(Value<String> editorId) {
        this.editorId = editorId;
    }
}
