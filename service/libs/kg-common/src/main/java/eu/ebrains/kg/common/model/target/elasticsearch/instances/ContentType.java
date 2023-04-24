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

package eu.ebrains.kg.common.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.common.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.ISODateValue;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.schemaorg.SchemaOrgInstance;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@MetaInfo(name = "Content type")
public class ContentType implements TargetInstance {

    @JsonIgnore
    private List<String> allIdentifiers;

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> category;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> disclaimer;

    @JsonProperty("first_release")
    @FieldInfo(ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    @FieldInfo(ignoreForSearch = true, visible = false)
    private SchemaOrgInstance meta;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> type = new Value<>("Content type");

    @FieldInfo(label = "Name", layout = "header", labelHidden = true)
    private Value<String> title;


    @FieldInfo(fieldType = FieldInfo.FieldType.MARKDOWN, overview = true)
    private Value<String> description;

    @FieldInfo(label = "File extensions")
    private List<Value<String>> fileExtensions;

    @FieldInfo(label = "Related media type")
    private TargetExternalReference relatedMediaType;

    @FieldInfo(label = "Specification")
    private Value<String> specification;

    @FieldInfo(label = "Synonyms", layout = "summary")
    private List<Value<String>> synonyms;

    @FieldInfo(label = "Data type")
    private TargetInternalReference dataType;

    @FieldInfo(layout = "Software", label = "Input format for")
    private List<TargetInternalReference> inputFormatForSoftware;

    @FieldInfo(layout = "Software", label = "Output format of")
    private List<TargetInternalReference> outputFormatForSoftware;

    @FieldInfo(layout = "Datasets")
    private List<TargetInternalReference> datasets;

    @FieldInfo(layout = "Models")
    private List<TargetInternalReference> models;

    @Override
    @JsonIgnore
    public boolean isSearchableInstance() { return false; }

}
