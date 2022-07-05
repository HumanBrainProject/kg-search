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
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Children;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.ISODateValue;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.common.model.target.elasticsearch.instances.commons.Value;
import eu.ebrains.kg.common.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.common.model.target.elasticsearch.TargetInstance;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@MetaInfo(name = "Subject")
public class Subject implements TargetInstance {
    @JsonIgnore
    private List<String> allIdentifiers;

    @ElasticSearchInfo(type = "keyword")
    private Value<String> type = new Value<>("Subject");

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

    @FieldInfo(layout = "header")
    private Value<String> editorId;

    @FieldInfo(label = "Species", type = FieldInfo.Type.TEXT, overview = true, facet = FieldInfo.Facet.LIST)
    private List<Value<String>> species;

    @FieldInfo(label = "Sex", overview = true, facet = FieldInfo.Facet.LIST)
    private List<Value<String>> sex;

    @FieldInfo(label = "Age", overview = true)
    private Value<String> age;

    @JsonProperty("agecategory")
    @FieldInfo(label = "Age category", overview = true)
    private List<Value<String>> ageCategory;

    @FieldInfo(label = "Weight")
    private Value<String> weight;

    @FieldInfo(label = "Strain", overview = true, facet = FieldInfo.Facet.LIST)
    private Value<String> strain;

    @FieldInfo(label = "Genotype", overview = true, facet = FieldInfo.Facet.LIST)
    private Value<String> genotype;

    @FieldInfo(label = "Samples", aggregate = FieldInfo.Aggregate.COUNT, layout = "Samples")
    private List<TargetInternalReference> samples;

    @FieldInfo(label = "Datasets", type = FieldInfo.Type.TEXT, layout = "Datasets")
    private List<Children<Dataset>> datasets;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    @FieldInfo(ignoreForSearch = true, visible = false, facet = FieldInfo.Facet.EXISTS)
    private boolean datasetExists;

    @Override
    public boolean isSearchableInstance() {
        return false;
    }


    @Getter
    @Setter
    public static class Dataset {

        public Dataset() {
        }

        public Dataset(List<String> component, List<TargetInternalReference> name) {
            this.component = component == null ? null : component.stream().map(Value::new).collect(Collectors.toList());
            this.name = name;
        }

        @FieldInfo
        private List<Value<String>> component;
        @FieldInfo
        private List<TargetInternalReference> name;
    }

}
