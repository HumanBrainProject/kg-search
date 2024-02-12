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

package eu.ebrains.kg.common.model.target.elasticsearch.instances;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.schemaorg.SchemaOrgInstance;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@MetaInfo(name = "File")
public class File implements TargetInstance {
    @JsonIgnore
    private List<String> allIdentifiers;
    //Internal
    @ElasticSearchInfo(type = "keyword")
    private final Value<String> type = new Value<>("File");
    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;
    @FieldInfo(ignoreForSearch = true, visible = false)
    private SchemaOrgInstance meta;
    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;
    @ElasticSearchInfo(type = "keyword")
    private Value<String> category;
    @ElasticSearchInfo(type = "keyword")
    private Value<String> disclaimer;
    @FieldInfo(label = "Name", layout = "header")
    private Value<String> title;
    @FieldInfo(label = "IRI", visible = false)
    private TargetExternalReference iri;
    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(label = "fileRepository", visible = false)
    private String fileRepository;
    @FieldInfo(label = "Size", layout = "header")
    private Value<String> size;
    @FieldInfo(label = "Format")
    private TargetInternalReference format;
    @FieldInfo(label = "View data")
    private List<TargetExternalReference> viewer;
    @FieldInfo(label = "Software taking this file as input")
    private List<TargetInternalReference> inputTypeForSoftware;
    @FieldInfo(label = "GroupingTypes", visible = false)
    private List<GroupingType> groupingTypes;
    @FieldInfo(label = "Is used in")
    private List<TargetInternalReference> usedInResearchProducts;

    @Override
    @JsonIgnore
    public boolean isSearchableInstance() {
        return false;
    }

    @Getter
    @Setter
    public static class GroupingType {
        private String name;
        private List<TargetInternalReference> fileBundles;
    }
}
