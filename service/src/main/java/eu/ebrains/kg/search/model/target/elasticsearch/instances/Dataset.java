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
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.*;
import org.apache.commons.lang3.StringUtils;

import java.util.List;


@MetaInfo(name = "DatasetVersions", order = 2)
public class Dataset implements TargetInstance {
    @JsonIgnore
    private List<String> allIdentifiers;

    @ElasticSearchInfo(type = "keyword")
    private final Value<String> type = new Value<>("DatasetVersions");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

    @FieldInfo(layout = "header")
    private Value<String> editorId;

    @FieldInfo(label = "Name", sort = true, layout = "header")
    private Value<String> title;

    @FieldInfo(label = "Authors", separator = "; ", layout = "header", type = FieldInfo.Type.TEXT, labelHidden = true)
    private List<TargetInternalReference> authors;

    @FieldInfo(label = "Custodians", separator = "; ", hint = "A custodian is the person responsible for the data bundle.", boost = 10)
    private List<TargetInternalReference> custodians;

    @FieldInfo(label = "Cite dataset", layout = "How to cite", labelHidden = true, isCitation=true)
    private Value<String> citation;

    @FieldInfo(label = "Cite dataset", layout = "How to cite", labelHidden = true, isCitation=true)
    private Value<String> customCitation;

    @FieldInfo(label = "DOI", hint = "This is the dataset DOI representing all the underlying datasets you must cite if you reuse this data in a way that leads to a publication")
    private Value<String> doi;

    @FieldInfo(label = "Homepage")
    private TargetExternalReference homepage;

    @FieldInfo(label = "Description", labelHidden = true, markdown = true, boost = 2)
    private Value<String> description;

    @FieldInfo(label = "Dataset versions", isTable = true, layout = "Dataset versions", labelHidden = true)
    private List<Children<Version>> datasets;

    @JsonProperty("first_release")
    @FieldInfo(label = "First release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue firstRelease;

    @JsonProperty("last_release")
    @FieldInfo(label = "Last release", ignoreForSearch = true, visible = false, type = FieldInfo.Type.DATE)
    private ISODateValue lastRelease;

    @Override
    public String getId() {
        return id;
    }

    @Override
    @JsonIgnore
    public boolean isSearchableInstance() {
        return false;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIdentifier(List<String> identifier) {
        this.identifier = identifier;
    }

    public void setEditorId(String editorId) {
        setEditorId(StringUtils.isBlank(editorId) ? null : new Value<>(editorId));
    }

    public void setTitle(String title) {
        setTitle(StringUtils.isBlank(title) ? null : new Value<>(title));
    }

    public void setDescription(String description) {
        setDescription(StringUtils.isBlank(description) ? null : new Value<>(description));
    }

    public Value<String> getType() {
        return type;
    }

    @Override
    public List<String> getIdentifier() {
        return identifier;
    }

    public Value<String> getEditorId() {
        return editorId;
    }

    public void setEditorId(Value<String> editorId) {
        this.editorId = editorId;
    }

    public Value<String> getDescription() {
        return description;
    }

    public void setDescription(Value<String> description) {
        this.description = description;
    }

    public Value<String> getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        setCitation(StringUtils.isBlank(citation) ? null : new Value<>(citation));
    }

    public void setCitation(Value<String> citation) {
        this.citation = citation;
    }

    public Value<String> getCustomCitation() {
        return customCitation;
    }

    public void setCustomCitation(String customCitation) {
        setCustomCitation(StringUtils.isBlank(customCitation) ? null : new Value<>(customCitation));
    }

    public void setCustomCitation(Value<String> customCitation) {
        this.customCitation = customCitation;
    }

    public Value<String> getTitle() {
        return title;
    }

    public void setTitle(Value<String> title) {
        this.title = title;
    }

    public Value<String> getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        setDoi(StringUtils.isBlank(doi) ? null : new Value<>(doi));
    }

    public void setDoi(Value<String> doi) {
        this.doi = doi;
    }

    public List<TargetInternalReference> getAuthors() {
        return authors;
    }

    public void setAuthors(List<TargetInternalReference> authors) {
        this.authors = authors;
    }

    public TargetExternalReference getHomepage() {
        return homepage;
    }

    public void setHomepage(TargetExternalReference homepage) {
        this.homepage = homepage;
    }

    public List<TargetInternalReference> getCustodians() {
        return custodians;
    }

    public void setCustodians(List<TargetInternalReference> custodians) {
        this.custodians = custodians;
    }

    public List<Children<Version>> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<Children<Version>> datasets) {
        this.datasets = datasets;
    }

    @Override
    public List<String> getAllIdentifiers() {
        return allIdentifiers;
    }

    public void setAllIdentifiers(List<String> allIdentifiers) {
        this.allIdentifiers = allIdentifiers;
    }
}
