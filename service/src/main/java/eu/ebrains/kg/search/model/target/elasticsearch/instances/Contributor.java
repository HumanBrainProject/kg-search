package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.ISODateValue;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@MetaInfo(name="Contributor", identifier = "uniminds/core/person/v1.0.0/search", order=6)
public class Contributor {

    private Value<String> type = new Value<>("Contributor");

    @FieldInfo(visible = false)
    private Value<String> identifier;

    @FieldInfo(layout = FieldInfo.Layout.HEADER)
    private Value<String> editorId;

    @FieldInfo(optional = false, sort = true, label = "Name", boost = 20f)
    private Value<String> title;

    @FieldInfo(label = "Custodian of", layout = FieldInfo.Layout.GROUP, overview = true)
    private List<TargetInternalReference> custodianOf;

    @FieldInfo(label = "Custodian of model", layout = FieldInfo.Layout.GROUP, overview = true)
    private List<TargetInternalReference> custodianOfModel;

    @FieldInfo(label = "Publications", markdown = true, facet = FieldInfo.Facet.EXISTS, layout = FieldInfo.Layout.GROUP)
    private List<Value<String>> publications;

    @FieldInfo(label = "Contributions", facet = FieldInfo.Facet.EXISTS, layout = FieldInfo.Layout.GROUP, type = FieldInfo.Type.TEXT, overview = true)
    private List<TargetInternalReference> contributions;

    @FieldInfo(label = "Model contributions", facet = FieldInfo.Facet.EXISTS, layout = FieldInfo.Layout.GROUP, type = FieldInfo.Type.TEXT, overview = true)
    private List<TargetInternalReference> modelContributions;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

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

    public Value<String> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Value<String> identifier) {
        this.identifier = identifier;
    }

    public Value<String> getEditorId() {
        return editorId;
    }

    public void setEditorId(Value<String> editorId) {
        this.editorId = editorId;
    }

    public List<TargetInternalReference> getCustodianOfModel() {
        return custodianOfModel;
    }

    public void setCustodianOfModel(List<TargetInternalReference> custodianOfModel) {
        this.custodianOfModel = (custodianOfModel == null || custodianOfModel.isEmpty())? null : custodianOfModel;
    }

    public List<Value<String>> getPublications() {
        return publications;
    }

    public void setPublications(List<String> publications) {
        this.publications = (publications == null || publications.isEmpty())? null : publications.stream().map(Value::new).collect(Collectors.toList());
    }

    public ISODateValue getFirstRelease() {
        return firstRelease;
    }

    public void setFirstRelease(ISODateValue firstRelease) {
        this.firstRelease = firstRelease;
    }

    public ISODateValue getLastRelease() {
        return lastRelease;
    }

    public void setLastRelease(ISODateValue lastRelease) {
        this.lastRelease = lastRelease;
    }

    public List<TargetInternalReference> getCustodianOf() {
        return custodianOf;
    }

    public void setCustodianOf(List<TargetInternalReference> custodianOf) {
        this.custodianOf = (custodianOf == null || custodianOf.isEmpty())? null : custodianOf;
    }

    public List<TargetInternalReference> getContributions() {
        return contributions;
    }

    public void setContributions(List<TargetInternalReference> contributions) {
        this.contributions = (contributions == null || contributions.isEmpty())? null : contributions;
    }

    public List<TargetInternalReference> getModelContributions() {
        return modelContributions;
    }

    public void setModelContributions(List<TargetInternalReference> modelContributions) {
        this.modelContributions = (modelContributions == null || modelContributions.isEmpty())? null : modelContributions;
    }

    public Value<String> getTitle() {
        return title;
    }

    public void setTitle(Value<String> title) {
        this.title = title;
    }

    public Value<String> getType() {
        return type;
    }

    public void setType(Value<String> type) {
        this.type = type;
    }
}
