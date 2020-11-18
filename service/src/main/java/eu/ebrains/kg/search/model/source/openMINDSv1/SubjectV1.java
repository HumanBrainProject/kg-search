package eu.ebrains.kg.search.model.source.openMINDSv1;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.source.commons.Reference;
import eu.ebrains.kg.search.model.source.commons.SpecimenGroup;

import java.util.Date;
import java.util.List;

public class SubjectV1 {
    private String identifier;
    private String editorId;
    private String title;
    private String weight;
    private String genotype;
    private String strain;
    private String age;
    private String strains;
    private Date lastReleaseAt;
    private Date firstReleaseAt;
    @JsonProperty("agecategory") // TODO: change capitalization
    private List<String> ageCategory;
    private List<String> species;
    private List<String> sex;
    private List<Sample> samples;
    private List<String> datasetExists;
    private List<SpecimenGroup> datasets;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getEditorId() {
        return editorId;
    }

    public void setEditorId(String editorId) {
        this.editorId = editorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getGenotype() {
        return genotype;
    }

    public void setGenotype(String genotype) {
        this.genotype = genotype;
    }

    public String getStrain() {
        return strain;
    }

    public void setStrain(String strain) {
        this.strain = strain;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getStrains() {
        return strains;
    }

    public void setStrains(String strains) {
        this.strains = strains;
    }

    public Date getLastReleaseAt() {
        return lastReleaseAt;
    }

    public void setLastReleaseAt(Date lastReleaseAt) {
        this.lastReleaseAt = lastReleaseAt;
    }

    public Date getFirstReleaseAt() {
        return firstReleaseAt;
    }

    public void setFirstReleaseAt(Date firstReleaseAt) {
        this.firstReleaseAt = firstReleaseAt;
    }

    public List<String> getAgeCategory() {
        return ageCategory;
    }

    public void setAgeCategory(List<String> ageCategory) {
        this.ageCategory = ageCategory;
    }

    public List<String> getSpecies() {
        return species;
    }

    public void setSpecies(List<String> species) {
        this.species = species;
    }

    public List<String> getSex() {
        return sex;
    }

    public void setSex(List<String> sex) {
        this.sex = sex;
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public void setSamples(List<Sample> samples) {
        this.samples = samples;
    }

    public List<String> getDatasetExists() {
        return datasetExists;
    }

    public void setDatasetExists(List<String> datasetExists) {
        this.datasetExists = datasetExists;
    }

    public List<SpecimenGroup> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<SpecimenGroup> specimenGroups) {
        this.datasets = specimenGroups;
    }

    public static class Sample extends Reference {
    }

}
