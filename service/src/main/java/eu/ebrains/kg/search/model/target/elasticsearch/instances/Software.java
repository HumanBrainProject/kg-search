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
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@MetaInfo(name = "SoftwareVersions", identifier = "https://openminds.ebrains.eu/core/Software", order = 2)
public class Software implements TargetInstance {
    @ElasticSearchInfo(type = "keyword")
    private final Value<String> type = new Value<>("SoftwareVersions");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

    @FieldInfo(label = "Name", sort = true, layout = FieldInfo.Layout.HEADER)
    private Value<String> title;

    @FieldInfo(label = "Description", labelHidden = true, markdown = true, boost = 2)
    private Value<String> description;

    @FieldInfo(label = "Custodians", separator = "; ", hint = "A custodian is the person responsible for the data bundle.", boost = 10)
    private List<TargetInternalReference> custodians;

    @FieldInfo(label = "Developers", separator = "; ", boost = 10)
    private List<TargetInternalReference> developers;

    @FieldInfo(label = "Cite software", isButton = true, markdown = true, icon="quote-left")
    private Value<String> citation;

    @FieldInfo(label = "DOI", hint = "This is the software DOI representing all the underlying software's versions you must cite if you reuse this data in a way that leads to a publication")
    private Value<String> doi;

    @FieldInfo(label = "Versions")
    private List<TargetInternalReference> versions;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    @Override
    @JsonIgnore
    public boolean isSearchable() {
        return false;
    }

    public void setTitle(String title) {
        setTitle(StringUtils.isBlank(title) ? null : new Value<>(title));
    }

    public void setDescription(String description) {
        setDescription(StringUtils.isBlank(description) ? null : new Value<>(description));
    }

    public void setCitation(String citation) {
        setCitation(StringUtils.isBlank(citation) ? null : new Value<>(citation));
    }

    public void setDoi(String doi) {
        setDoi(StringUtils.isBlank(doi) ? null : new Value<>(doi));
    }

    public Value<String> getType() {
        return type;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public List<String> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(List<String> identifier) {
        this.identifier = identifier;
    }

    public Value<String> getDescription() {
        return description;
    }

    public Value<String> getTitle() {
        return title;
    }

    public void setTitle(Value<String> title) {
        this.title = title;
    }

    public void setDescription(Value<String> description) {
        this.description = description;
    }

    public List<TargetInternalReference> getDevelopers() {
        return developers;
    }

    public void setDevelopers(List<TargetInternalReference> developers) {
        this.developers = developers;
    }

    public List<TargetInternalReference> getCustodians() {
        return custodians;
    }

    public void setCustodians(List<TargetInternalReference> custodians) {
        this.custodians = custodians;
    }

    public Value<String> getCitation() {
        return citation;
    }

    public void setCitation(Value<String> citation) {
        this.citation = citation;
    }

    public Value<String> getDoi() {
        return doi;
    }

    public void setDoi(Value<String> doi) {
        this.doi = doi;
    }

    public List<TargetInternalReference> getVersions() {
        return versions;
    }

    public void setVersions(List<TargetInternalReference> versions) {
        this.versions = versions;
    }
}
