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

import eu.ebrains.kg.search.model.target.elasticsearch.ElasticSearchInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.FieldInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.MetaInfo;
import eu.ebrains.kg.search.model.target.elasticsearch.TargetInstance;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetExternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.TargetInternalReference;
import eu.ebrains.kg.search.model.target.elasticsearch.instances.commons.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.List;


@MetaInfo(name = "DatasetVersions", identifier = "https://openminds.ebrains.eu/core/Dataset", order = 2)
public class Dataset implements TargetInstance {
    @ElasticSearchInfo(type = "keyword")
    private final Value<String> type = new Value<>("DatasetVersions");

    @FieldInfo(ignoreForSearch = true, visible = false)
    private String id;

    @ElasticSearchInfo(type = "keyword")
    @FieldInfo(ignoreForSearch = true, visible = false)
    private List<String> identifier;

    @FieldInfo(layout = FieldInfo.Layout.HEADER)
    private Value<String> editorId;

    @FieldInfo(label = "Name", sort = true, layout = FieldInfo.Layout.HEADER)
    private Value<String> title;

    @FieldInfo(label = "Description", labelHidden = true, markdown = true, boost = 2)
    private Value<String> description;

    @FieldInfo(label = "Authors", separator = "; ", layout = FieldInfo.Layout.HEADER, type = FieldInfo.Type.TEXT, labelHidden = true)
    private List<TargetInternalReference> authors;

    @FieldInfo(label = "Cite dataset", isButton = true, markdown = true, icon="quote-left")
    private Value<String> citation;

    @FieldInfo(label = "DOI", hint = "This is the dataset DOI representing all the underlying datasets you must cite if you reuse this data in a way that leads to a publication")
    private Value<String> doi;

    @FieldInfo(label = "Homepage")
    private TargetExternalReference homepage;

    @FieldInfo(label = "Components")
    private List<TargetInternalReference> components;

    @FieldInfo(label = "Datasets")
    private List<TargetInternalReference> datasets;

    @Override
    public String getId() { return id; }

    @Override
    public boolean isSearchable() {
        return false;
    }

    public void setId(String id) { this.id = id; }

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

    public List<TargetInternalReference> getComponents() {
        return components;
    }

    public void setComponents(List<TargetInternalReference> components) {
        this.components = components;
    }

    public List<TargetInternalReference> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<TargetInternalReference> datasets) {
        this.datasets = datasets;
    }
}
