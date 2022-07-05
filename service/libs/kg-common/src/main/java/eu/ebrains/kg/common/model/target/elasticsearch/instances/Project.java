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
import eu.ebrains.kg.common.model.target.elasticsearch.*;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.ISODateValue;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@MetaInfo(name="Project", order=1, searchable=true)
@RibbonInfo(aggregation="count", dataField="datasets", singular="dataset", plural="datasets")
public class Project implements TargetInstance {
    @JsonIgnore
    private List<String> allIdentifiers;
    @ElasticSearchInfo(type = "keyword")
    private Value<String> type = new Value<>("Project");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(visible = false, ignoreForSearch = true)
    private List<String> identifier;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> category;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> disclaimer;

    @FieldInfo(sort = true, label = "Name", boost = 20)
    private Value<String> title;

    @FieldInfo(layout="header")
    private Value<String> editorId;

    @FieldInfo(label = "Description", markdown =  true, boost =  7.5f, labelHidden = true, overview = true)
    private Value<String> description;

    @FieldInfo(label = "Datasets", layout = "Datasets", labelHidden = true)
    private List<TargetInternalReference> dataset;

    @FieldInfo(label = "Models", layout = "Models", labelHidden = true)
    private List<TargetInternalReference> models;

    @FieldInfo(label = "Software", layout = "Software", labelHidden = true)
    private List<TargetInternalReference> software;

    @FieldInfo(label = "(Meta)Data models", layout = "(Meta)Data models", labelHidden = true)
    private List<TargetInternalReference> metaDataModels;

    @FieldInfo(label = "Related publications", markdown = true, labelHidden = true, layout = "Publications")
    private List<Value<String>> publications;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type=FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    @Override
    @JsonIgnore
    public boolean isSearchableInstance() {
        return true;
    }

}
