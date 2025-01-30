/*
 *  Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *  Copyright 2021 - 2024 EBRAINS AISBL
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

package eu.ebrains.kg.projects.tefHealth.target;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ebrains.kg.common.model.target.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@MetaInfo(name = "Organization", searchable = true, sortByRelevance = false)
public class Organization implements TargetInstance {

    @JsonIgnore
    private List<String> allIdentifiers;

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> type = new Value<>("Organization");

    @FieldInfo(label = "Name", layout = "header", labelHidden = true, boost = 20, useForSuggestion = true)
    private Value<String> title;

    @FieldInfo(label = "Abbreviation", useForSuggestion = true)
    private Value<String> abbreviation;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> category;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> disclaimer;

    @FieldInfo(ignoreForSearch = true, visible = false)
    private SchemaOrgInstance meta;

    @FieldInfo(label = "Services", useForSuggestion = true)
    private List<TargetInternalReference> providedServices;

    @FieldInfo(layout = "header", label = "Country", labelHidden = true, facet = FieldInfo.Facet.LIST, type = FieldInfo.Type.TEXT, isFilterableFacet = true, useForSuggestion = true)
    private TargetInternalReference country;

    @FieldInfo(fieldType = FieldInfo.FieldType.FILE_PREVIEW, layout = "Description", labelHidden = true)
    private TargetExternalReference businessCard;

    @Override
    public boolean isSearchableInstance() {
        return true;
    }
}
