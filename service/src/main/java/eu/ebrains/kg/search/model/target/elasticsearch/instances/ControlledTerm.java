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
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;

import java.util.List;

@MetaInfo(name = "Controlled term")
public class ControlledTerm implements TargetInstance {

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> type = new Value<>("Controlled term");

    @FieldInfo(label = "Name", sort=true, layout = "header", labelHidden = true)
    private Value<String> title;

    @FieldInfo(label = "Ontology identifier")
    private Value<String> ontologyIdentifier;

    @FieldInfo(label = "Definition")
    private Value<String> definition;

    @FieldInfo(label = "Description")
    private Value<String> description;

    @FieldInfo(label = "External definitions")
    private List<TargetExternalReference> externalDefinitions;

    @FieldInfo(label = "Synonyms")
    private List<Value<String>> synonyms;



    public Value<String> getType() {
        return type;
    }

    public void setType(Value<String> type) {
        this.type = type;
    }

    public Value<String> getTitle() {
        return title;
    }

    public void setTitle(Value<String> title) {
        this.title = title;
    }

    public Value<String> getOntologyIdentifier() {
        return ontologyIdentifier;
    }

    public void setOntologyIdentifier(Value<String> ontologyIdentifier) {
        this.ontologyIdentifier = ontologyIdentifier;
    }

    public Value<String> getDefinition() {
        return definition;
    }

    public void setDefinition(Value<String> definition) {
        this.definition = definition;
    }

    public Value<String> getDescription() {
        return description;
    }

    public void setDescription(Value<String> description) {
        this.description = description;
    }

    public List<TargetExternalReference> getExternalDefinitions() {
        return externalDefinitions;
    }

    public void setExternalDefinitions(List<TargetExternalReference> externalDefinitions) {
        this.externalDefinitions = externalDefinitions;
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

    public List<Value<String>> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<Value<String>> synonyms) {
        this.synonyms = synonyms;
    }

    @Override
    @JsonIgnore
    public boolean isSearchableInstance() { return false; }
}
