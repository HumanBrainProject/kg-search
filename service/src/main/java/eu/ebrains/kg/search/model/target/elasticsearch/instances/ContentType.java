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

package eu.ebrains.kg.search.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;

import java.util.List;

@MetaInfo(name = "Content type")
public class ContentType implements TargetInstance {

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> type = new Value<>("Content type");

    @FieldInfo(label = "Name", sort=true, layout = FieldInfo.Layout.HEADER, labelHidden = true)
    private Value<String> name;

    @FieldInfo(label = "Description")
    private Value<String> description;

    @FieldInfo(label = "File extensions")
    private List<Value<String>> fileExtensions;

    @FieldInfo(label = "Related media type")
    private Value<String> relatedMediaType;

    @FieldInfo(label = "Specification")
    private Value<String> specification;

    @FieldInfo(label = "Synonyms")
    private List<Value<String>> synonyms;

    @FieldInfo(label = "Data type")
    private TargetInternalReference dataType;

    @FieldInfo(label = "Input format for software", layout = FieldInfo.Layout.GROUP)
    private List<TargetInternalReference> inputFormatForSoftware;

    @FieldInfo(label = "Output format for software", layout = FieldInfo.Layout.GROUP)
    private List<TargetInternalReference> outputFormatForSoftware;

    @FieldInfo(label = "Research products", isTable = true, layout = FieldInfo.Layout.GROUP)
    private List<Children<ResearchProduct>> researchProducts;


    public static class ResearchProduct{

        @FieldInfo(label = "Instance", sort = true)
        private TargetInternalReference researchProduct;

        @FieldInfo(label = "Type")
        private Value<String> type;


        public TargetInternalReference getResearchProduct() {
            return researchProduct;
        }

        public void setResearchProduct(TargetInternalReference researchProduct) {
            this.researchProduct = researchProduct;
        }

        public Value<String> getType() {
            return type;
        }

        public void setType(Value<String> type) {
            this.type = type;
        }
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public List<String> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(List<String> identifier) {
        this.identifier = identifier;
    }

    public Value<String> getType() {
        return type;
    }

    public void setType(Value<String> type) {
        this.type = type;
    }

    public Value<String> getName() {
        return name;
    }

    public void setName(Value<String> name) {
        this.name = name;
    }

    public Value<String> getDescription() {
        return description;
    }

    public void setDescription(Value<String> description) {
        this.description = description;
    }

    public List<Value<String>> getFileExtensions() {
        return fileExtensions;
    }

    public void setFileExtensions(List<Value<String>> fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

    public Value<String> getRelatedMediaType() {
        return relatedMediaType;
    }

    public void setRelatedMediaType(Value<String> relatedMediaType) {
        this.relatedMediaType = relatedMediaType;
    }

    public Value<String> getSpecification() {
        return specification;
    }

    public void setSpecification(Value<String> specification) {
        this.specification = specification;
    }

    public List<Value<String>> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<Value<String>> synonyms) {
        this.synonyms = synonyms;
    }

    public TargetInternalReference getDataType() {
        return dataType;
    }

    public void setDataType(TargetInternalReference dataType) {
        this.dataType = dataType;
    }

    public List<TargetInternalReference> getInputFormatForSoftware() {
        return inputFormatForSoftware;
    }

    public void setInputFormatForSoftware(List<TargetInternalReference> inputFormatForSoftware) {
        this.inputFormatForSoftware = inputFormatForSoftware;
    }

    public List<TargetInternalReference> getOutputFormatForSoftware() {
        return outputFormatForSoftware;
    }

    public void setOutputFormatForSoftware(List<TargetInternalReference> outputFormatForSoftware) {
        this.outputFormatForSoftware = outputFormatForSoftware;
    }

    public List<Children<ResearchProduct>> getResearchProducts() {
        return researchProducts;
    }

    public void setResearchProducts(List<Children<ResearchProduct>> researchProducts) {
        this.researchProducts = researchProducts;
    }

    @Override
    @JsonIgnore
    public boolean isSearchableInstance() { return false; }
}
