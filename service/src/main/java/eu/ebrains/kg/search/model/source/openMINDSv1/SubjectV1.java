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

package eu.ebrains.kg.search.model.source.openMINDSv1;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.ebrains.kg.search.model.source.SourceInstance;
import eu.ebrains.kg.search.model.source.commons.ListOrSingleStringAsStringDeserializer;
import eu.ebrains.kg.search.model.source.commons.SourceInternalReference;
import eu.ebrains.kg.search.model.source.commons.SpecimenGroup;

import java.util.Date;
import java.util.List;

public class SubjectV1 implements SourceInstance {
    private String id;
    private String identifier;
    private String editorId;
    private String title;
    private String weight;
    private String genotype;
    private String strain;
    private String age;
    @JsonDeserialize(using = ListOrSingleStringAsStringDeserializer.class)
    private String strains;
    private Date lastReleaseAt;
    private Date firstReleaseAt;
    @JsonProperty("agecategory") // TODO: change capitalization
    private List<String> ageCategory;
    private List<String> species;
    private List<String> sex;
    private List<SourceInternalReference> samples;
    private List<String> datasetExists;
    private List<SpecimenGroup> datasets;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

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
