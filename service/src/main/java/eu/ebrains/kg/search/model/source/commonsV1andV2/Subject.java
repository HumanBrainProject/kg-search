/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

package eu.ebrains.kg.search.model.source.commonsV1andV2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

public class Subject {

    private String age;
    @JsonProperty("agecategory") // TODO: change capitalization
    private List<String> ageCategory;
    private String weight;
    private String genotype;
    private String relativeUrl;
    @JsonDeserialize(using = ListOrSingleStringAsStringDeserializer.class)
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