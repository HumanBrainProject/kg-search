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
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.common.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.*;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.schemaorg.SchemaOrgInstance;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@MetaInfo(name = "WebServiceVersions")
public class WebService implements TargetInstance {

    @JsonIgnore
    private List<String> allIdentifiers;

    @ElasticSearchInfo(type = "keyword")
    private final Value<String> type = new Value<>("WebServiceVersions");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

    @FieldInfo(ignoreForSearch = true, visible = false)
    private SchemaOrgInstance meta;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> category;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> disclaimer;

    @FieldInfo(label = "Name", layout = "header")
    private Value<String> title;

    @FieldInfo(label = "Description", labelHidden = true, fieldType = FieldInfo.FieldType.MARKDOWN, boost = 2, overview = true)
    private Value<String> description;

    @FieldInfo(label = "Custodians", separator = "; ", hint = "A custodian is the person responsible for the webservice.", boost = 10)
    private List<TargetInternalReference> custodians;

    @FieldInfo(label = "Developers", separator = "; ", boost = 10)
    private List<TargetInternalReference> developers;

    @FieldInfo(layout = "How to cite", labelHidden = true, fieldType = FieldInfo.FieldType.CITATION)
    private Value<String> customCitation;

    @FieldInfo(label = "Web service versions", fieldType = FieldInfo.FieldType.TABLE, layout = "Web service versions")
    private List<Children<Version>> webServiceVersions;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    @FieldInfo(layout = "How to use", label = "Programmatic access to metadata", ignoreForSearch = true)
    private Value<String> queryBuilderText;

    @Override
    @JsonIgnore
    public boolean isSearchableInstance() {
        return false;
    }

    public static class Version {
        @FieldInfo(label = "Version")
        private TargetInternalReference version;

        @FieldInfo(label = "Innovation", fieldType = FieldInfo.FieldType.MARKDOWN)
        private Value<String> innovation;

        public TargetInternalReference getVersion() {
            return version;
        }

        public void setVersion(TargetInternalReference version) {
            this.version = version;
        }

        public Value<String> getInnovation() {
            return innovation;
        }

        public void setInnovation(Value<String> innovation) {
            this.innovation = innovation;
        }
    }
}
