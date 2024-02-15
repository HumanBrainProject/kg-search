/*
 *  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *  Copyright 2021 - 2023 EBRAINS AISBL
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  This open source software code was developed in part or in whole in the
 *  Human Brain Project, funded from the European Union's Horizon 2020
 *  Framework Programme for Research and Innovation under
 *  Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 *  (Human Brain Project SGA1, SGA2 and SGA3).
 */

package eu.ebrains.kg.projects.ebrains.target;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ebrains.kg.common.model.target.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@MetaInfo(name = "Behavioral protocol")
public class BehavioralProtocol implements TargetInstance {

    @JsonIgnore
    private List<String> allIdentifiers;

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> type = new Value<>("Behavioral protocol");

    @ElasticSearchInfo(type = "keyword")
    private Value<String> category;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> disclaimer;

    @FieldInfo(label = "Name", layout = "header", labelHidden = true)
    private Value<String> title;

    @FieldInfo(label = "Official abbreviation")
    private Value<String> officialAbbreviation;

    @FieldInfo(fieldType = FieldInfo.FieldType.MARKDOWN, overview = true)
    private Value<String> description;

    @FieldInfo(label = "Described in", fieldType = FieldInfo.FieldType.MARKDOWN)
    private Value<String> describedIn;

    @FieldInfo(label = "Described in")
    private TargetExternalReference describedInLink;

    @FieldInfo(ignoreForSearch = true, visible = false)
    private SchemaOrgInstance meta;

    @FieldInfo(layout = "How to use", label = "Programmatic access to metadata", ignoreForSearch = true)
    private Value<String> queryBuilderText;

    @Override
    @JsonIgnore
    public boolean isSearchableInstance() { return false; }

}
