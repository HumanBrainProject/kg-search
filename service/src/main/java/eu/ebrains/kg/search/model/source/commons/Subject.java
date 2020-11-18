package eu.ebrains.kg.search.model.source.commons;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Subject {

    public Subject() {}

    public Subject(String age, List<String> ageCategory, String weight, String genotype, String relativeUrl, String strains, String species, String strain, List<String> sex, String name, String identifier) {
        this.age = age;
        this.ageCategory = ageCategory;
        this.weight = weight;
        this.genotype = genotype;
        this.relativeUrl = relativeUrl;
        this.strains = strains;
        this.species = species;
        this.strain = strain;
        this.sex = sex;
        this.name = name;
        this.identifier = identifier;
    }

    private String age;
    @JsonProperty("agecategory") // TODO: change capitalization
    private List<String> ageCategory;
    private String weight;
    private String genotype;
    private String relativeUrl;
    private String strains;
    private String species;
    private String strain;
    private List<String> sex;
    private String name;
    private String identifier;

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

    public String getSpecies() { return species; }

    public void setSpecies(String species) { this.species = species; }

    public String getStrain() { return strain; }

    public void setStrain(String strain) { this.strain = strain; }

    public List<String> getSex() { return sex; }

    public void setSex(List<String> sex) { this.sex = sex; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getIdentifier() { return identifier; }

    public void setIdentifier(String identifier) { this.identifier = identifier; }
}