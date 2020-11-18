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
}