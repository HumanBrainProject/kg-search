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
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.*;
import eu.ebrains.kg.common.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.TargetInstance;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@MetaInfo(name = "ModelVersions")
public class Model implements TargetInstance, HasCitation {
    @JsonIgnore
    private List<String> allIdentifiers;
    @ElasticSearchInfo(type = "keyword")
    private final Value<String> type = new Value<>("ModelVersions");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> category;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> disclaimer;

    @FieldInfo(label = "Name", boost = 20)
    private Value<String> title;

    @FieldInfo(label = "Description", markdown = true, boost = 2, labelHidden = true, overview = true)
    private Value<String> description;

    @FieldInfo(layout = "How to cite", labelHidden = true, isCitation=true)
    private Value<String> citation;

    @FieldInfo(layout = "How to cite", labelHidden = true, isCitation=true)
    private Value<String> customCitation;

    @FieldInfo(layout = "How to cite", labelHidden = true)
    private Value<String> citationHint;

    @FieldInfo(label = "DOI", hint = "This is the model DOI representing all the underlying model's versions you must cite if you reuse this data in a way that leads to a publication")
    private Value<String> doi;

    @FieldInfo(label = "Homepage")
    private TargetExternalReference homepage;

    @FieldInfo(label = "Study target", layout = "summary")
    private List<TargetInternalReference> studyTarget;

    @FieldInfo(label = "Scope", layout = "summary", facet = FieldInfo.Facet.LIST)
    private TargetInternalReference scope;

    @FieldInfo(label = "Abstraction level", layout = "summary", facet = FieldInfo.Facet.LIST)
    private TargetInternalReference abstractionLevel;

    @FieldInfo(label = "Contributors", layout = "header", separator = "; ", type = FieldInfo.Type.TEXT, labelHidden = true, boost = 10)
    private List<TargetInternalReference> contributors;

    @FieldInfo(label = "Custodian", layout = "summary", separator = "; ", type = FieldInfo.Type.TEXT, hint = "A custodian is the person responsible for the data bundle.")
    private List<TargetInternalReference> custodians;

    @FieldInfo(label = "Model versions", isTable = true, layout = "Model versions", labelHidden = true)
    private List<Children<Version>> models;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    @Override
    @JsonIgnore
    public boolean isSearchableInstance() {
        return false;
    }

}
