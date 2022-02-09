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
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.ISODateValue;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@MetaInfo(name="Contributor", order=8, searchable=true)

public class Contributor implements TargetInstance {
    @JsonIgnore
    private List<String> allIdentifiers;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> type = new Value<>("Contributor");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(visible = false, ignoreForSearch = true)
    private List<String> identifier;

    @FieldInfo(sort = true, label = "Name", boost = 20)
    private Value<String> title;

    @FieldInfo(layout = "header")
    private Value<String> editorId;

    @FieldInfo(label = "Custodian of datasets", layout = "Custodian of datasets", labelHidden = true)
    private List<TargetInternalReference> custodianOfDataset;

    @FieldInfo(label = "Custodian of models", layout = "Custodian of models", labelHidden = true)
    private List<TargetInternalReference> custodianOfModel;

    @FieldInfo(label = "Custodian of software", layout = "Custodian of software", labelHidden = true)
    private List<TargetInternalReference> custodianOfSoftware;

    @FieldInfo(label = "Custodian of (meta)data models", layout = "Custodian of (meta)data models", labelHidden = true)
    private List<TargetInternalReference> custodianOfMetaDataModels;

    @FieldInfo(type = FieldInfo.Type.TEXT, layout = "Dataset contributions", labelHidden = true)
    private List<TargetInternalReference> datasetContributions;

    @FieldInfo(type = FieldInfo.Type.TEXT, layout = "Model contributions", labelHidden = true)
    private List<TargetInternalReference> modelContributions;

    @FieldInfo(type = FieldInfo.Type.TEXT, layout = "Software contributions", labelHidden = true)
    private List<TargetInternalReference> softwareContributions;

    @FieldInfo(type = FieldInfo.Type.TEXT, layout = "(Meta)Data model contributions", labelHidden = true)
    private List<TargetInternalReference> metaDataModelContributions;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    @Override
    public String getId() { return id; }

    @Override
    @JsonIgnore
    public boolean isSearchableInstance() {
        return true;
    }
}
