package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.ISODateValue;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@MetaInfo(name = "Subject", identifier = "minds/experiment/subject/v1.0.0/search", order = 3)
public class Subject implements TargetInstance {
    @ElasticSearchInfo(type = "keyword")
    private Value<String> type = new Value<>("Subject");

    @FieldInfo(visible = false)
    private Value<String> identifier;

    @FieldInfo(label = "Name", sort = true, boost = 20)
    private Value<String> title;

    @FieldInfo(layout = FieldInfo.Layout.HEADER)
    private Value<String> editorId;

    @FieldInfo(label = "Species", facet = FieldInfo.Facet.LIST, type = FieldInfo.Type.TEXT, overview = true)
    private List<Value<String>> species;

    @FieldInfo(label = "Sex", facet = FieldInfo.Facet.LIST, overview = true)
    private List<Value<String>> sex;

    @FieldInfo(label = "Age", overview = true)
    private Value<String> age;

    @JsonProperty("agecategory")
    @FieldInfo(label = "Age category", overview = true)
    private List<Value<String>> ageCategory;

    @FieldInfo(label = "Weight")
    private Value<String> weight;

    @FieldInfo(label = "Strain", facet = FieldInfo.Facet.LIST, overview = true)
    private Value<String> strain;

    @FieldInfo(label = "Genotype", facet = FieldInfo.Facet.LIST, overview = true)
    private Value<String> genotype;

    @FieldInfo(label = "Samples", layout = FieldInfo.Layout.GROUP, hint = "List of samples that have been obtained from a given subject.", aggregate = FieldInfo.Aggregate.COUNT)
    private List<TargetInternalReference> samples;

    @FieldInfo(label = "Datasets", visible = false, facet = FieldInfo.Facet.EXISTS)
    private List<Value<String>> datasetExists;

    @FieldInfo(label = "Datasets", layout = FieldInfo.Layout.GROUP, type = FieldInfo.Type.TEXT, hint = "List of datasets in which the subject was used to produce data.")
    private List<Children<Dataset>> datasets;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    public void setType(String type) {
        setType(StringUtils.isBlank(type) ? null : new Value<>(type));
    }

    public void setIdentifier(String identifier) {
        setIdentifier(identifier != null ? new Value<>(identifier) : null);
    }

    public void setEditorId(String editorId) {
        setEditorId(StringUtils.isBlank(editorId) ? null : new Value<>(editorId));
    }

    public void setTitle(String title) {
        setTitle(StringUtils.isBlank(title) ? null : new Value<>(title));
    }

    public Value<String> getType() {
        return type;
    }

    public void setType(Value<String> type) {
        this.type = type;
    }

    @Override
    public Value<String> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Value<String> identifier) {
        this.identifier = identifier;
    }

    public Value<String> getTitle() {
        return title;
    }

    public void setTitle(Value<String> title) {
        this.title = title;
    }

    public Value<String> getEditorId() {
        return editorId;
    }

    public void setEditorId(Value<String> editorId) {
        this.editorId = editorId;
    }

    public List<Value<String>> getSpecies() {
        return species;
    }

    public void setSpecies(List<String> species) {
        this.species = species == null ? null : species.stream().map(Value::new).collect(Collectors.toList());
    }

    public List<Value<String>> getSex() {
        return sex;
    }

    public void setSex(List<String> sex) {
        this.sex = sex == null ? null : sex.stream().map(Value::new).collect(Collectors.toList());
    }

    public Value<String> getAge() {
        return age;
    }

    public void setAge(String age) {
        setAge(StringUtils.isBlank(age) ? null : new Value<>(age));
    }

    public void setAge(Value<String> age) {
        this.age = age;
    }

    public List<Value<String>> getAgeCategory() {
        return ageCategory;
    }

    public void setAgeCategory(List<String> ageCategory) {
        this.ageCategory = ageCategory == null ? null : ageCategory.stream().map(Value::new).collect(Collectors.toList());
    }

    public Value<String> getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        setWeight(StringUtils.isBlank(weight) ? null : new Value<>(weight));
    }

    public void setWeight(Value<String> weight) {
        this.weight = weight;
    }

    public Value<String> getStrain() {
        return strain;
    }

    public void setStrain(String strain) {
        setStrain(StringUtils.isBlank(strain) ? null : new Value<>(strain));
    }

    public void setStrain(Value<String> strain) {
        this.strain = strain;
    }

    public Value<String> getGenotype() {
        return genotype;
    }

    public void setGenotype(String genotype) {
        setGenotype(StringUtils.isBlank(genotype) ? null : new Value<>(genotype));
    }

    public void setGenotype(Value<String> genotype) {
        this.genotype = genotype;
    }

    public List<TargetInternalReference> getSamples() {
        return samples;
    }

    public void setSamples(List<TargetInternalReference> samples) {
        this.samples = samples;
    }

    public List<Value<String>> getDatasetExists() {
        return datasetExists;
    }

    public void setDatasetExists(List<String> datasetExists) {
        this.datasetExists = datasetExists == null ? null : datasetExists.stream().map(Value::new).collect(Collectors.toList());
    }

    public List<Children<Dataset>> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<Dataset> datasets) {
        this.datasets = datasets == null ? null : datasets.stream().map(Children::new).collect(Collectors.toList());
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

    public static class Dataset {

        public Dataset() {
        }

        public Dataset(List<String> component, List<TargetInternalReference> name) {
            this.component = component == null ? null : component.stream().map(Value::new).collect(Collectors.toList());
            this.name = name;
        }

        private List<Value<String>> component;
        private List<TargetInternalReference> name;

        public List<Value<String>> getComponent() {
            return component;
        }

        public void setComponent(List<Value<String>> component) {
            this.component = component;
        }

        public List<TargetInternalReference> getName() {
            return name;
        }

        public void setName(List<TargetInternalReference> name) {
            this.name = name;
        }
    }

}
