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

package eu.ebrains.kg.common.model.target.elasticsearch.instances.commons;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import eu.ebrains.kg.common.model.target.elasticsearch.ElasticSearchInfo;
import lombok.EqualsAndHashCode;

import java.util.Comparator;
import java.util.List;

@EqualsAndHashCode
public class TargetInternalReference implements Comparable<TargetInternalReference> {
    private TargetInternalReference() {
    }

    public TargetInternalReference(String reference, String value) {
        this.reference = reference;
        this.value = value;
    }

    public TargetInternalReference(String reference, String value, Context context) {
        this.reference = reference;
        this.value = value;
        this.context = context;
    }

    @ElasticSearchInfo(ignoreAbove = 256)
    private String reference;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String value;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> count;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Context context;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getValue() {
        return value;
    }

    @JsonIgnore
    private String getLowerCaseValue() {
        return value != null ? value.toLowerCase() : null;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public int compareTo(TargetInternalReference targetInternalReference) {
        return targetInternalReference == null ? -1 : Comparator.comparing(TargetInternalReference::getLowerCaseValue).compare(this, targetInternalReference);
    }

    public List<String> getCount() {
        return count;
    }

    public void setCount(List<String> count) {
        this.count = count;
    }

    public static class Context {

        private String tab;

        private String targetId;

        public Context(String tab) {
            this.tab = tab;
        }

        public Context(String tab, String targetId) {
            this.tab = tab;
            this.targetId = targetId;
        }

        public String getTab() {
            return tab;
        }

        public void setTab(String tab) {
            this.tab = tab;
        }

        public String getTargetId() {
            return targetId;
        }

        public void setTargetId(String targetId) {
            this.targetId = targetId;
        }
    }
}
