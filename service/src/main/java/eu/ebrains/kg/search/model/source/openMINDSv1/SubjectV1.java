package eu.ebrains.kg.search.model.source.openMINDSv1;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.ebrains.kg.search.model.source.commonsV1andV2.ListOrSingleStringAsStringDeserializer;
import eu.ebrains.kg.search.model.source.SourceInstanceV1andV2;
import eu.ebrains.kg.search.model.source.commonsV1andV2.SourceInternalReference;
import eu.ebrains.kg.search.model.source.commonsV1andV2.SpecimenGroup;

import java.util.List;

public class SubjectV1 extends SourceInstanceV1andV2 {
    private String title;
    private String weight;
    private String genotype;
    private String strain;
    private String age;
    @JsonDeserialize(using = ListOrSingleStringAsStringDeserializer.class)
    private String strains;
    @JsonProperty("agecategory") // TODO: change capitalization
    private List<String> ageCategory;
    private List<String> species;
    private List<String> sex;
    private List<SourceInternalReference> samples;
    private List<String> datasetExists;
    private List<SpecimenGroup> datasets;

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

    public List<SourceInternalReference> getSamples() {
        return samples;
    }

    public void setSamples(List<SourceInternalReference> samples) {
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
}
