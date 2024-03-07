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
import eu.ebrains.kg.common.model.target.FieldInfo;
import eu.ebrains.kg.common.model.target.MetaInfo;
import eu.ebrains.kg.common.model.target.TargetInstance;
import eu.ebrains.kg.common.model.target.ElasticSearchInfo;
import eu.ebrains.kg.common.model.target.TargetInternalReference;
import eu.ebrains.kg.common.model.target.Value;
import eu.ebrains.kg.common.model.target.SchemaOrgInstance;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@MetaInfo(name = "Service", defaultSelection = true, searchable = true)
public class Service implements TargetInstance {

    @JsonIgnore
    private List<String> allIdentifiers;

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> type = new Value<>("Service");

    @FieldInfo(label = "Name", layout = "header", labelHidden = true)
    private Value<String> title;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> category;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> disclaimer;

    @FieldInfo(ignoreForSearch = true, visible = false)
    private SchemaOrgInstance meta;

    @FieldInfo(label = "Description", labelHidden = true, fieldType = FieldInfo.FieldType.MARKDOWN, boost = 2, useForSuggestion = true, overview = true)
    private Value<String> description;

    @FieldInfo(label = "Provided by", facet = FieldInfo.Facet.LIST, type = FieldInfo.Type.TEXT, isFilterableFacet = true, useForSuggestion = true, overview = true)
    private List<TargetInternalReference> providedBy;

    @FieldInfo(label = "Countries", facet = FieldInfo.Facet.LIST, type = FieldInfo.Type.TEXT, isFilterableFacet = true, useForSuggestion = true)
    private List<TargetInternalReference> countries;

    @FieldInfo(label = "Service category", facet = FieldInfo.Facet.LIST, type = FieldInfo.Type.TEXT, isFilterableFacet = true, useForSuggestion = true)
    private List<Value<String>> serviceCategories;

    @FieldInfo(label = "Categories", facet = FieldInfo.Facet.LIST, type = FieldInfo.Type.TEXT, isFilterableFacet = true, useForSuggestion = true)
    private List<Value<String>> useCaseCategories;

    @FieldInfo(label = "Domain", facet = FieldInfo.Facet.LIST, type = FieldInfo.Type.TEXT, isFilterableFacet = true, useForSuggestion = true)
    private List<Value<String>> useCaseDomains;

    @FieldInfo(labelHidden = true, type = FieldInfo.Type.TEXT)
    private Value<String> useCaseDomainOtherDescription;


    @Override
    public boolean isSearchableInstance() {
        return true;
    }
}
