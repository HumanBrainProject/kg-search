package eu.ebrains.kg.search.model.source.commons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collections;
import java.util.List;

public class Subject {

    private String age;
    @JsonProperty("agecategory") // TODO: change capitalization
    private List<String> ageCategory;
    private String weight;
    private String genotype;
    private String relativeUrl;
    @JsonDeserialize(using = ListOrSingleStringDeserializer.class)
    private String strains;
    private List<String> species;
    private String strain;
    private List<String> sex;
    private String name;
    private String identifier;
    @JsonProperty("experimentmethod")
    private List<String> experimentMethod; // TODO: is this used??

    public List<SourceInternalReference> getSamples() { return samples; }

    public void setSamples(List<SourceInternalReference> samples) { this.samples = samples; }

    private List<SourceInternalReference> samples;

    public String getAge() { return age; }

    public void setAge(String age) { this.age = age; }

    public List<String> getAgeCategory() { return ageCategory; }

    public void setAgeCategory(List<String> ageCategory) { this.ageCategory = ageCategory; }

    public String getWeight() { return weight; }

    public void setWeight(String weight) { this.weight = weight; }

    public String getGenotype() { return genotype; }

    public void setGenotype(String genotype) { this.genotype = genotype; }

    public String getRelativeUrl() { return relativeUrl; }

    public void setRelativeUrl(String relativeUrl) { this.relativeUrl = relativeUrl; }

    public String getStrains() { return strains; }

    public void setStrains(String strains) { this.strains = strains; }

    public List<String> getSpecies() { return species; }

    public void setSpecies(List<String> species) { this.species = species; }

    public String getStrain() { return strain; }

    public void setStrain(String strain) { this.strain = strain; }

    public List<String> getSex() { return sex; }

    public void setSex(List<String> sex) { this.sex = sex; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getIdentifier() { return identifier; }

    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public List<String> getExperimentMethod() { return experimentMethod; }

    public void setExperimentMethod(List<String> experimentMethod) { this.experimentMethod = experimentMethod; }

}